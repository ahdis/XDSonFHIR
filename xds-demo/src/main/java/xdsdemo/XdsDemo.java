package xdsdemo;

import org.apache.commons.io.IOUtils;
import org.ehealth_connector.common.Code;
import org.ehealth_connector.common.Identificator;
import org.ehealth_connector.common.enums.Confidentiality;
import org.ehealth_connector.common.enums.LanguageCode;
import org.ehealth_connector.communication.*;
import org.ehealth_connector.communication.ch.ConvenienceCommunicationCh;
import org.ehealth_connector.communication.xd.storedquery.FindDocumentsQuery;
import org.ehealth_connector.communication.xd.storedquery.FindFoldersStoredQuery;
import org.ehealth_connector.communication.xd.storedquery.GetDocumentsQuery;
import org.ehealth_connector.communication.xd.storedquery.GetFolderAndContentsQuery;
import org.openhealthtools.ihe.xds.document.DocumentDescriptor;
import org.openhealthtools.ihe.xds.document.XDSDocument;
import org.openhealthtools.ihe.xds.metadata.AvailabilityStatusType;
import org.openhealthtools.ihe.xds.metadata.DocumentEntryType;
import org.openhealthtools.ihe.xds.response.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class XdsDemo {
    public static final String SIM_REPOSITORY = "http://localhost:8888/xdstools4/sim/default__ahdis/rep/prb";
    public static final String SIM_REGISTRY = "http://localhost:8888/xdstools4/sim/default__ahdis/reg/rb";
    public static final String SIM_RETRIEVE = "http://localhost:8888/xdstools4/sim/default__ahdis/rep/ret";

    public static final String PJA_REPOSITORY = "http://ehealthsuisse.ihe-europe.net:8481/xdstools4/sim/default__test/rep/prb";
    public static final String PJA_REGISTRY = "http://ehealthsuisse.ihe-europe.net:8481/xdstools4/sim/default__test/reg/sq";
    public static final String PJA_RETRIEVE = "http://ehealthsuisse.ihe-europe.net:8481/xdstools4/sim/default__test/rep/ret";

    public static final String NIST_REPOSITORY = "http://ihexds.nist.gov:12090/tf6/services/xdsrepositoryb";
    public static final String NIST_REGISTRY = "http://ihexds.nist.gov:12090/tf6/services/xdsregistryb";
    public static final String NIST_RETRIEVE = "http://ihexds.nist.gov:12090/tf6/services/xdsrepositoryb";

    public static final String REPOSITORY = PJA_REPOSITORY;
    public static final String REGISTRY = PJA_REGISTRY;
    public static final String RETRIEVE = PJA_RETRIEVE;

    public static final String ORG_ID = "1.3.6.1.4.1.21367.101";

    private static final DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    private static final Identificator patientId = new Identificator("1.3.6.1.4.1.21367.2005.13.20.1000", "ec193926d6ea4aa");

    public static void main(String[] args) {
        doDemo();
    }

    public static void doDemo() {
        try {
            System.out.println("Test start...");

            // Construct ConvenienceCommunication:
            Destination repository = new Destination(ORG_ID, new URI(REPOSITORY));
            // Destination retrieve = new Destination(ORG_ID, new URI(RETRIEVE));
            Destination registry = new Destination(ORG_ID, new URI(REGISTRY));
            AffinityDomain domain = new AffinityDomain(null, registry, repository);
            ConvenienceCommunicationCh comm = new ConvenienceCommunicationCh(domain);

            //uploadDocument(comm);
            queryDocuments(comm);

            System.out.print(dateFormat.format(new Date()) + ": done\n\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void queryDocuments(ConvenienceCommunicationCh comm) throws URISyntaxException, IOException {
        System.out.println("queryDocuments()");
        // Request folders
        FindFoldersStoredQuery foldersStoredQuery = new FindFoldersStoredQuery(patientId, AvailabilityStatusType.APPROVED_LITERAL);
        XDSQueryResponseType result = comm.queryFolders(foldersStoredQuery);

        System.out.println("Query for folders references. Response status: " + result.getStatus().getName());
        System.out.println("" + result.getFolderResponses().size() + " Folders found for patient " +patientId.getRoot()+"/"+patientId.getExtension());

        if (false) {
            for (FolderResponseType folder : result.getFolderResponses()) {
                if (folder.getFolder() != null) {
                    System.out.println("- Folder UUID: " + folder.getFolder().getEntryUUID());

                    GetFolderAndContentsQuery folderStoredQuery = new GetFolderAndContentsQuery(folder.getFolder().getEntryUUID(), true, null, null);
                    result = comm.queryDocuments(folderStoredQuery);
                    System.out.println("\tQuery for folder. Response status: " + result.getStatus().getName());
                    System.out.println("\t\tHas " + result.getAssociations().size() + " associations");
                    System.out.println("\t\tHas " + result.getDocumentEntryResponses().size() + " document entries");
                }
            }
        }

        // Request all document references
        FindDocumentsQuery findDocumentsQuery = new FindDocumentsQuery(patientId, AvailabilityStatusType.APPROVED_LITERAL);
        result = comm.queryDocumentsReferencesOnly(findDocumentsQuery);

        System.out.println("Query for document references. Response status: " + result.getStatus().getName() + ". Returned " + result.getReferences().size() + " references.");

        // Get UUIDs of the 3 newest documents
        int nDocs = Math.min(3, result.getReferences().size());
        if (nDocs == 0)
            return;

        System.out.println("Getting last " + nDocs + " documents...");

        final String[] docUUIDs = new String[nDocs];
        for (int i = 0; i < nDocs; i++)
            docUUIDs[i] = result.getReferences().get(result.getReferences().size() - 1 - i).getId(); // references are returned from oldest to newest

        // Request those 3 documents
        final GetDocumentsQuery getDocumentsQuery = new GetDocumentsQuery(docUUIDs, true);
        result = comm.queryDocuments(getDocumentsQuery);
        System.out.println("Response status: " + result.getStatus().getName() + ". Returned " + result.getDocumentEntryResponses().size() + " documents.");

        for (DocumentEntryResponseType res : result.getDocumentEntryResponses()) {
            DocumentEntryType doc = res.getDocumentEntry();
            System.out.println("\t- " + doc.getUniqueId());
            System.out.println("\t\tType: " + doc.getMimeType());
            System.out.println("\t\tLanguage: " + doc.getLanguageCode());
            System.out.println("\t\tCreation Time: " + doc.getCreationTime());

            if (doc.getMimeType().startsWith("text/")) {
                DocumentRequest request = new DocumentRequest(
                        doc.getRepositoryUniqueId(),
                        new URI(RETRIEVE),
                        doc.getUniqueId());

                XDSRetrieveResponseType response = comm.retrieveDocument(request);
                if (response.getErrorList() != null) {
                    System.out.println("\t\tErrors: " + response.getErrorList().getHighestSeverity().getName());
                }

                System.out.println("---------------");
                XDSDocument document = response.getAttachments().get(0);
                InputStream docIS = document.getStream();
                IOUtils.copy(docIS, System.out);
                System.out.println("---------------");
                System.out.println();
            }
        }
    }

    public static void uploadDocument(ConvenienceCommunicationCh comm) throws Exception {
        System.out.println("uploadDocument()");

        // Create new folder
        FolderMetadata folderMeta = comm.addFolder(new Code("2.16.840.1.113883.6.1", "34133-9", "Summarization of Episode Note"));

        folderMeta.setAvailabilityStatus(AvailabilityStatusType.APPROVED_LITERAL);
        folderMeta.addCode(new Code("Connect-a-thon folderCodeList", "Referrals",
                "Connect-a-thon folderCodeList", "Referrals"));
        folderMeta.setComments("This is a Folder");
        folderMeta.setPatientId(patientId);
        folderMeta.setTitle("Folder for Patient " + patientId.getExtension());

        // Create new document, with attached XML file
        DocumentMetadata metaData = comm.addDocument(DocumentDescriptor.XML, "test.xml");

        metaData.setDestinationPatientId(patientId);
        metaData.setSourcePatientId(new Identificator("1.2.3.4", "2342134localid"));

        metaData.setCodedLanguage(LanguageCode.GERMAN_CODE);
        metaData.setTypeCode(new Code("2.16.840.1.113883.6.1", "34133-9", "Summarization of Episode Note"));
        metaData.setFormatCode(new Code("1.3.6.1.4.1.19376.1.2.3", "urn:ihe:iti:xds-sd:pdf:2008","1.3.6.1.4.1.19376.1.2.20 (Scanned Document)"));

        metaData.setClassCode(new Code("1.3.6.1.4.1.21367.100.1", "DEMO-Consult", "Consultation"));

        metaData.setHealthcareFacilityTypeCode(new Code("2.16.840.1.113883.5.11", "AMB", "Ambulance"));
        metaData.setPracticeSettingCode(new Code("2.16.840.1.113883.6.96", "394802001", "General Medicine"));
        metaData.addConfidentialityCode(Confidentiality.NORMAL);
        metaData.setTitle("Informed Consent");

        // Put this document in the folder
        comm.addDocumentToFolder(metaData.getEntryUUID(), folderMeta.getEntryUUID());

        XDSResponseType result = comm.submit();
        printXdsResponse(result);
    }

    private static void printXdsResponse(XDSResponseType aResponse) {
        if (XDSStatusType.SUCCESS_LITERAL.equals(aResponse.getStatus())) {
            System.out.print("done. Response: " + aResponse.getStatus().getName() + "\n\n");
        } else if (XDSStatusType.ERROR_LITERAL.equals(aResponse.getStatus())
                || XDSStatusType.FAILURE_LITERAL.equals(aResponse.getStatus())
                || XDSStatusType.PARTIAL_SUCCESS_LITERAL.equals(aResponse.getStatus())
                || XDSStatusType.UNAVAILABLE_LITERAL.equals(aResponse.getStatus())
                || XDSStatusType.WARNING_LITERAL.equals(aResponse.getStatus())) {
            System.out.print("done. Response: " + aResponse.getStatus().getName() + "\n");
            if ((aResponse.getErrorList() != null)
                    && (aResponse.getErrorList().getError() != null)) {
                for (final XDSErrorType error : aResponse.getErrorList().getError()) {
                    System.out.print("      Context:  " + error.getCodeContext() + "\n");
                    System.out.print("      Location: " + error.getLocation() + "\n");
                    System.out.print("      Value:    " + error.getValue() + "\n");
                    System.out.print("\n");
                }
            }
            System.out.print("\n\n");
        }

    }
}