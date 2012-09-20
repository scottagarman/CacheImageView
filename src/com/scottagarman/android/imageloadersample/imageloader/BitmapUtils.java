package com.scottagarman.android.imageloadersample.imageloader;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.*;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
/**
 * BitmapUtils.java
 *
 * Utility class with bitmap manipulation tools
 */
public class BitmapUtils {
    // Used for handling messages from other threads
	private final static int OPERATION_SUCCESS = 900;
	private final static int OPERATION_FAILURE = 901;

    // decodes image and scales it to reduce memory consumption
    public static Bitmap decodeFile(File f, int size){
        try {
            //Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            //TODO: make async
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);

            //The new size we want to scale to
            final int REQUIRED_SIZE=size;

            //Find the correct scale value. It should be the power of 2.
            int width_tmp=o.outWidth, height_tmp=o.outHeight;
            int scale=1;
            while(true){
                if(width_tmp/2<REQUIRED_SIZE || height_tmp/2<REQUIRED_SIZE)
                    break;
                width_tmp/=2;
                height_tmp/=2;
                scale*=2;
            }

            //Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize=scale;
            o2.inPurgeable = true;

            //TODO: make async
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {}
        return null;
    }

    public static Bitmap decodeFile(File file) {
        Bitmap toRet = null;
        try {
            toRet = BitmapFactory.decodeStream(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e){
            e.printStackTrace();
        }
        return toRet;
    }

    // decodes image and scales it to reduce memory consumption
    public static void decodeFileAsync(final File f, final int size, final BitmapUtilsListener listener) {
        final Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {

                switch(msg.what){

                // Successful network operation
                case OPERATION_SUCCESS:
                    if(listener != null) listener.onImageReady((Bitmap) msg.obj);
                    break;
                // Failed network operation
                case OPERATION_FAILURE:
                    if(listener != null) listener.onImageReady(null);
                    break;
                }

                super.handleMessage(msg);
            }
	    };
        //TODO: this could probably a neat asynctask
        new Thread(){
            @Override
            public void run() {
                Message msg = Message.obtain();
                try {
                    //Decode image size
                    BitmapFactory.Options o = new BitmapFactory.Options();
                    o.inJustDecodeBounds = true;
                    //TODO: make async
                    BitmapFactory.decodeStream(new FileInputStream(f), null, o);

                    //The new size we want to scale to
                    final int REQUIRED_SIZE=size;

                    //Find the correct scale value. It should be the power of 2.
                    int width_tmp=o.outWidth, height_tmp=o.outHeight;
                    int scale=1;
                    while(true){
                        if(width_tmp/2<REQUIRED_SIZE || height_tmp/2<REQUIRED_SIZE)
                            break;
                        width_tmp/=2;
                        height_tmp/=2;
                        scale*=2;
                    }

                    //Decode with inSampleSize
                    BitmapFactory.Options o2 = new BitmapFactory.Options();
                    o2.inSampleSize=scale;
                    o2.inPurgeable = true;

                    msg.what = OPERATION_SUCCESS;
                    msg.obj = BitmapFactory.decodeStream(new FileInputStream(f), null, o2);

                } catch (FileNotFoundException e) {
                    msg.what = OPERATION_FAILURE;
                }
                handler.sendMessage(msg);
            }
        }.start();
    }

