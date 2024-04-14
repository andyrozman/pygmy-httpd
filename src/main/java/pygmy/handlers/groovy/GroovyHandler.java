package pygmy.handlers.groovy;

import lombok.extern.slf4j.Slf4j;
import pygmy.core.*;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.IOException;
import java.io.File;
import java.net.HttpURLConnection;

import groovy.lang.GroovyShell;
import groovy.lang.Binding;
import org.codehaus.groovy.syntax.SyntaxException;

@Slf4j
public class GroovyHandler extends AbstractHandler {
    //private static final Logger log = Logger.getLogger( GroovyHandler.class.getName() );

    private File scriptsDir;

    public GroovyHandler(File scriptsDir) {
        this.scriptsDir = scriptsDir;
    }

    public boolean start(Server server) {
        super.start(server);
        return true;
    }

    protected boolean handleBody(HttpRequest request, HttpResponse response) throws IOException {
        if( request.getUrl().endsWith(".groovy") ) {
            if( log.isInfoEnabled()) {
                log.info("Executing script: " + request.getUrl() );
            }
            Binding binding = createScriptContext(request, response);

            try {
                GroovyShell shell = new GroovyShell( binding );
                File groovyScript = Http.translatePath(scriptsDir, request.getUrl() );
                if( groovyScript.exists() ) {
                    shell.evaluate( groovyScript.getAbsolutePath() );
                } else {
                    response.sendError( HttpURLConnection.HTTP_NOT_FOUND, request.getUrl() + " not found.");
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
            catch (IOException e) {
                log.error( e.getMessage(), e );
                response.sendError( HttpURLConnection.HTTP_INTERNAL_ERROR, "Script error", e);
            }
            catch (Exception e) {
                log.error( e.getMessage(), e );
                response.sendError( HttpURLConnection.HTTP_INTERNAL_ERROR, "Script error", e);
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
