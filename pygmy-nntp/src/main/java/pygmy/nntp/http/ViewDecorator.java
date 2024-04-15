package pygmy.nntp.http;

import pygmy.core.HttpRequest;

import java.io.IOException;

import pygmy.nntp.http.View;

public class ViewDecorator extends View {

    View component;

    public ViewDecorator(String urlPrefix, View component) {
        super(urlPrefix);
        this.component = component;
    }

    public String render(HttpRequest request) throws IOException {
        return component.render( request );
    }
}
