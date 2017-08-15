package xdsonfhir.backbones;

import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ehealth_connector.common.Code;
import org.ehealth_connector.common.Identificator;
import org.ehealth_connector.common.enums.Confidentiality;
import org.ehealth_connector.communication.*;
import org.ehealth_connector.communication.xd.storedquery.FindDocumentsQuery;
import org.ehealth_connector.communication.xd.storedquery.GetDocumentsQuery;
import org.hl7.fhir.dstu3.model.*;
import org.openhealthtools.ihe.xds.document.DocumentDescriptor;
import org.openhealthtools.ihe.xds.metadata.AvailabilityStatusType;
import org.openhealthtools.ihe.xds.metadata.DocumentEntryType;
import org.openhealthtools.ihe.xds.metadata.LocalizedStringType;
import org.openhealthtools.ihe.xds.response.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XdsBackbone implements Backbone {
    private static final Log log = LogFactory.getLog(XdsBackbone.class);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddhhmm");
    private static final Pattern PATIENT_ID_REGEX = Pattern.compile("(?:Patient/)?([^^]+)\\^\\^\\^&([^&]+)&.*");
    private static final String PATIENT_ID_FORMAT = "%s^^^&%s&%s";
    private final URI retrieveUri;
    private final ConvenienceCommunication comm;

    public XdsBackbone(URI repository, URI registry, URI retrieve, String orgId) {
        this.retrieveUri = retrieve;

        AffinityDomain domain = new AffinityDomain(null, new Destination(orgId, registry), new Destination(orgId, repository));
        this.comm = new ConvenienceCommunication(domain);
    }

    @Override
    public DocumentReference getDocumentReferenceById(IdType id) {
        DocumentEntryType entry = getDocumentEntryById(id.getIdPart());
        return documentEntryToReference(entry);
    }

    @Override
    public List<DocumentReference> findDocumentReferencesForPatient(ReferenceParam patient) {
        log.info("findDocumentReferencesForPatient: " + patient.getIdPart());

        Identificator patientId = stringToPatientId(patient.getIdPart());
        if (patientId == null) {
            log.info("Malformed Patiend ID " + patient.getIdPart());
            throw new InvalidRequestException("Malformed Patiend ID " + patient.getIdPart());
        }

        FindDocumentsQuery query = new FindDocumentsQuery(patientId, AvailabilityStatusType.APPROVED_LITERAL);
        XDSQueryResponseType response = comm.queryDocuments(query);

        if (!isSuccess(response.getStatus())) {
            log.error("Error retrieving documents: " + response.getErrorList().getHighestSeverity().getName());
            throw new InternalErrorException("Error retrieving documents: " + response.getErrorList().getHighestSeverity().getName());
        }

        log.info("Size = " + response.getDocumentEntryResponses().size());
        List<DocumentReference> documents = new ArrayList<>(response.getDocumentEntryResponses().size());
        for (DocumentEntryResponseType resp : response.getDocumentEntryResponses()) {
            DocumentEntryType entry = resp.getDocumentEntry();
            documents.add(documentEntryToReference(entry));
        }

        return documents;
    }

    @Override
    public Binary getBinaryById(IdType id) {
        DocumentEntryType entry = getDocumentEntryById(id.getIdPart());
        if (entry == null) {
            throw new ResourceNotFoundException(id);
        }

        DocumentRequest request = new DocumentRequest(entry.getRepositoryUniqueId(), retrieveUri, entry.getUniqueId());
        XDSRetrieveResponseType response = comm.retrieveDocument(request);

        if (!isSuccess(response.getStatus())) {
            log.error("Error retrieving document: " + response.getErrorList().getHighestSeverity().getName());
            throw new InternalErrorException("Error retrieving document: " + response.getErrorList().getHighestSeverity().getName());
        }

        try {
            byte[] content = IOUtils.toByteArray(response.getAttachments().get(0).getStream());
            String type = entry.getMimeType();

            Binary binary = new Binary();
            binary.setContent(content);
            binary.setContentType(type);
            return binary;
        } catch (IOException e) {
            log.error("Error reading document stream", e);
            throw new InternalErrorException("Error reading document stream", e);
        }
    }

    @Override
    public void addDocuments(DocumentManifest manifest, List<DocumentReference> references) {
        for (DocumentReference document : references) {
            log.info("Adding document:");
            Attachment attachment = document.getContent().get(0).getAttachment();
            log.info("\tContentType: " + attachment.getContentType());
            DocumentDescriptor descriptor = DocumentDescriptor.MIME_TYPE_MAP.getOrDefault(attachment.getContentType(),
                    new DocumentDescriptor("UNKNOWN", attachment.getContentType()));
            InputStream stream = new ByteArrayInputStream(attachment.getData());
            DocumentMetadata metadata = comm.addDocument(descriptor, stream);


            String pid = document.getSubject().getReference();
            Identificator patientId = stringToPatientId(pid);
            if (patientId == null) {
                log.info("Malformed Patiend ID " + pid);
                throw new InvalidRequestException("Malformed Patiend ID " + pid);
            }

            log.info("\tPatient: " + patientId.toString());

            metadata.setDestinationPatientId(patientId);
            metadata.setSourcePatientId(patientId); // TODO: set source or destination or both?

            log.info("\tLanguage: " + attachment.getLanguage());
            metadata.setCodedLanguage(attachment.getLanguage());
            metadata.setCreationTime(document.getIndexed());


            // TODO: Get all this data from supplied DocumentReference
            metadata.setTypeCode(new Code("2.16.840.1.113883.6.1", "34133-9", "Summarization of Episode Note"));
            metadata.setFormatCode(new Code("1.3.6.1.4.1.19376.1.2.3", "urn:ihe:iti:xds-sd:pdf:2008","1.3.6.1.4.1.19376.1.2.20 (Scanned Document)"));
            metadata.setClassCode(new Code("1.3.6.1.4.1.21367.100.1", "DEMO-Consult", "Consultation"));
            metadata.setHealthcareFacilityTypeCode(new Code("2.16.840.1.113883.5.11", "AMB", "Ambulance"));
            metadata.setPracticeSettingCode(new Code("2.16.840.1.113883.6.96", "394802001", "General Medicine"));
            metadata.addConfidentialityCode(Confidentiality.NORMAL);
            // TODO: availabilityStatus?

            metadata.setTitle(document.getDescription());
        }

        SubmissionSetMetadata submissionSetMetadata = new SubmissionSetMetadata();

        if (manifest.getStatus() == Enumerations.DocumentReferenceStatus.CURRENT)
            submissionSetMetadata.setAvailabilityStatus(AvailabilityStatusType.APPROVED_LITERAL);
        else if (manifest.getStatus() == Enumerations.DocumentReferenceStatus.SUPERSEDED)
            submissionSetMetadata.setAvailabilityStatus(AvailabilityStatusType.DEPRECATED_LITERAL);


        String pid = manifest.getSubject().getReference();
        Identificator patientId = stringToPatientId(pid);
        if (patientId == null) {
            log.info("Malformed Patiend ID " + pid);
            throw new InvalidRequestException("Malformed Patiend ID " + pid);
        }
        submissionSetMetadata.setDestinationPatientId(patientId);

        // Commented out because it throws this error:
        // Input did not validate against schema:
        // Error: cvc-complex-type.4: Attribute 'value' must appear on element 'rim:LocalizedString'.
        //submissionSetMetadata.setTitle(manifest.getDescription());

        submissionSetMetadata.setSourceId(manifest.getSource());

        // TODO:
        // - submissionSetMetadata.setContentTypeCode(); from: manifest.getType()
        // - submissionSetMetadata.setComments(); from: manifest.getText() ???
        // - submissionSetMetadata.setAuthor(); from manifest.getAuthor();

        try {
            XDSResponseType result = comm.submit(submissionSetMetadata);
            if (!isSuccess(result.getStatus())) {
                String error = result.getErrorList().getHighestSeverity().getName();
                throw new Exception(error);
            }
            comm.clearDocuments();
        } catch (Exception e) {
            comm.clearDocuments();
            log.error("Error submitting document", e);
            throw new InternalErrorException("Error submitting document", e);
        }
    }

    private boolean isSuccess(XDSStatusType status) {
        return status != null && XDSStatusType.SUCCESS == status.getValue();
    }

    private Identificator stringToPatientId(String id) {
        Matcher m = PATIENT_ID_REGEX.matcher(id);
        if (!m.matches())
            return null;

        String pid = m.group(1);
        String oid = m.group(2);

        return new Identificator(oid, pid);
    }


    private DocumentEntryType getDocumentEntryById(String id) {
        GetDocumentsQuery query = new GetDocumentsQuery(new String[]{id}, true);
        XDSQueryResponseType result = comm.queryDocuments(query);
        if (result == null) {
            log.error("Error retrieving document");
            throw new InternalErrorException("Error retrieving document");
        }

        return !isSuccess(result.getStatus()) || result.getDocumentEntryResponses().size() == 0
                ? null
                : result.getDocumentEntryResponses().get(0).getDocumentEntry();
    }

    private DocumentReference documentEntryToReference(DocumentEntryType entry) {
        if (entry == null)
            return null;

        DocumentReference document = new DocumentReference();
        document.setId(entry.getEntryUUID());

        String patientId = String.format(PATIENT_ID_FORMAT,
                entry.getPatientId().getIdNumber(),
                entry.getPatientId().getAssigningAuthorityUniversalId(),
                entry.getPatientId().getAssigningAuthorityUniversalIdType());
        try {
            patientId = URLEncoder.encode(patientId, "UTF-8");
            document.setSubject(new Reference(new IdType("Patient", patientId)));
        } catch (UnsupportedEncodingException e) {
            // Shouldn't happen
            e.printStackTrace();
        }


        try {
            document.setIndexed(DATE_FORMAT.parse(entry.getCreationTime()));
        } catch (ParseException e) {
            log.error("Error parsing date", e);
            throw new InternalErrorException("Error parsing date", e);
        }

        if (entry.getAvailabilityStatus().getValue() == AvailabilityStatusType.APPROVED)
            document.setStatus(Enumerations.DocumentReferenceStatus.CURRENT);
        else if (entry.getAvailabilityStatus().getValue() == AvailabilityStatusType.DEPRECATED)
            document.setStatus(Enumerations.DocumentReferenceStatus.SUPERSEDED);
        else
            document.setStatus(Enumerations.DocumentReferenceStatus.NULL);

        if (entry.getTitle().getLocalizedString().size() > 0) {
            LocalizedStringType title = (LocalizedStringType) entry.getTitle().getLocalizedString().get(0);
            document.setDescription(title.getValue());
        }

        // TODO: document.addRelatesTo();
        // TODO: document.setType(entry.getTypeCode()???);
        // TODO: document.setClass_(entry.getClassCode()???);
        // TODO: document.addAuthor().setReference(entry.getAuthors()???);
        // TODO: document.setAuthenticator(entry.getLegalAuthenticator()???);
        // TODO: document.addSecurityLabel()
        // TODO: entry.getConfidentialityCode()

        document.addContent()
                .getAttachment()
                .setContentType(entry.getMimeType())
                .setLanguage(entry.getLanguageCode())
                .setSize(Integer.parseInt(entry.getSize()))
                .setUrl("Binary/" + document.getId());

        return document;
    }
}
