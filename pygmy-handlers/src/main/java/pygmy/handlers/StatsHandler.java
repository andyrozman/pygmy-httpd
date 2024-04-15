package pygmy.handlers;

import pygmy.core.*;

import java.io.*;
import java.util.HashMap;
import java.util.Collections;
import java.util.Map;
import java.util.Iterator;

public class StatsHandler extends AbstractHandler {

    public static final ConfigOption FILE_OPTION = new ConfigOption( "file", "stats.log", "File name for the stats." );

    private String filename;
    private Map statsMap = Collections.synchronizedMap( new HashMap() );

    public boolean initialize(String handlerName, Server server) {
        super.initialize(handlerName, server);
        filename = FILE_OPTION.getProperty( server, getName() );
        return true;
    }

    protected boolean handleBody(HttpRequest request, HttpResponse response) throws IOException {
        UrlStatistics stats = (UrlStatistics)statsMap.get( request.getUrl() );
        if( stats == null ) {
            stats = new UrlStatistics( System.currentTimeMillis() );
            statsMap.put( request.getUrl(), stats );
        } else {
            stats.lastTime = System.currentTimeMillis();
        }
        stats.increment();
        saveStatistics();
        return false;
    }

    private void saveStatistics() throws IOException {
        Writer file = new BufferedWriter( new FileWriter(filename) );
        try {
            for( Iterator i = statsMap.keySet().iterator(); i.hasNext(); ) {
                String url = (String) i.next();
                UrlStatistics stats = (UrlStatistics) statsMap.get(url);
                synchronized( stats ) {
                    file.write( url + " " + stats.count() + " " + stats.getFirstTime() + " " + stats.getLastTime() + "\n" );
                }
            }
        } finally {
            file.close();
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

        public synchronized void lastTime( long currentTime ) {
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
