# HAPI-FHIR-TEST

This sample project was used to get familiar with the [HAPI-FHIR Framework](http://hapifhir.io), and served as a starting point for the XdsOnFhir backend server.

In this project, you'll find the base code necessary to have a simple FHIR REST server running (through an embedded Tomcat instance).

The `HapiFhir.runServer()` method (called by `main()`) starts the Tomcat server and registers `HapiFhir` as Servlet.

To get a working REST server, the *HapiFhir* class just has to extend *RestfulServer* (provided by HAPI-FHIR) and register one or more Providers (in this example, only one Provider is provided, in the `Provider` class).

Providers do the heavy lifting, i.e. respond to client requests. HAPI-FHIR automatically maps a Provider's methods to endpoints using annotations and the method's signature. For instance, `Provider.getDocumentManifestById()` is automatically mapped to `GET /DocumentManifest/{id}` because it has a `@Read` annotation and a return type of `DocumentManifest`. The [HAPI-FHIR documentation](http://hapifhir.io/doc_rest_server.html) explains this in more detail.
