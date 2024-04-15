package pygmy.nntp;

import java.io.IOException;

public interface ArticleSpool {
    public void addArticle( Article article, String host ) throws IOException;
    public void setForum( Forum aForum );
}
