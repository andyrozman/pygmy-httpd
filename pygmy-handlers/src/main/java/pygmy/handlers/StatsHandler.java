package pygmy.handlers;

import lombok.extern.slf4j.Slf4j;
import pygmy.core.*;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Slf4j
public class StatsHandler extends AbstractHandler {

    private File filename;
    private Handler delegate;
    private Map statsMap = Collections.synchronizedMap(new HashMap());

    public StatsHandler(File filename, Handler delegate) {
        this.filename = filename;
        this.delegate = delegate;
    }

    protected boolean handleBody(HttpRequest request, HttpResponse response) throws IOException {
        UrlStatistics stats = (UrlStatistics) statsMap.get(request.getUrl());
        if (stats == null) {
            stats = new UrlStatistics(System.currentTimeMillis());
            statsMap.put(request.getUrl(), stats);
        } else {
            stats.lastTime = System.currentTimeMillis();
        }
        stats.increment();
        saveStatistics();
        return delegate.handle(request, response);
    }

    private void saveStatistics() throws IOException {
        Writer file = new BufferedWriter(new FileWriter(filename));
        try {
            for (Iterator i = statsMap.keySet().iterator(); i.hasNext(); ) {
                String url = (String) i.next();
                UrlStatistics stats = (UrlStatistics) statsMap.get(url);
                synchronized (stats) {
                    file.write(url + " " + stats.count() + " " + stats.getFirstTime() + " " + stats.getLastTime() + "\n");
                }
            }
        } finally {
            file.close();
        }
    }

    public boolean shutdown(Server server) {
        try {
            super.shutdown(server);
            saveStatistics();
            return true;
        } catch (IOException e) {
            log.warn("IOException: {}", e.getMessage());
            return false;
        }
    }

    public static class UrlStatistics {
        private long lastTime;
        private int count;
        private long firstTime;

        public UrlStatistics(long currentTime) {
            this.firstTime = currentTime;
            this.lastTime = currentTime;
        }

        public synchronized void lastTime(long currentTime) {
            lastTime = currentTime;
        }

        public synchronized void increment() {
            count++;
        }

        public synchronized long getFirstTime() {
            return firstTime;
        }

        public synchronized long getLastTime() {
            return lastTime;
        }

        public synchronized int count() {
            return count;
        }
    }
}
