package com.teddyhyde;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;
import com.petebevin.markdown.MarkdownProcessor;

/**
 * Created with IntelliJ IDEA.
 * User: xrdawson
 * Date: 3/14/13
 * Time: 1:24 AM
 * To change this template use File | Settings | File Templates.
 */
public class EditorActivity extends Activity { // } implements View.OnClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
    }

}