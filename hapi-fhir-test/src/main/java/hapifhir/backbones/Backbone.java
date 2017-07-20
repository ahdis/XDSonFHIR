package hapifhir.backbones;

import ca.uhn.fhir.rest.param.ReferenceParam;
import org.hl7.fhir.dstu3.model.*;

import java.util.List;

public interface Backbone {
    DocumentManifest getDocumentManifestById(IdType id);

    List<DocumentManifest> findDocumentManifests(ReferenceParam patient);

    DocumentReference getDocumentReferenceById(IdType id);

    List<DocumentReference> findDocumentReferences(ReferenceParam patient);

    ListResource getListById(IdType id);

    List<ListResource> findLists(ReferenceParam patient);

    Binary getBinaryById(IdType id);

    // TODO: add "addDocument" method
    // TODO: add search parameters (MHD 3.66.4.1.2.1 & 3.67.4.1.2.1)
}
