package xdsonfhir;

import ca.uhn.fhir.rest.server.interceptor.InterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class StatusInterceptor extends InterceptorAdapter {
    @Override
    public boolean incomingRequestPreProcessed(HttpServletRequest theRequest, HttpServletResponse theResponse) {
        if (!theRequest.getPathInfo().equalsIgnoreCase("/status"))
            return true;

        theResponse.setStatus(200);
        theResponse.setContentType("application/json");
        try {
            theResponse.getOutputStream().println("{ \n" +
                    "  \"status\": \"OK\",\n" +
                    "  \"message\": \"XdsOnFhir is running\"\n" +
                    "}");
        } catch (IOException e) {
            // ignore
        }

        return false;
    }
}