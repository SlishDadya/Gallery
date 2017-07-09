package entartaiment.goshan.photogallery2;

import android.net.Uri;

public class GalleryItem {
    private String title;
    private String id;
    private String url_s;
    private String owner;
    public Uri getPhotoPageUri()
    {
        return Uri.parse("http://www.flickr.com/photos/").buildUpon().appendPath(owner).appendPath(id).build();
    }
    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Override
    public String toString()
    {
        return title;
    }

    public String getUrl_s() {
        return url_s;
    }

    public void setUrl_s(String url_s) {
        this.url_s = url_s;
    }

    public String getId() {

        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {

        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
