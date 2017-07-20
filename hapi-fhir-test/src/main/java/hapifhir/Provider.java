package hapifhir;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.server.exceptions.NotImplementedOperationException;
import hapifhir.backbones.Backbone;
import org.hl7.fhir.dstu3.model.*;

import java.util.List;

public class Provider {
    private Backbone backbone;

    public Provider(Backbone backbone) {
        this.backbone = backbone;
    }

    @Read
    public DocumentManifest getDocumentManifestById(@IdParam IdType id) {
        return backbone.getDocumentManifestById(id);
    }

    // TODO: add more search parameters (see MHD 3.66.4.1.2.1)
    @Search
    public List<DocumentManifest> findDocumentManifests(@OptionalParam(name = DocumentManifest.SP_PATIENT) ReferenceParam patient) {
        return backbone.findDocumentManifests(patient);
    }

    @Read
    public DocumentReference getDocumentReferenceById(@IdParam IdType id) {
        return backbone.getDocumentReferenceById(id);
    }

    // TODO: add more search parameters (see MHD 3.67.4.1.2.1)
    @Search(type = DocumentReference.class)
    public List<DocumentReference> findDocumentReferences(@OptionalParam(name = DocumentReference.SP_PATIENT) ReferenceParam patient) {
        return backbone.findDocumentReferences(patient);
    }

    // TODO: are lists needed?
    @Read
    public ListResource getListById(@IdParam IdType id) {
        return backbone.getListById(id);
    }

    @Search(type = ListResource.class)
    public List<ListResource> findLists(@RequiredParam(name = ListResource.SP_PATIENT) ReferenceParam patient) {
        return backbone.findLists(patient);
    }

    @Read
    public Binary getBinaryById(@IdParam IdType id) {
        return backbone.getBinaryById(id);
    }

    @Transaction
    public Bundle transaction(@TransactionParam Bundle bundle) {
        if (!Bundle.BundleType.TRANSACTION.equals(bundle.getType()))
            throw new NotImplementedOperationException("Only Document transactions are supported");

        // TODO: validate bundle content
        // TODO: delegate resource creation to Backbone
        return null;
    }
}
