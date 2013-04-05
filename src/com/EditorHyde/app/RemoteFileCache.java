package com.EditorHyde.app;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: xrdawson
 * Date: 4/3/13
 * Time: 10:51 AM
 * To change this template use File | Settings | File Templates.
 */
public class RemoteFileCache {

    private static ArrayList<RemoteImage> mImages;
    private static ArrayList<String> mUrls;
    private static boolean mLoaded;
    private static String mHttpRoot;

    public static ArrayList<RemoteImage> getImages() {
        return mImages;
    }

    public static void setHttpRoot( String root ) {
        mHttpRoot = root;
    }

    public static String getHttpRoot() {
        return mHttpRoot;
    }

    public static void clear() {
        if( null != mImages ) {
        mImages.clear();
        }
    }

    public static void loadImagesReferences( ArrayList<String> urls ) {
        mUrls = urls;
    }

    public static boolean isLoaded() {
        return mLoaded;
    }

    public static void loadImages() {

        mImages = new ArrayList<RemoteImage>();

        for( String imageUrl : mUrls ) {
            try {
                Bitmap bmp = RemoteFileCache.getRemoteImage(new URL(imageUrl));
                RemoteImage ri = new RemoteImage( imageUrl, bmp );
                mImages.add( ri );
            } catch (MalformedURLException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        mLoaded = true;
    }

    // Thanks
    // http://stackoverflow.com/questions/3075637/loading-remote-images
    public static Bitmap getRemoteImage(final URL aURL) {
        Bitmap bm = null;
        try {
            URLConnection conn = aURL.openConnection();
            conn.connect();
            BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
            bm = BitmapFactory.decodeStream(bis);
            bis.close();
        } catch (IOException e) {}
        return bm;
    }

    public static void addImage( RemoteImage ri ) {
        if( null != mImages ) {
            mImages.add( 0, ri );
        }
    }
}
