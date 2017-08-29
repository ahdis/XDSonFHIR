package xdsonfhir;

import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.CorsInterceptor;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.springframework.web.cors.CorsConfiguration;
import xdsonfhir.backbones.Backbone;
import xdsonfhir.backbones.XdsBackbone;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

@WebServlet(urlPatterns = {"/fhir/*"}, displayName = "FHIR Server")
public class XdsOnFhir extends RestfulServer {
    public static void main(String[] args) {
        try {
            runServer();
        } catch (LifecycleException e) {
            e.printStackTrace();
        }
    }

    public static void runServer() throws LifecycleException {
        Tomcat tomcat = getTomcat();
        tomcat.start();
        tomcat.getServer().await();
    }

    public static Tomcat getTomcat() throws LifecycleException {
        return getTomcat(8000);
    }

    public static Tomcat getTomcat(int port) throws LifecycleException {
        // Start an embedded Tomcat server
        // From https://github.com/iSma/tomcat-in-the-cloud

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);

        // Servlet Configuration:
        File base = new File(System.getProperty("java.io.tmpdir"));
        tomcat.setBaseDir(base.getAbsolutePath());
        Context ctx = tomcat.addContext("", base.getAbsolutePath());

        Tomcat.addServlet(ctx, "XdsOnFhir", new XdsOnFhir());
        ctx.addServletMappingDecoded("/fhir/*", "XdsOnFhir");

        return tomcat;
    }

    @Override
    protected void initialize() throws ServletException {
        // Allow Cross-Origin requests
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedHeader("x-fhir-starter");
        config.addAllowedHeader("Origin");
        config.addAllowedHeader("Accept");
        config.addAllowedHeader("X-Requested-With");
        config.addAllowedHeader("Content-Type");

        config.addAllowedOrigin("*");

        config.addExposedHeader("Location");
        config.addExposedHeader("Content-Location");
        config.setAllowedMethods(Arrays.asList("GET", "POST"));

        CorsInterceptor corsInterceptor = new CorsInterceptor(config);
        registerInterceptor(corsInterceptor);

        StatusInterceptor statusInterceptor = new StatusInterceptor();
        registerInterceptor(statusInterceptor);

        try {
            Backbone backbone = new XdsBackbone(
                    new URI("http://localhost:8888/xdstools4/sim/default__ahdis/rep/prb"),
                    new URI("http://localhost:8888/xdstools4/sim/default__ahdis/reg/sq"),
                    new URI("http://localhost:8888/xdstools4/sim/default__ahdis/reg/ret"),
                    "1.3.6.1.4.1.21367.101");

            Provider provider = new Provider(backbone);
            setPlainProviders(provider);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}