    public static void compressBitmapToDisk(final File source, final File dest, final int quality, final int size) {
        new Thread(){
            @Override
            public void run() {
                Bitmap b = decodeFile(source, size);
                try {
                    dest.getParentFile().mkdirs();
                    FileOutputStream out = new FileOutputStream(dest);
                    b.compress(Bitmap.CompressFormat.PNG, quality, out);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public static void compressBitmapToDisk(final File dest, final Bitmap b, final int quality) {
        new Thread(){
            @Override
            public void run() {
                try {
                    dest.getParentFile().mkdirs();
                    FileOutputStream out = new FileOutputStream(dest);
                    b.compress(Bitmap.CompressFormat.PNG, quality, out);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public static void compressBitmapToDisk(final File source, final File dest, final int quality, final int size, final BitmapUtilsListener listener) {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                switch(msg.what){

                // Successful network operation
                case OPERATION_SUCCESS:
                    if(listener != null) listener.onImageReady((Bitmap) msg.obj);
                    break;
                // Failed network operation
                case OPERATION_FAILURE:
                    if(listener != null) listener.onImageReady(null);
                    break;
                }

                super.handleMessage(msg);
            }
	    };

        new Thread() {
            @Override
            public void run() {
                Bitmap b = decodeFile(source, size);
                Message msg = Message.obtain();
                try {
                    dest.getParentFile().mkdirs();
                    FileOutputStream out = new FileOutputStream(dest);
                    b.compress(Bitmap.CompressFormat.PNG, quality, out);
                    msg.what = OPERATION_SUCCESS;
                    msg.obj = b;
                } catch (Exception e) {
                    e.printStackTrace();
                    msg.what = OPERATION_FAILURE;
                }
                handler.sendMessage(msg);
            }
        }.start();
    }

    public static void compressBitmapToDisk(final File dest, final Bitmap b, final int quality, final BitmapUtilsListener listener) {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                switch(msg.what){

                // Successful network operation
                case OPERATION_SUCCESS:
                    if(listener != null) listener.onImageReady((Bitmap) msg.obj);
                    break;
                // Failed network operation
                case OPERATION_FAILURE:
                    if(listener != null) listener.onImageReady(null);
                    break;
                }

                super.handleMessage(msg);
            }
	    };

        new Thread() {
            @Override
            public void run() {
                Message msg = Message.obtain();
                try {
                    dest.getParentFile().mkdirs();
                    FileOutputStream out = new FileOutputStream(dest);
                    b.compress(Bitmap.CompressFormat.PNG, quality, out);
                    msg.what = OPERATION_SUCCESS;
                    msg.obj = b;
                } catch (Exception e) {
                    e.printStackTrace();
                    msg.what = OPERATION_FAILURE;
                }
                handler.sendMessage(msg);
            }
        }.start();
    }

    public static String getPathAsString(Context ctx, Uri uri) {
        String filePath;
        Uri selectedImage = uri;

        try{ // Fails if from file:/// instead of content:///
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = ctx.getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            filePath = cursor.getString(columnIndex);
            cursor.close();
        }catch (Exception e){
            filePath = uri.getPath();
        }
        return filePath;
    }

    public static void getRealPathFromURI(final Activity ctx, final Uri contentUri, final BitmapUtilsQueryListener listener) {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                switch(msg.what){

                // Successful network operation
                case OPERATION_SUCCESS:
                    if(listener != null) listener.onQueryFinished((String) msg.obj);
                    break;
                // Failed network operation
                case OPERATION_FAILURE:
                    if(listener != null) listener.onQueryFinished(null);
                    break;
                }

                super.handleMessage(msg);
            }
	    };

        new Thread() {
            @Override
            public void run() {
                Message msg = Message.obtain();
                try {
                    String[] proj = { MediaStore.Images.Media.DATA };
                    Cursor cursor = ctx.managedQuery(contentUri, proj, null, null, null);
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    cursor.moveToFirst();
                    ctx.stopManagingCursor(cursor);
                    msg.what = OPERATION_SUCCESS;
                    msg.obj = cursor.getString(column_index);
                } catch (Exception e) {
                    e.printStackTrace();
                    msg.what = OPERATION_FAILURE;
                }
                handler.sendMessage(msg);
            }
        }.start();
    }

    /**
     * returns a center cropped image
     */
    public static Bitmap getCenterCroppedBitmap(Bitmap b, float ratio) {
        int height = b.getHeight();
        int width = b.getWidth();

        int newHeight = (int)((float)width * ratio);

        Bitmap croppedImage = Bitmap.createBitmap(width, newHeight, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(croppedImage);

        Rect srcRect = new Rect(0,0, width, height);
        Rect dstRect = new Rect(0, 0, width, newHeight);

        int dx = (srcRect.width() - dstRect.width()) / 2;
        int dy = (srcRect.height() - dstRect.height()) / 2;

        // If the srcRect is too big, use the center part of it.
        srcRect.inset(Math.max(0, dx), Math.max(0, dy));

        // If the dstRect is too big, use the center part of it.
        dstRect.inset(Math.max(0, -dx), Math.max(0, -dy));


        // Make paint for gud quality
        Paint paint = new Paint();
        paint.setFilterBitmap(true);

        // Draw the cropped bitmap in the center
        canvas.drawBitmap(b, srcRect, dstRect, paint);
        b.recycle();

        return croppedImage;
    }

    public static Bitmap greyScaler(Bitmap b) {
        Bitmap grayscaleBitmap = Bitmap.createBitmap(b.getWidth(), b.getHeight(), Bitmap.Config.RGB_565);
        Canvas c = new Canvas(grayscaleBitmap);
        Paint p = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(cm);
        p.setColorFilter(filter);
        c.drawBitmap(b, 0, 0, p);
        return grayscaleBitmap;
    }

    public static Bitmap toSephia(Bitmap bmpOriginal) {
        int width, height, r,g, b, c, gry; height = bmpOriginal.getHeight(); width = bmpOriginal.getWidth(); int depth = 20;

        Bitmap bmpSephia = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bmpSephia);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setScale(.3f, .3f, .3f, 1.0f);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        canvas.drawBitmap(bmpOriginal, 0, 0, paint);
        for(int x=0; x < width; x++) {
            for(int y=0; y < height; y++) {
                c = bmpOriginal.getPixel(x, y);

                r = Color.red(c);
                g = Color.green(c);
                b = Color.blue(c);

                gry = (r + g + b) / 3;
                r = g = b = gry;

                r = r + (depth * 2);
                g = g + depth;

                if(r > 255) {
                  r = 255;
                }
                if(g > 255) {
                  g = 255;
                }
                bmpSephia.setPixel(x, y, Color.rgb(r, g, b));
            }
        }
        return bmpSephia;
    }

    public static Bitmap shrinkBitmap(Bitmap _bitmapPreScale, int factor) {
        int oldWidth = _bitmapPreScale.getWidth();
        int oldHeight = _bitmapPreScale.getHeight();
        int newWidth = oldWidth / factor;  // whatever your desired width and height are
        int newHeight = oldHeight / factor;

        // calculate the scale
        float scaleWidth = ((float) newWidth) / oldWidth;
        float scaleHeight = ((float) newHeight) / oldHeight;

        // create a matrix for the manipulation
        Matrix matrix = new Matrix();
        // resize the bit map
        matrix.postScale(scaleWidth, scaleHeight);

        // recreate the new Bitmap
        return Bitmap.createBitmap(_bitmapPreScale, 0, 0, oldWidth, oldHeight, matrix, true);
    }

    public static interface BitmapUtilsListener {
        public void onImageReady(Bitmap bitmap);
    }
    public static interface BitmapUtilsQueryListener {
        public void onQueryFinished(String path);
    }

}
