# Running the full stack (xds-on-fhir + fhir-webapp + xdstools-docker) with docker-compose

This example shows how to run xds-on-fhir, fhir-webapp, and a dockerized xds-tools instance using [docker-compose](https://docs.docker.com/compose/) on your server with your own domain name.

xds-on-fhir will be available at `server.YOUR-DOMAIN.com`, fhir-webapp at `app.YOUR-DOMAIN.com` and xds-tools at `xdstools.YOUR-DOMAIN.com/xdstools4`. By default, `YOUR-DOMAIN.com` is configured to `127.0.0.1.nip.io` ([nip.io](http://nip.io/) is a service that resolves any domain of the type `anything.IP.AD.DR.ESS.nip.io` to that IP address, it will therefore only work locally). This can be changed by changing the values of `VIRTUAL_HOST` in `docker-compose.yml`.

It uses the [nginx-proxy](https://github.com/jwilder/nginx-proxy) Docker image to automatically set up a reverse proxy for all 3 components. See the project's GitHub repo for more details on advanced configurations like SSL.

Before starting, you'll need to install docker-compose (see [installation instructions](https://docs.docker.com/compose/install/)).

Then, in `xds-on-fhir`, in the file `XdsOnFhir.java`, the repository URLs need to be changed to point to the xds-tools instance. Simply replace `localhost` by `xdstools`.

In `fhir-webapp` you will also need to set the backend URL to xds-on-fhir's URL. Either change the value of `DEFAULT_URL` in `src/providers/fhir/fhir.ts` to `server.YOUR-DOMAIN.com/fhir/` or change it through the "Settings" menu (if you do it this way, it will have to be done by each end-user of the app).

Finally, you can build the Docker images for [fhir-webapp](https://github.com/ahdis/fhir-webapp) and [xds-tools-docker](https://github.com/ahdis/xdstools-docker) (see the respective repos for instructions).

Once all of this is done, run the service like this:
```bash
docker-compose up
```

Test that it is working by opening:
- http://server.127.0.0.1.nip.io/fhir/status 
- http://app.127.0.0.1.nip.io/
- http://xdstools.127.0.0.1.nip.io/xdstools4/

To rebuild the xds-on-fhir image, run `docker-compose build`.
