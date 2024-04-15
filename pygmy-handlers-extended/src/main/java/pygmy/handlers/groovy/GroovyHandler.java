package pygmy.handlers.groovy;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import lombok.extern.slf4j.Slf4j;
import pygmy.core.*;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;

@Slf4j
public class GroovyHandler extends AbstractHandler {

    public static final ConfigOption SCRIPT_DIRECTORY_OPTION = new ConfigOption("script-dir", true, "The directory where scripts are located.");

    String groovyDir;

    public boolean initialize(String handlerName, Server server) {
        super.initialize(handlerName, server);
        groovyDir = SCRIPT_DIRECTORY_OPTION.getProperty(server, handlerName);
        return true;
    }

    protected boolean handleBody(HttpRequest request, HttpResponse response) throws IOException {
        if (request.getUrl().endsWith(".groovy")) {
            if (log.isInfoEnabled()) {
                log.info("Executing script: " + request.getUrl());
            }
            Binding binding = createScriptContext(request, response);

            try {
                GroovyShell shell = new GroovyShell(binding);
                File groovyScript = Http.translatePath(groovyDir, request.getUrl());
                if (groovyScript.exists()) {
                    shell.evaluate(groovyScript.getAbsolutePath());
                } else {
                    response.sendError(HttpURLConnection.HTTP_NOT_FOUND, request.getUrl() + " not found.");
                }
            }
//            catch (ClassNotFoundException e) {
//                log.error( e.getMessage(), e );
//                response.sendError( HttpURLConnection.HTTP_INTERNAL_ERROR, "Script error", e);
//            }
//            catch (SyntaxException e) {
//                log.error( e.getMessage(), e );
//                response.sendError( HttpURLConnection.HTTP_INTERNAL_ERROR, "Script error", e);
//            }
            catch (Exception e) {
                log.error(e.getMessage(), e);
                response.sendError(HttpURLConnection.HTTP_INTERNAL_ERROR, "Script error", e);
            }
            return true;
        }
        return false;
    }

    private Binding createScriptContext(HttpRequest request, HttpResponse response) {
        // Set up the script context
        Binding binding = new Binding();
        binding.setVariable("request", request);
        binding.setVariable("response", response);
        return binding;
    }
}
