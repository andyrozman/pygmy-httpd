package pygmy.nntp;

import java.io.IOException;

public interface NewsSpool {
    public void addArticle( Article article ) throws IOException;
}
