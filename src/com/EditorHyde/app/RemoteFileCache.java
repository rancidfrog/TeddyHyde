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

    private static RemoteImage[] mImages;

    public static RemoteImage[] getImages() {
        return mImages;
    }

    public static void clear() {
        mImages = null;
    }

    public static void loadImages( ArrayList<String> urls ) {
        mImages = new RemoteImage[urls.size()];

        int index = 0;
        for( String imageUrl : urls ) {
            try {
                Bitmap bmp = RemoteFileCache.getRemoteImage(new URL(imageUrl));
                mImages[index] = new RemoteImage( imageUrl, bmp );
            } catch (MalformedURLException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            index++;
        }
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

}
