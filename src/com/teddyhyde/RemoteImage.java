package com.teddyhyde;

import android.graphics.Bitmap;
import com.teddyhyde.RemoteImage;

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

    public RemoteImage( String thumbnailUri, Bitmap bmp ) {
        // Tack on / to front if not there.
        if('/' != thumbnailUri.charAt( 0 ) ) {
            thumbnailUri = "/" + thumbnailUri;
        }

        mThumbURI = thumbnailUri;
        mBmp = bmp;

        mFullURI = thumbnailUri.replace( "-thumb", "");
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
