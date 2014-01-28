package com.EditorHyde.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.UserService;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class BaseActivity extends FragmentActivity {

    Drawable avatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setContentView(R.layout.activity_base);

        if( null == avatar ) {
            new LoadAvatar().execute();
        }
        else if( null != avatar ) {
            getActionBar().setIcon( avatar );
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
       // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.base, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//        if (id == R.id.action_settings) {
//            return true;
//        }

        int itemId = item.getItemId();
        int groupId = item.getGroupId();


        switch( itemId ) {
        case android.R.id.home:
            // Navigate "up" the demo structure to the launchpad activity.
            // See http://developer.android.com/design/patterns/navigation.html for more.
            NavUtils.navigateUpTo(this, new Intent(this, MainActivity.class));
            break;
        }

        return super.onOptionsItemSelected(item);
    }


    private class LoadAvatar extends AsyncTask<Void, Void, Boolean> {



        public Drawable loadImageFromURL(String url ) {
            try {
                InputStream is = (InputStream) new URL(url).getContent();
                Drawable d = Drawable.createFromStream(is, null );
                return d;
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // get the avatar_url from the user object
            UserService us = new UserService();
            //us.getClient().setOAuth2Token( "adad" );
            User user = null;
            try {
                user = us.getUser("xrd");
            } catch (IOException e) {
                e.printStackTrace();
            }
            String avatarUrl = user.getAvatarUrl();

            avatar = loadImageFromURL( avatarUrl );

            return true;
        }

        protected void onPostExecute( Boolean rv ) {
            getActionBar().setIcon( avatar );
        }
    }
}

