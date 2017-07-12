package hapifhir;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;

import java.io.File;

public class HapiFhir {
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
        Context ctx = tomcat.addContext("", base.getAbsolutePath());

        Tomcat.addServlet(ctx, "TestServlet", new TestServlet());
        ctx.addServletMappingDecoded("/fhir/*", "TestServlet");

        // Start Tomcat
        tomcat.start();
        tomcat.getServer().await();
    }
}
