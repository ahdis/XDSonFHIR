package xdsonfhir.backbones;

import ca.uhn.fhir.rest.param.ReferenceParam;
import org.hl7.fhir.dstu3.model.Binary;
import org.hl7.fhir.dstu3.model.DocumentManifest;
import org.hl7.fhir.dstu3.model.DocumentReference;
import org.hl7.fhir.dstu3.model.IdType;

import java.util.List;

public interface Backbone {
    DocumentReference getDocumentReferenceById(IdType id);

    List<DocumentReference> findDocumentReferencesForPatient(ReferenceParam patient);

    Binary getBinaryById(IdType id);

    void addDocuments(DocumentManifest manifest, List<DocumentReference> references);

    // TODO: add "addDocument" method
    // TODO: add search parameters (MHD 3.66.4.1.2.1 & 3.67.4.1.2.1)
}
