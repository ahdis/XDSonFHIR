package hapifhir.providers;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.Enumerations;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.instance.model.api.IBaseResource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PatientProvider implements IResourceProvider {
    private Map<Long, Patient> patients;

    public PatientProvider() {
        Patient patient = new Patient();
        patient.setId("0");
        patient.addName().addGiven("Jane").addSuffix("Doe");
        patient.setGender(Enumerations.AdministrativeGender.FEMALE);
        patient.addAddress().setCity("London");

        patients = new HashMap<>();
        patients.put(0L, patient);
    }

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Patient.class;
    }

    @Read()
    public Patient getPatientById(@IdParam IdType id) {
        return patients.getOrDefault(id.getIdPartAsLong(), null);
    }

    @Create
    public MethodOutcome createPatient(@ResourceParam Patient patient) {
        IdType id = new IdType(patients.size());
        patient.setId(id);
        patients.put(id.getIdPartAsLong(), patient);

        MethodOutcome outcome = new MethodOutcome();
        outcome.setId(id);
        return outcome;
    }

    @Update
    public MethodOutcome update(@IdParam IdType id, @ResourceParam Patient patient) {
        patients.put(id.getIdPartAsLong(), patient);
        return new MethodOutcome();
    }

    @Delete()
    public void deletePatient(@IdParam IdType id) {
        // .. Delete the patient ..
        if (!patients.containsKey(id.getIdPartAsLong())) {
            throw new ResourceNotFoundException("Unknown version");
        }

        patients.remove(id.getIdPartAsLong());
    }

    @Search()
    public List<Patient> getAllPatients() {
        List<Patient> list = new ArrayList<>();
        list.addAll(patients.values());
        return list;
    }
}
