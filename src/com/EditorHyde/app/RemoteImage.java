package com.EditorHyde.app;

import android.graphics.Bitmap;

/**
 * Created with IntelliJ IDEA.
 * User: xrdawson
 * Date: 4/2/13
 * Time: 2:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class RemoteImage {

    private String mURL;
    private Bitmap mBmp;

    public RemoteImage( String url, Bitmap bmp ) {
        mURL = url;
        mBmp = bmp;
    }

    public String getUrl() {
        return mURL;
    }

    public Bitmap getBmp() {
        return mBmp;
    }
}
