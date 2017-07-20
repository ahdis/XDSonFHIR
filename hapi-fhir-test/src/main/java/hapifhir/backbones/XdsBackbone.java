package hapifhir.backbones;

import ca.uhn.fhir.rest.param.ReferenceParam;
import org.hl7.fhir.dstu3.model.*;

import java.util.List;

public class XdsBackbone implements Backbone {
    @Override
    public DocumentManifest getDocumentManifestById(IdType id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<DocumentManifest> findDocumentManifests(ReferenceParam patient) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DocumentReference getDocumentReferenceById(IdType id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<DocumentReference> findDocumentReferences(ReferenceParam patient) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ListResource getListById(IdType id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ListResource> findLists(ReferenceParam patient) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Binary getBinaryById(IdType id) {
        throw new UnsupportedOperationException();
    }
}
