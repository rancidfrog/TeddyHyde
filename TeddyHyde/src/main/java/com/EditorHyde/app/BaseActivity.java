package com.EditorHyde.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.eclipse.egit.github.core.Authorization;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.OAuthService;
import org.eclipse.egit.github.core.service.OrganizationService;
import org.eclipse.egit.github.core.service.UserService;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class BaseActivity extends FragmentActivity {

    Drawable avatar;
    public static final String APP_ID = "com.EditorHyde.app";
    public static String logname = "com.EditorHyde.app";
    protected ProgressDialog pd;
    SharedPreferences sp;
    String authToken;
    Context ctx;

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

        ctx = this;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.base, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();
        int groupId = item.getGroupId();

        if( itemId == android.R.id.home ) {
//            // Navigate "up" the demo structure to the launchpad activity.
//            // See http://developer.android.com/design/patterns/navigation.html for more.
//            NavUtils.navigateUpTo(this, new Intent(this, MainActivity.class));
//            break;
            chooseOrganization();

        }
        else if( itemId == R.id.action_logout ) {
            pd = ProgressDialog.show(this, "", "Logging out from GitHub...", true);
            new LogoutTask().execute();
        }
        else if( itemId == R.id.action_scratchpad ) {
            Intent i = new Intent(this, ScratchpadActivity.class);
            startActivity(i);
        }
        else {
            super.onOptionsItemSelected(item);
        }

        return super.onOptionsItemSelected(item);
    }


    private class LogoutTask extends AsyncTask<Void, Void, Boolean> {

        SharedPreferences sp;
        protected Boolean doInBackground(Void...voids) {

            Boolean rv = true;

            sp = getBaseContext().getSharedPreferences(MainActivity.APP_ID, MODE_PRIVATE);

            String authToken = sp.getString( "authToken", null );

            if( null != authToken ) {
                OAuthService oauthService = new OAuthService();
                // Replace with actual login and password
                oauthService.getClient().setOAuth2Token(authToken);
                List<Authorization> authorizations = null;
                try {
                    authorizations = oauthService.getAuthorizations();
                    for( Authorization authorization : authorizations ) {
                        oauthService.deleteAuthorization(authorization.getId());
                    }
                }
                catch (IOException e) {
                    rv = false;
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }

            }

            return rv;

        }

        protected void onPostExecute(Boolean result) {
            pd.hide();
            sp.edit().putString( "authToken", "" ).commit();
            finish();
        }
    }




    private void chooseOrganization() {

        sp = this.getSharedPreferences(APP_ID, MODE_PRIVATE);
        authToken = sp.getString("authToken", null);
        new DialogPicker().execute();
    }

    private void setOrganization( String name ) {
        Log.v("BaseActivity", "Got organization: " + name);
        Toast.makeText( ctx, "Got organization: " + name, Toast.LENGTH_LONG );
    }

    private class DialogPicker extends AsyncTask<Void, Void, Boolean> {

        List<User> organizations;
        @Override
        protected Boolean doInBackground(Void... params) {

            // Get the organizations
            UserService u = new UserService();
            OrganizationService os = new OrganizationService();
            os.getClient().setOAuth2Token( authToken );
            organizations = null;
            try {
                organizations = os.getOrganizations();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return true;
        }

        protected void onPostExecute( Boolean rv ) {
            final LinearLayout ll = new LinearLayout(BaseActivity.this);
            ll.setOrientation(LinearLayout.HORIZONTAL);

            if( null != organizations ) {
                for (User organization : organizations) {

                    LinearLayout row = new LinearLayout(BaseActivity.this);
                    row.setOrientation(LinearLayout.VERTICAL);

                    ImageView icon = new ImageView(ctx);
                    Picasso.with(ctx).load(organization.getAvatarUrl()).into(icon);
                    icon.setContentDescription(organization.getName());

                    TextView tv = new TextView(ctx);
                    tv.setContentDescription(organization.getName());

                    row.addView(icon);
                    row.addView(tv);

                    row.setOnClickListener(new LinearLayout.OnClickListener() {
                        public void onClick(View v)
                        {
                            setOrganization((String) v.getContentDescription());
                        }
                    } );
                }
            }

            new AlertDialog.Builder(BaseActivity.this)
                    .setTitle("Choose an organization")
                    .setView(ll)
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.dismiss();
                        }
                    }).show();

        }
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

