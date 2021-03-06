package entartaiment.goshan.photogallery2;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.util.LruCache;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class ThumbnailDownloader<T> extends HandlerThread {
    private static final String TAG="ThumbnailDownloader";
    private static final int MESSAGE_DOWNLOAD = 0;

    private Handler mRequestHandler;
    private Handler mResponseHandler;
    private ConcurrentMap<T,String> mRequestMap=new ConcurrentHashMap<T, String>();
    private ThumbnailDownloadListener<T> mThumbnailDownloadListener;

    private MyLruCache cache=new MyLruCache(1024*1024*8*10);


    public interface ThumbnailDownloadListener<T>
    {
        void onThumbnailDownload(T target,Bitmap thumbnail);
    }
    public void setThumbnailDownloadListener(ThumbnailDownloadListener<T> listener)
    {
        mThumbnailDownloadListener = listener;
    }
    @Override
    protected void onLooperPrepared()
    {
        mRequestHandler=new Handler()
        {
          @Override
            public void handleMessage(Message msg)
          {
              if (msg.what==MESSAGE_DOWNLOAD)
              {
                  T target=(T) msg.obj;
                  Log.i(TAG," Got a request for URL: "+ mRequestMap.get(target));

                  handleRequest(target);

              }
          }
   };
    }

    public ThumbnailDownloader(Handler responseHandler)
    {
        super(TAG);
        mResponseHandler=responseHandler;
    }




        public void queueThumbnail(T target, String url)
        {
            Log.i(TAG,"Got a URL: " + url);

            if (url==null) mRequestMap.remove(target);
            else
            {
                mRequestMap.put(target,url);
                mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD,target).sendToTarget();

        }
    }
    public void clearQueue()
    {
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
    }
    private void handleRequest(final T target)
    {
        try{
            final String url=mRequestMap.get(target);
            if (url == null) {
                return;
            }
            if(cache.get(url)!=null)
            {
                Log.i(TAG,"Bitmap cached");
                mResponseHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mRequestMap.get(target)!= url) return;

                        mRequestMap.remove(target);
                        mThumbnailDownloadListener.onThumbnailDownload(target,cache.get(url));
                    }
                });
                return;
            }
            byte[] bitmapBytes=new FlickrFetchr().getURLbytes(url);
            final Bitmap bitmap= BitmapFactory.decodeByteArray(bitmapBytes,0,bitmapBytes.length);
            Log.i(TAG,"Bitmap created");
            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mRequestMap.get(target)!= url) return;
                    cache.put(url,bitmap);
                    mRequestMap.remove(target);
                    mThumbnailDownloadListener.onThumbnailDownload(target,bitmap);
                }
            });
        }
        catch(IOException e)
        {
            Log.e(TAG," Error downloading image" , e);
        }
    }
    private static class MyLruCache extends LruCache<String,Bitmap>
    {
        public MyLruCache(int MaxSize)
        {
            super(MaxSize);
        }
        protected int sizeOf(String key, Bitmap value) {
            return value.getByteCount();
        }
    }
}
