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

    private String mThumbURL;
    private Bitmap mBmp;
    private String mFullURL;

    public RemoteImage( String url, Bitmap bmp ) {
        mThumbURL = url;
        mBmp = bmp;

        mFullURL = url.replace( "-thumb", "");
    }

    public String getThumbUrl() {
        return mThumbURL;
    }

    public String getUrl() {
        return mFullURL;
    }

    public Bitmap getBmp() {
        return mBmp;
    }
}
