package com.scottagarman.android.imageloadersample.imageloader.operations;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;

import java.io.File;
import java.io.Serializable;

/**
 * FileOperation.java
 *
 * Subclass of Operation for reading files in the shared thread pool
 *
 */
public class FileOperation extends Operation implements Serializable {
	private static final long serialVersionUID = -3389953904685901495L;
    private static BitmapFactory.Options BITMAP_OPTIONS = new BitmapFactory.Options();

	// Delegate for listening on operation completion
	public FileOperationCompleteListener delegate;

	// Constants for thread messages
	private final int NETWORK_OPERATION_SUCCESS = 100;
	private final int NETWORK_OPERATION_FAILURE = 101;

	// request data
	public File fileLocation;
	public Bitmap loadedFile;
    public String key;

	// response data
	public Exception responseError;

	public FileOperation(File f, String k, FileOperationCompleteListener del){
        fileLocation = f;
        key = k;
        delegate = del;
        BITMAP_OPTIONS.inPurgeable = true;
	}

	
	/**
	 * Adds operation runnable to thread pool and begins thread tasks.
	 */
	public void beginOperation(){
		super.beginOperation(new FileOperationRunnable());
	}
	
	/**
	 * Cancels operation.
	 */
    @Override
	public void cancel(){
        super.cancel();
        delegate = null;
	}

	private class FileOperationRunnable implements Runnable {
		
		public void run(){
            try{
                loadedFile = BitmapFactory.decodeFile(fileLocation.getAbsolutePath(), BITMAP_OPTIONS);
                mHandler.sendEmptyMessage(NETWORK_OPERATION_SUCCESS);
            }catch(Exception e){
                responseError = e;
                mHandler.sendEmptyMessage(NETWORK_OPERATION_FAILURE);
            }
		}
	}
	
	/**
	 * Thread handler. This object is used to notify the main UI thread
	 * that activity has completed on the operation thread.
	 */
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			
			switch(msg.what){
			
			// Successful operation
			case NETWORK_OPERATION_SUCCESS:
				if(delegate != null)
					delegate.onFileOperationComplete(FileOperation.this);
				break;
			
			// Failed operation
			case NETWORK_OPERATION_FAILURE:
				if(delegate != null)
					delegate.onFileOperationCompleteWithError(FileOperation.this);
				break;
			
			}
			
			super.handleMessage(msg);
		}
	};

	public interface FileOperationCompleteListener {
		public void onFileOperationComplete(FileOperation operation);
		public void onFileOperationCompleteWithError(FileOperation operation);
	}

}
