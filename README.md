# XDSonFHIR
This project exposes a FHIR-compatible REST API (for MHD profile) and translates and forwards requests to an XDS server.

## Architecture
![Architecture](https://raw.githubusercontent.com/ahdis/XDSonFHIR/master/archi.png)
REST request handling and XML/JSON parsing is handled by the HAPI-FHIR library. To set up a
REST server using this library, a Servlet has to be created by extending the *RestfulServer* class (provided by HAPI-FHIR) and one or more Providers have to be registered. In this application, the Servlet is implemented in the *XdsOnFhir* class, which registers only one Provider, simply named Provider. For convenience, *XdsOnFhir* also features a main() method which starts an embedded Tomcat server with this Servlet, thus avoiding the necessity to install and maintain a dedicated Tomcat instance.
HAPI-FHIR translates *Provider’s* methods to API endpoints through the methods’ annotations.
These methods are implemented:
* transaction() → POST /
* getDocumentReferenceById() → GET DocumentReference/{id}
* findDocumentReferencesForPatient() → GET DocumentReference?patient={patientId}
* getBinaryById() → GET Binary/{id}


For modularity, *Provider* doesn’t directly answer request itself. Instead, it has a reference to an instance of *Backbone*, to which method calls are directly delegated, except in the case oftransaction(), where additional preprocessing is performed (see below) before forwarding the call. Two implemetations of *Backbone* exist: *TestBackbone* which saves data to memory (it was intended for testing the Front-End application, but hasn’t been used much in practice) and *XdsBackbone* which uses the eHealthConnector library to forward requests to an XDS server and translate between FHIR *DocumentReferences* and XDS *DocumentEntries*.
The transaction() method in *Provider* performs some basic validity check on the received
transaction Bundle, before calling the *Backbone’s* addDocument() method:
* Bundle must contain exactly one DocumentManifest
* Contained DocumentReferences not referenced by the manifest are discarded
* Contained DocumentReferences without attachment are discarded
* At least one DocumentReference must remain

## How to run

Compile this project:
```bash
cd xds-on-fhir
mvn clean install
```

And run it:
```bash
java -jar target/xds-on-fhir-1.0-SNAPSHOT.jar
```

Check that the server is running by opening http://localhost:8000/fhir/status in your browser.

# Customizing

By default, the embedded Tomcat server starts on port 8000, it can be changed in `XdsOnFhir.java`.

The XDS URLs can be changed in `XdsOnFhir.java` too, in method `initialize()`


## Deploy with Docker
This app can also be deployed in a Docker container with the provided Dockerfile. Docker will build the app inside the container, and then run the embedded Tomcat server on port 8000.

First, the image must be built:
```bash
docker build -t xds-on-fhir .
```

Then, run a container by attaching port 8000 to a port on your local machine, e.g.:
```
docker run --rm -p 8000:8000 xds-on-fhir
```
