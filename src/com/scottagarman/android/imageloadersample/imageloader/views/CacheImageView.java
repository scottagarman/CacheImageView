package com.scottagarman.android.imageloadersample.imageloader.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.scottagarman.android.imageloadersample.imageloader.ImageLoader;

/**
 * CacheImageView.java
 *
 * Utility class that subclasses ImageView to allow easy
 * loading of images from urls. Uses ImageLoader to do
 * the async loading. Supports call canceling for when used in
 * listviews / being recycled.
 */
public class CacheImageView extends ImageView implements ImageLoader.ImageLoaderListener{
    protected String mImageUrl;
    protected ImageLoader mImageLoader;
    protected CacheImageViewListener mListener;

    public CacheImageView(Context context) {
        super(context);
        init();
    }

    public CacheImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CacheImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mImageLoader = ImageLoader.getInstance(getContext());
    }
    
    public void setImageUrl(String url) {
        cancelRequest();
        if(url != null) {
            mImageUrl = url;
            mImageLoader.downloadImageAtUrl(mImageUrl, this);
        }
    }

    public void setImageUrl(String url, Drawable loadingDrawable) {
        setImageDrawable(loadingDrawable);
        setImageUrl(url);
    }

    public void cancelRequest() {
        if(mImageUrl != null)mImageLoader.cancelDownload(mImageUrl, this);
    }
    
    public void setListener(CacheImageViewListener listener) {
        mListener = listener;
    }

    /* ImageLoaderListener */
    @Override
    public void onImageLoaded(Bitmap bitmap) {
        setImageBitmap(bitmap);
        if(mListener != null) mListener.onImageLoaded();
    }
    
    public interface CacheImageViewListener {
        public void onImageLoaded();
    }
    
}
