package com.EditorHyde.app;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * Created with IntelliJ IDEA.
 * User: xrdawson
 * Date: 3/14/13
 * Time: 1:24 AM
 * To change this template use File | Settings | File Templates.
 */
public class EditorActivity extends Activity { // } implements View.OnClickListener {


    private Context cxt;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.editor);
        cxt = this;
        MarkdownPagerAdapter adapter = new MarkdownPagerAdapter();
        ViewPager vp = (ViewPager) findViewById(R.id.markdownPager);
        vp.setAdapter(adapter);

    }


    private class MarkdownPagerAdapter extends PagerAdapter{


        @Override
        public int getCount() {
            return 2;
        }

        /**
         * Create the page for the given position.  The adapter is responsible
         * for adding the view to the container given here, although it only
         * must ensure this is done by the time it returns from
         * {@link #finishUpdate(android.view.ViewGroup)}.
         *
         * @param collection The containing View in which the page will be shown.
         * @param position The page position to be instantiated.
         * @return Returns an Object representing the new page.  This does not
         * need to be a View, but can be some other container of the page.
         */
        @Override
        public Object instantiateItem(ViewGroup collection, int position) {
            TextView tv = new TextView(cxt);
            tv.setText(getString(R.string.app_name) + position);
            tv.setTextColor(Color.WHITE);
            tv.setTextSize(30);

            collection.addView(tv,0);

            return tv;
        }

        /**
         * Remove a page for the given position.  The adapter is responsible
         * for removing the view from its container, although it only must ensure
         * this is done by the time it returns from {@link #finishUpdate(android.view.ViewGroup)}.
         *
         * @param collection The containing View from which the page will be removed.
         * @param position The page position to be removed.
         * @param view The same object that was returned by
         * {@link #instantiateItem(android.view.View, int)}.
         */
        @Override
        public void destroyItem(ViewGroup collection, int position, Object view) {
            collection.removeView((TextView) view);
        }


        /**
         * Determines whether a page View is associated with a specific key object
         * as returned by instantiateItem(ViewGroup, int). This method is required
         * for a PagerAdapter to function properly.
         * @param view Page View to check for association with object
         * @param object Object to check for association with view
         * @return
         */
        @Override
        public boolean isViewFromObject(View view, Object object) {
            return (view==object);
        }


        /**
         * Called when the a change in the shown pages has been completed.  At this
         * point you must ensure that all of the pages have actually been added or
         * removed from the container as appropriate.
         * @param arg0 The containing View which is displaying this adapter's
         * page views.
         */
        @Override
        public void finishUpdate(ViewGroup arg0) {}


        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {}

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void startUpdate(ViewGroup arg0) {}

    }


}