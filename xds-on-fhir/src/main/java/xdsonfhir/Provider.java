package xdsonfhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.client.IGenericClient;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.NotImplementedOperationException;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import xdsonfhir.backbones.Backbone;

import java.util.ArrayList;
import java.util.List;

public class Provider {
    private Backbone backbone;

    public Provider(Backbone backbone) {
        this.backbone = backbone;
    }

    @Read
    public DocumentReference getDocumentReferenceById(@IdParam IdType id) {
        return backbone.getDocumentReferenceById(id);
    }

    // TODO: add more search parameters (see MHD 3.67.4.1.2.1)
    @Search(type = DocumentReference.class)
    public List<DocumentReference> findDocumentReferencesForPatient(@RequiredParam(name = DocumentReference.SP_PATIENT) ReferenceParam patient) {
        return backbone.findDocumentReferencesForPatient(patient);
    }

    @Read
    public Binary getBinaryById(@IdParam IdType id) {
        return backbone.getBinaryById(id);
    }

    @Transaction
    public Bundle transaction(@TransactionParam Bundle bundle) throws FHIRException {
        if (!Bundle.BundleType.TRANSACTION.equals(bundle.getType()))
            throw new NotImplementedOperationException("Only Document transactions are supported");

        /* Conditions:
        - Must contain exactly one DocumentManifest
        - Must contain at least one DocumentReference (referenced by the Manifest)
        - Each DocumentReference must contain an attachment
         */
        System.out.println("Received Bundle:");
        printXml(bundle);
        System.out.println("===========");
        DocumentManifest manifest = null;
        List<DocumentReference> allReferences = new ArrayList<>();
        for (Bundle.BundleEntryComponent entry: bundle.getEntry()) {

            if (entry.getResource().getResourceType().equals(ResourceType.DocumentManifest)) {
                if (manifest != null)
                    throw new InvalidRequestException("Transaction must contain only one DocumentManifest");

                manifest = (DocumentManifest) entry.getResource();
            }

            if (entry.getResource().getResourceType().equals(ResourceType.DocumentReference))
                allReferences.add((DocumentReference) entry.getResource());
        }

        if (manifest == null)
            throw new InvalidRequestException("Transaction must contain a DocumentManifest");

        List<DocumentReference> referencedReferences = new ArrayList<>();
        for (DocumentManifest.DocumentManifestContentComponent content : manifest.getContent()) {
            String ref = content.getPReference().getReference();
            for (DocumentReference reference : allReferences) {
                if (reference.getId().equals(ref)) {
                    referencedReferences.add(reference);
                    break;
                }
            }
        }

        if (referencedReferences.size() == 0)
            throw new InvalidRequestException("Transaction must contain at least one DocumentReference referenced by the DocumentManifest");

        List<DocumentReference> references = new ArrayList<>();
        for (DocumentReference reference : referencedReferences) {
            if (reference.getContent().size() == 0)
                break;

            if (reference.getContent().get(0).getAttachment() == null)
                break;

            if (reference.getContent().get(0).getAttachment().getData() == null)
                break;

            if (reference.getContent().get(0).getAttachment().getData().length > 0)
                references.add(reference);
        }

        if (references.size() == 0)
            throw new InvalidRequestException("Transaction must contain at least one DocumentReference with attachment");

        backbone.addDocuments(manifest, references);
        return new Bundle(); // TODO: return "result" bundle
    }

    private final FhirContext ctx = FhirContext.forDstu3();
    private final IParser xmlParser = ctx.newXmlParser().setPrettyPrint(true);

    private String toXml(IBaseResource resource) throws DataFormatException {
        return xmlParser.encodeResourceToString(resource);
    }

    private void printXml(IBaseResource resource) {
        System.out.println(toXml(resource));
    }
}
