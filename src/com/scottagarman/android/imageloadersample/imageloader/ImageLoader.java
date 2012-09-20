package com.scottagarman.android.imageloadersample.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.scottagarman.android.imageloadersample.imageloader.operations.FileOperation;
import com.scottagarman.android.imageloadersample.imageloader.operations.NetworkOperation;
import com.scottagarman.android.imageloadersample.imageloader.operations.Operation;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * ImageLoader.java
 *
 * Class designed to download and cache images as well as keep a few in memory for
 * high performance in scrolling uses. For use with utility class CacheImageView
 * or standalone. Files are cached to Android getCacheDir().
 */
public class ImageLoader {
    //TODO: add first in first out queue for operations when over a max(20ish)
    public static final long ONE_DAY_IN_SEC = 24 * 60 * 60;
    private static final long CACHE_EXPIRATION_AGE_IN_SEC = ONE_DAY_IN_SEC * 30;

    private static final String JSON_CACHE_FOLDER = "image_cache";
    private static String CACHE_DIR;
    private static final int MAX_IMAGE_CACHE = 35;
    private static BitmapFactory.Options BITMAP_OPTIONS;
    
    // Static Reference
    private static ImageLoader self;
    
    // LruCache for in memory images
    private LruCache<String, Bitmap> memoryCache;

    // Map for urls, each url can have as many listeners as it wants
    private HashMap<String, ArrayList<ImageLoaderListener>> listenerMap;

    private NetworkOperation.NetworkOperationCompleteListener networkOperationCompleteListener;
    private HashMap<String, Operation> operationHashMap;
    private FileOperation.FileOperationCompleteListener fileOperationCompleteListener;

    // Private constructor
	private ImageLoader(){}

	// Singleton model
	public static ImageLoader getInstance(Context context){
		if(self == null) {
            // Create singleton instance
			self = new ImageLoader();
            self.memoryCache = new LruCache<String, Bitmap>(MAX_IMAGE_CACHE);
            self.listenerMap = new HashMap<String, ArrayList<ImageLoaderListener>>();
            self.operationHashMap = new HashMap<String, Operation>();
            BITMAP_OPTIONS = new BitmapFactory.Options();
            BITMAP_OPTIONS.inPurgeable = true;

            // Get the cache dir from android context and place in folder defined in JSON_CACHE_FOLDER
            CACHE_DIR = context.getCacheDir()+ File.separator+JSON_CACHE_FOLDER;

            // Listener for network operations (url image downloads)
            self.networkOperationCompleteListener = new NetworkOperation.NetworkOperationCompleteListener() {
                @Override
                public void onNetworkOperationComplete(NetworkOperation operation) {
                    // Remove from ops list
                    self.operationHashMap.remove(HashTool.getKeyForUrl(operation.operationUrl));

                    if(operation.responseData != null && operation.responseData.length > 0) {
                        // Get a the result bitmap from the op, place it in memory and update any listener waiting for this data
                        Bitmap b = BitmapFactory.decodeByteArray(operation.responseData, 0, operation.responseData.length, BITMAP_OPTIONS);
                        if (b != null) {
                            synchronized (self.memoryCache) {
                                self.memoryCache.put(HashTool.getKeyForUrl(operation.operationUrl), b);
                            }
                            self.updateListenersAtUrl(operation.operationUrl, b);

                            // Store image for future use
                            File cacheFile = new File(CACHE_DIR, HashTool.getKeyForUrl(operation.operationUrl));
                            BitmapUtils.compressBitmapToDisk(cacheFile, b, 100);
                        }else {
                            // error
                        }
                    }else {
                        // error no image found
                    }
                }

                @Override
                public void onNetworkOperationCompleteWithError(NetworkOperation operation) {
                    self.operationHashMap.remove(HashTool.getKeyForUrl(operation.operationUrl));
                    // error
                }
            };

            // Listener for diskio / file operations
            self.fileOperationCompleteListener = new FileOperation.FileOperationCompleteListener() {
                @Override
                public void onFileOperationComplete(FileOperation operation) {
                    self.operationHashMap.remove(operation.key);
                    if(operation.loadedFile != null) {
                        synchronized (self.memoryCache) {
                            self.memoryCache.put(operation.key, operation.loadedFile);
                        }
                        self.updateListenersWithKey(operation.key, operation.loadedFile);
                    }else {
                        //error
                    }
                }

                @Override
                public void onFileOperationCompleteWithError(FileOperation operation) {
                    self.operationHashMap.remove(operation.key);
                    // error
                }
            };
        }
        return self;
    }

