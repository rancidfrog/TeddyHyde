package com.EditorHyde.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import  com.commonsware.cwac.anddown.*;
/**
 * Created with IntelliJ IDEA.
 * User: xrdawson
 * Date: 3/14/13
 * Time: 1:24 AM
 * To change this template use File | Settings | File Templates.
 */
public class EditorActivity extends Activity implements View.OnClickListener {

   AndDown ad;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.editor);

        ad = new AndDown();

        ImageButton renderButton = (ImageButton) findViewById(R.id.imageButton);
        renderButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.imageButton) {
            renderToWebView();
        }
    }

    private void renderToWebView() {
        TextView tv = (TextView) findViewById(R.id.editText1);
        String rendered = ad.markdownToHtml( tv.toString() );
        Intent i = new Intent(this, RenderedActivity.class);
        i.putExtra( "rendered", rendered );
        startActivity(i);
    }


}