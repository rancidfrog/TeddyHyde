package com.EditorHyde.app;

import android.graphics.Bitmap;
import com.EditorHyde.app.RemoteImage;

/**
 * Created with IntelliJ IDEA.
 * User: xrdawson
 * Date: 4/2/13
 * Time: 2:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class RemoteImage {

    private String mThumbURI;
    private Bitmap mBmp;
    private String mFullURI;

    public RemoteImage( String uri, Bitmap bmp ) {
        // Tack on / to front if not there.
        if('/' != uri.charAt( 0 ) ) {
            uri = "/" + uri;
        }

        mThumbURI = uri;
        mBmp = bmp;

        mFullURI = uri.replace( "-thumb", "");
    }

    public String getThumbURI() {
        return mThumbURI;
    }

    public String getFullURI() {
        return mFullURI;
    }

    public String getThumbUrl() {
        return RemoteFileCache.getHttpRoot() + mThumbURI;
    }

    public String getUrl() {
        return RemoteFileCache.getHttpRoot() + mFullURI;
    }

    public Bitmap getBmp() {
        return mBmp;
    }
}
