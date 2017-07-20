package hapifhir;

import ca.uhn.fhir.rest.server.RestfulServer;
import hapifhir.backbones.Backbone;
import hapifhir.backbones.TestBackbone;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import java.io.File;

@WebServlet(urlPatterns = {"/fhir/*"}, displayName = "FHIR Server")
public class HapiFhir extends RestfulServer {
    public static void main(String[] args) {
        try {
            runServer();
        } catch (LifecycleException e) {
            e.printStackTrace();
        }
    }

    private static void runServer() throws LifecycleException {
        // Start an embedded Tomcat server
        // From https://github.com/iSma/tomcat-in-the-cloud

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8080);

        // Servlet Configuration:
        File base = new File(System.getProperty("java.io.tmpdir"));
        tomcat.setBaseDir(base.getAbsolutePath());
        Context ctx = tomcat.addContext("", base.getAbsolutePath());

        Tomcat.addServlet(ctx, "HapiFhir", new HapiFhir());
        ctx.addServletMappingDecoded("/fhir/*", "HapiFhir");

        // Start Tomcat
        tomcat.start();
        tomcat.getServer().await();
    }

    @Override
    protected void initialize() throws ServletException {
        Backbone backbone = new TestBackbone();
        Provider provider = new Provider(backbone);
        setPlainProviders(provider);
    }
}
