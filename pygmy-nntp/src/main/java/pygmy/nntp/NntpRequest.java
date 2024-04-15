package pygmy.nntp;

import pygmy.core.Request;

import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;
import java.util.Properties;
import java.net.Socket;

public class NntpRequest extends Request {
    NntpInputStream stream;
    String command;
    String[] parameters;

    public NntpRequest( Socket connection, Properties serverConfig, InputStream stream ) throws IOException {
        super(connection, serverConfig );
        this.stream = new NntpInputStream( stream );
    }

    public String nextCommand() throws IOException {
        String line = stream.readline();

        StringTokenizer tokenizer = new StringTokenizer( line, " \t" );
        command = tokenizer.nextToken();
        parameters = new String[ tokenizer.countTokens() ];
        for( int i = 0; i < parameters.length; i++ ) {
            parameters[i] = tokenizer.nextToken();
        }

        return command;
    }

    public String getCommand() {
        return command;
    }

    public String getParameter( int index ) {
        if( index < 0 || index >= parameterLength() ) {
            return null;
        } else {
            return parameters[index];
        }
    }

    public NntpInputStream getInput() {
        return stream;
    }

    public boolean isDone() {
        return (command != null && command.equalsIgnoreCase("quit"));
    }

    public String getCurrentNewsgroup() throws NoCurrentNewsgroupException {
        String group = getProperty("newsgroup",null);
        if( group == null ) throw new NoCurrentNewsgroupException();
        return group;
    }

    public String getCurrentArticle() throws NoCurrentArticleException {
        String group = getProperty("currentArticle",null);
        if( group == null ) throw new NoCurrentArticleException();
        return group;
    }

    public void setCurrentNewsgroup( String newsgroup ) {
        putProperty("newsgroup", newsgroup);
    }

    public void setCurrentArticle( String articlePointer ) {
        putProperty( "currentArticle", articlePointer );
    }

    public int parameterLength() {
        return parameters.length;
    }
}
