package com.EditorHyde.app;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

/**
 * Created with IntelliJ IDEA.
 * User: xrdawson
 * Date: 3/15/13
 * Time: 2:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class RenderedActivity extends Activity {


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.rendered_markup);
        String data = savedInstanceState.getString("rendered");
        WebView wv = (WebView) findViewById(R.id.webView);
        wv.loadData(data, "text/html", null);

    }
}
