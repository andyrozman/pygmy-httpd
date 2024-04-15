package pygmy.handlers.jython;

import lombok.extern.slf4j.Slf4j;
import org.python.core.PyException;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;
import pygmy.core.*;

import java.io.IOException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.util.Properties;
import java.util.logging.Level;

@Slf4j
public class JythonHandler extends AbstractHandler {

    private static final ConfigOption SCRIPT_DIRECTORY_OPTION = new ConfigOption("script-dir", true, "Location of the scripts you want to run.");


    String pythonDir;
    PythonInterpreter interpreter;
    private static final ConfigOption PYTHON_HOME = new ConfigOption("python.home", true, "Home of the jython interpreter.");
    private static final ConfigOption PYTHON_PATH = new ConfigOption("python.path", true, "Path used to resolve jython libaries.");

    public boolean initialize(String handlerName, Server server) {
        super.initialize(handlerName, server);
        pythonDir = SCRIPT_DIRECTORY_OPTION.getProperty(server, handlerName);

        Properties props = new Properties();

        putPythonProperty(PYTHON_HOME, props);
        putPythonProperty(PYTHON_PATH, props);
        PythonInterpreter.initialize(System.getProperties(), props, new String[0]);

        interpreter = new PythonInterpreter(null, new PySystemState());
        interpreter.setErr(new LogWriter(Level.SEVERE));
        interpreter.setOut(new LogWriter(Level.INFO));
//        PySystemState sys = Py.getSystemState();
//        sys.path.append(new PyString(rootPath));
        return true;
    }

    private void putPythonProperty(ConfigOption option, Properties props) {
        if (System.getProperty(option.getName()) == null) {
            String pythonHome = option.getProperty(server, handlerName);
            if (pythonHome != null)
                props.put(option.getName(), pythonHome);
        }
    }

    protected boolean handleBody(HttpRequest request, HttpResponse response) throws IOException {
        if (request.getUrl().endsWith(".py")) {
            try {
                if (log.isInfoEnabled()) {
                    log.info("Executing script: " + request.getUrl());
                }
                interpreter.set("request", request);
                interpreter.set("response", response);
                interpreter.execfile(Http.translatePath(pythonDir, request.getUrl()).getAbsolutePath());
            } catch (PyException e) {
                log.error(e.getMessage(), e);
                response.sendError(HttpURLConnection.HTTP_INTERNAL_ERROR, "Script error", e);
            }
            return true;
        }
        return false;
    }

    public class LogWriter extends Writer {
        Level level;
        StringBuffer buf = new StringBuffer();

        public LogWriter(Level level) {
            this.level = level;
        }

        public synchronized void write(char cbuf[], int off, int len) throws IOException {
            buf.append(cbuf, off, len);
        }

        public synchronized void flush() throws IOException {
            switch (level.getName()) {
                case "SEVERE":
                    log.error(buf.toString());
                    break;
                case "WARNING":
                    log.warn(buf.toString());
                    break;
                case "INFO":
                    log.warn(buf.toString());
                    break;
                case "FINE":
                case "FINER":
                case "FINEST":
                    log.debug(buf.toString());
                    break;
                default:
                    log.debug(buf.toString());
            }
            buf.delete(0, buf.length() - 1);
        }

        public void close() throws IOException {
            flush();
        }
    }
}
