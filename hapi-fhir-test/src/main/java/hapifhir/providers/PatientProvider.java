package hapifhir.providers;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.dstu3.model.Enumerations;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.instance.model.api.IBaseResource;

import java.util.ArrayList;
import java.util.List;

public class PatientProvider implements IResourceProvider {
    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Patient.class;
    }

    @Read()
    public Patient getPatientById(@IdParam IdType id) {
        Patient patient = null;

        if (id.getIdPartAsLong() == 1) {
            patient = new Patient();
            patient.setId("1");
            patient.addIdentifier();
            patient.getIdentifier().get(0).setSystem("urn:hapitest:mrns");
            patient.getIdentifier().get(0).setValue("00001");
            patient.addName().addGiven("John").addSuffix("Doe");
            patient.setGender(Enumerations.AdministrativeGender.MALE);
            patient.addAddress().setCity("Zürich");
        } else if (id.getIdPartAsLong() == 2) {
            patient = new Patient();
            patient.setId("2");
            patient.addIdentifier();
            patient.getIdentifier().get(0).setSystem("urn:hapitest:mrns");
            patient.getIdentifier().get(0).setValue("00002");
            patient.addName().addGiven("Jane").addSuffix("Doe");
            patient.setGender(Enumerations.AdministrativeGender.FEMALE);
            patient.addAddress().setCity("London");
        }

        return patient;
    }

    @Search()
    public List<Patient> getAllPatients() {
        List<Patient> patients = new ArrayList<>();
        for (int i = 1; i <= 2; i++)
            patients.add(getPatientById(new IdType(i)));
        return patients;
    }
}
