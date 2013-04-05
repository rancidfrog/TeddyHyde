package com.EditorHyde.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: xrdawson
 * Date: 3/27/13
 * Time: 11:01 AM
 * To change this template use File | Settings | File Templates.
 */


public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private RemoteImage[] mImages;

    public ImageAdapter(Context c) {
        mContext = c;
    }

    public int getCount() {
        return mImages.length;
    }

    public void setImages( RemoteImage[] images ) {
        mImages = images;
    }

    public Object getItem(int position) {
        return mImages[ position ];
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {  // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams( PixActivity.THUMBNAIL_WIDTH, (int) (PixActivity.THUMBNAIL_WIDTH * 1.25)));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(4, 4, 4, 6);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageBitmap( mImages[position].getBmp() );


        return imageView;
    }

}