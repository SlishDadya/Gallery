package entartaiment.goshan.photogallery2;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;



public class PhotoGalleryFragment extends Fragment {
    private static final String TAG="PhotoGalleryFragment";
    private RecyclerView mRecyclerView;
    private ThumbnailDownloader<PhotoHolder> mThumbnailDownloader;
    private List<GalleryItem> mItems=new ArrayList<>();
    private Integer page=1;
    public static PhotoGalleryFragment newInstance()
    {
        return new PhotoGalleryFragment();
    }

    private void setupAdapter()
    {

       // if (isAdded()) mRecyclerView.setAdapter(new PhotoAdapter(mItems));
       //if(isAdded()) mRecyclerView.getAdapter().notifyDataSetChanged();
      //  if (isAdded() mRecyclerView.getAdapter().notifyItemInserted(mItems.size());
        mRecyclerView.getAdapter().notifyItemInserted(mItems.size());
        page++;

    }

    private class FetchItemTask extends AsyncTask<Void,Void,List<GalleryItem>>
    {

        @Override
        protected List<GalleryItem> doInBackground(Void... params)
        {
            return new FlickrFetchr().fetchItems(page);


        }
        @Override
        protected void onPostExecute(List<GalleryItem> items)
        {


            Log.i(TAG,"Page: "+page);
            mItems.addAll(items);

            setupAdapter();

        }
    }


    @Override
    public void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);
        setRetainInstance(true);
        FetchItemTask itemTask=new FetchItemTask();
        Handler responseHandler=new Handler();
        mThumbnailDownloader =new ThumbnailDownloader<>(responseHandler);
        mThumbnailDownloader.setThumbnailDownloadListener(new ThumbnailDownloader.ThumbnailDownloadListener<PhotoHolder>() {
            @Override
            public void onThumbnailDownload(PhotoHolder target, Bitmap thumbnail) {
                Drawable drawable=new BitmapDrawable(getResources(),thumbnail);
                target.bindDrawable(drawable);
            }
        });
        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();
        Log.i(TAG,"Background thread started.");

        try {
            itemTask.execute();
        }
        catch(Exception x)
        {
            Log.i(TAG,x.getStackTrace().toString());
        }

    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle bundle)
    {
        View v=layoutInflater.inflate(R.layout.fragment_photo_gallery,container,false);
        mRecyclerView=(RecyclerView) v.findViewById(R.id.fragment_photo_gallery_recycler_view);

        final GridLayoutManager gridLayoutManager;

            gridLayoutManager = new GridLayoutManager(getActivity(), 3);
            mRecyclerView.setLayoutManager(gridLayoutManager);//количество столбцов


            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

                                                  private boolean loading = true;

                                                  private int previousTotal = 0;
                                                  private int visibleThreshold = 100;//чтобы загружалось перед окончанием страницы

                                                  @Override
                                                  public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                                                      super.onScrolled(recyclerView, dx, dy);

                                                      int visibleItemCount = mRecyclerView.getChildCount();
                                                      int totalItemCount = gridLayoutManager.getItemCount();
                                                      int firstVisibleItem = gridLayoutManager.findFirstVisibleItemPosition();


                                                      if (loading)

                                                      {
                                                          if (totalItemCount > previousTotal) {
                                                              loading = false;
                                                              previousTotal = totalItemCount;
                                                          }
                                                      }
                                                      if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold))

                                                      {

                                                          FetchItemTask fetchItemTask = new FetchItemTask();
                                                          fetchItemTask.execute();

                                                          loading = true;
                                                      }
                                                  }
                                              }

            );








        mRecyclerView.setAdapter(new PhotoAdapter(mItems));
        return v;
    }
    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        mThumbnailDownloader.clearQueue();
    }
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        mThumbnailDownloader.quit();
        Log.i(TAG,"Background thread destryed");
    }

    private class PhotoHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        private ImageView mImageView;
        private GalleryItem mGalleryItem;

        public PhotoHolder(View v)
        {
            super(v);
            mImageView=(ImageView) v.findViewById(R.id.fragment_photo_gallery_view);
            mImageView.setOnClickListener(this);
        }

        public void bindDrawable(Drawable drawable)
        {
            mImageView.setImageDrawable(drawable);
        }
        public void bindGalleryItem(GalleryItem galleryItem)
        {
            mGalleryItem=galleryItem;
        }
        @Override
        public void onClick(View v)
        {
            Intent intent=new Intent(Intent.ACTION_VIEW,mGalleryItem.getPhotoPageUri());
            startActivity(intent);
        }
    }

    private class PhotoAdapter extends  RecyclerView.Adapter<PhotoHolder>
    {
      //  private List<GalleryItem> mGalleryItems;

        public PhotoAdapter(List<GalleryItem> galleryItems)
        {
            mItems=galleryItems;
        }
        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup viewGroup,int viewType)
        {
            LayoutInflater layoutInflater=LayoutInflater.from(getActivity());
            View v=layoutInflater.inflate(R.layout.gallery_item,viewGroup,false);

            return new PhotoHolder(v);

        }

        @Override
        public void onBindViewHolder(PhotoHolder holder,int position)
        {
            GalleryItem galleryItem=mItems.get(position);
            Drawable drawable=getResources().getDrawable(R.drawable.kama);
            holder.bindGalleryItem(mItems.get(position));
            holder.bindDrawable(drawable);
            mThumbnailDownloader.queueThumbnail(holder,galleryItem.getUrl_s());

        }

        @Override
        public int getItemCount()
        {
            return mItems.size();
        }
    }

}