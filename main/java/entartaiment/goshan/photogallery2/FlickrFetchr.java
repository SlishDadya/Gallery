package entartaiment.goshan.photogallery2;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;


public class FlickrFetchr {
    private static final String TAG="FlickrFetchr";
    private static final String api_key="91507cfd3fc44266fadda735eebc5d09";
    public List<GalleryItem> fetchItems(Integer page)
    {

            List<GalleryItem> items = new ArrayList<>();
        try{
            String url = Uri.parse("https://api.flickr.com/services/rest/")
                    .buildUpon()
                    .appendQueryParameter("method", "flickr.photos.getRecent")
                    .appendQueryParameter("api_key", api_key)
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("nojsoncallback", "1")
                    .appendQueryParameter("per_page","250")
                    .appendQueryParameter("extras", "url_s")
                    .appendQueryParameter("page", page.toString()).build().toString();
            String jsonString = getURL(url);
            JSONObject jsonBody = new JSONObject(jsonString);
            parseItems(items, jsonBody);
            Log.i(TAG, " recived JSON " + jsonString);


        }

        catch(JSONException e)
    {
        Log.e(TAG," Failed to parse JSON "+e);
    }
        catch(IOException e)
    {
        Log.e(TAG," failed to fetch items "+e);
    }
        return items;

    }
    public List<GalleryItem> fetchItems()
    {
      List<GalleryItem> items=new ArrayList<>();

        try{
           String url= Uri.parse("https://api.flickr.com/services/rest/")
                    .buildUpon()
                    .appendQueryParameter("method", "flickr.photos.getRecent")
                    .appendQueryParameter("api_key",api_key)
                    .appendQueryParameter("format","json")
                    .appendQueryParameter("nojsoncallback","1")
                    .appendQueryParameter("extras","url_s").build().toString();


            String jsonString=getURL(url);
        JSONObject jsonBody = new JSONObject(jsonString);
        parseItems(items,jsonBody);
        Log.i(TAG, " recived JSON "+jsonString);
    }
        catch(JSONException e)
    {
        Log.e(TAG," Failed to parse JSON "+e);
    }
        catch(IOException e)
    {
        Log.e(TAG," failed to fetch items "+e);
    }
        return items;
    }
    private  void parseItems(List<GalleryItem> items,JSONObject jsonBody) throws IOException,JSONException {
        JSONObject photosJsonObject = jsonBody.getJSONObject("photos");
        JSONArray photoJsonArray = photosJsonObject.getJSONArray("photo");

        Gson gson=new Gson();
        for(int i=0;i<photoJsonArray.length();i++)
        {
            items.add(gson.fromJson(photoJsonArray.get(i).toString(),GalleryItem.class));
        }


    }
    public byte[] getURLbytes(String urlspec) throws IOException
    {
        URL url=new URL(urlspec);
        HttpURLConnection connection=(HttpURLConnection)url.openConnection();

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if(connection.getResponseCode()!= HttpURLConnection.HTTP_OK)
            {
                throw new IOException(connection.getResponseMessage()+": with"+ urlspec);
            }
            int bytesRead=0;
            byte[] buffer=new byte[1024];
            while((bytesRead=in.read(buffer))>0)
            {
                outputStream.write(buffer,0,bytesRead);
            }
            outputStream.close();
            return outputStream.toByteArray();


        }
        finally {
            connection.disconnect();
        }
    }

    public String getURL(String urlspec) throws IOException
    {
        return  new String(getURLbytes(urlspec));
    }

}

