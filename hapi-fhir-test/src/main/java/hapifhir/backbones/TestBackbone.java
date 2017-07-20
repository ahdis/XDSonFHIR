package hapifhir.backbones;

import ca.uhn.fhir.rest.param.ReferenceParam;
import org.hl7.fhir.dstu3.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestBackbone implements Backbone {
    private Map<String, DocumentManifest> documentManifests = new HashMap<>();
    private Map<String, DocumentReference> documentReferences = new HashMap<>();
    private Map<String, ListResource> lists = new HashMap<>();
    private Map<String, Binary> binaries = new HashMap<>();

    @Override
    public DocumentManifest getDocumentManifestById(IdType id) {
        return documentManifests.getOrDefault(id.getId(), null);
    }

    @Override
    public List<DocumentManifest> findDocumentManifests(ReferenceParam patient) {
        List<DocumentManifest> results = new ArrayList<>();
        for (DocumentManifest i : documentManifests.values())
            if (i.getSubject().getId().equals(patient.getIdPart()))
                results.add(i);

        return results;
    }

    @Override
    public DocumentReference getDocumentReferenceById(IdType id) {
        return documentReferences.getOrDefault(id.getId(), null);
    }

    @Override
    public List<DocumentReference> findDocumentReferences(ReferenceParam patient) {
        List<DocumentReference> results = new ArrayList<>();
        for (DocumentReference i : documentReferences.values())
            if (i.getSubject().getId().equals(patient.getIdPart()))
                results.add(i);

        return results;
    }

    @Override
    public ListResource getListById(IdType id) {
        return lists.getOrDefault(id.getId(), null);
    }

    @Override
    public List<ListResource> findLists(ReferenceParam patient) {
        List<ListResource> results = new ArrayList<>();
        for (ListResource i : lists.values())
            if (i.getSubject().getId().equals(patient.getIdPart()))
                results.add(i);

        return results;
    }

    @Override
    public Binary getBinaryById(IdType id) {
        return binaries.getOrDefault(id.getId(), null);
    }
}