    // Prevent cloning
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
    }

    public void downloadImageAtUrl(long cacheAgeInMs, String url, ImageLoaderListener listener) {
        String key = HashTool.getKeyForUrl(url);

        // Check if in memory
        if(self.memoryCache.get(key) != null){
            listener.onImageLoaded(self.memoryCache.get(key));
            return;
        }

        // Check if loading already
        if(self.listenerMap.containsKey(key)) {
            // already loading, just add to listener map
            synchronized (self.listenerMap) {
                self.listenerMap.get(key).add(listener);
            }
            return;
        }

        // Check if file exists in cache
        final File cacheFile = new File(CACHE_DIR, key);
        Date now = new Date();
        long fileAgeInMs = now.getTime() - cacheFile.lastModified();
        if(cacheFile.exists() && !(fileAgeInMs > cacheAgeInMs)) {
            synchronized (self.listenerMap) {
                ArrayList<ImageLoaderListener> list = new ArrayList<ImageLoaderListener>();
                list.add(listener);
                self.listenerMap.put(key, list);
            }

            FileOperation fo = new FileOperation(cacheFile, key, fileOperationCompleteListener);
            self.operationHashMap.put(key, fo);
            fo.beginOperation();
            return;
        }

        // Get from network
        synchronized (self.listenerMap) {
            ArrayList<ImageLoaderListener> list = new ArrayList<ImageLoaderListener>();
            list.add(listener);
            self.listenerMap.put(key, list);
        }
        NetworkOperation downloader = new NetworkOperation(url, self.networkOperationCompleteListener);
        self.operationHashMap.put(key, downloader);
        downloader.beginOperation();
    }
    
    public void downloadImageAtUrl(String url, ImageLoaderListener listener) {
        final long cacheAgeInMs = CACHE_EXPIRATION_AGE_IN_SEC * 1000;
        downloadImageAtUrl(cacheAgeInMs, url, listener);
    }

    private void updateListenersWithKey(String key, Bitmap bitmap) {
        synchronized (self.listenerMap) {
            if(self.listenerMap.containsKey(key)){
                ArrayList<ImageLoaderListener> list = self.listenerMap.get(key);
                for(int i = 0, len = list.size(); i < len; i++) {
                    list.get(i).onImageLoaded(bitmap);
                }
                self.listenerMap.remove(key);
            }
        }
    }
    
    private void updateListenersAtUrl(String url, Bitmap bitmap) {
        updateListenersWithKey(HashTool.getKeyForUrl(url), bitmap);
    }

    public void cancelDownload(String url, ImageLoaderListener listener) {
        String key = HashTool.getKeyForUrl(url);

        if(self.listenerMap.containsKey(key)) {
            synchronized (self.listenerMap) {
                self.listenerMap.get(key).remove(listener);
                if(self.listenerMap.get(key).size() == 0) {
                    // cancel request
                    if(self.operationHashMap.get(key) != null) {
                        self.operationHashMap.get(key).cancel();
                        self.operationHashMap.remove(key);
                    }
                    // remove request
                    self.listenerMap.remove(key);
                }
            }
        }else {
        }
    }

    public interface ImageLoaderListener {
        public void onImageLoaded(Bitmap bitmap);
    }
}
