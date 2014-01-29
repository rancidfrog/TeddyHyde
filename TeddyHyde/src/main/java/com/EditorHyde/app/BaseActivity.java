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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.roscopeco.ormdroid.ORMDroidApplication;
import com.squareup.picasso.Picasso;
import com.wuman.android.auth.AuthorizationFlow;
import com.wuman.android.auth.AuthorizationUIController;
import com.wuman.android.auth.DialogFragmentController;
import com.wuman.android.auth.OAuthManager;
import com.wuman.android.auth.oauth2.store.SharedPreferencesCredentialStore;

import org.eclipse.egit.github.core.Authorization;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.OAuthService;
import org.eclipse.egit.github.core.service.OrganizationService;
import org.eclipse.egit.github.core.service.UserService;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BaseActivity extends FragmentActivity {

    Drawable avatar;
    public static final String APP_ID = "com.EditorHyde.app";
    public static String logname = "com.EditorHyde.app";
    protected ProgressDialog pd;
    SharedPreferences sp;
    String authToken;
    Context ctx;
    SharedPreferencesCredentialStore credentialStore;
    Credential credential;
    String organization;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ORMDroidApplication.initialize(this);

        sp = getSharedPreferences(MainActivity.APP_ID, MODE_PRIVATE);

        loadOrganization();

        if( null == authToken ) {
            authToken = sp.getString( "authToken", null );
        }

        if( null == avatar ) {
            new LoadAvatar().execute();
        }
        else if( null != avatar ) {
            getActionBar().setIcon( avatar );
        }

        ctx = this;
    }

    private void loadOrganization() {
        organization = sp.getString( "organization", null );
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

        protected Boolean doInBackground(Void...voids) {

            Boolean rv = true;

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

    private void setOrganization( String login ) {
        Log.v("BaseActivity", "Got organization: " + login);
        sp.edit().putString("organization", login).commit();
        showRepoList();
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

            String[] list = new String[organizations.size()];
            for (int i = 0; i < organizations.size(); i++ ) {
                String name = organizations.get( i ).getLogin();
                list[i] = name;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(BaseActivity.this)
                    .setTitle("Choose an organization")
                    .setSingleChoiceItems(list, -1, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            setOrganization(organizations.get(which).getLogin());
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton("Use me (no organization)", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            setOrganization(null);
                            dialog.dismiss();
                        }
                    });
            builder.show();
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
            us.getClient().setOAuth2Token( authToken );
            User user = null;
            try {
                user = us.getUser();
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



    protected class DoLogin extends AsyncTask<Void, Void, Boolean> {

        private class GitHubConstants {
            public static final String CLIENT_ID = "e4f185a088112cb1b0e9";

            public static final String CLIENT_SECRET = "5a46ba23d0d66ae5fa4eeca519f502fb3f9a5a09";

            public static final String AUTHORIZATION_CODE_SERVER_URL = "https://github.com/login/oauth/authorize";

            public static final String TOKEN_SERVER_URL = "https://github.com/login/oauth/access_token";

            public static final String REDIRECT_URL = "http://localhost/Callback";

            private GitHubConstants() {
            }
        }

        protected Boolean doInBackground(Void...voids) {

            AuthorizationFlow.Builder builder = new AuthorizationFlow.Builder(
                    BearerToken.authorizationHeaderAccessMethod(),
                    AndroidHttp.newCompatibleTransport(),
                    new JacksonFactory(),
                    new GenericUrl(GitHubConstants.TOKEN_SERVER_URL),
                    new ClientParametersAuthentication( GitHubConstants.CLIENT_ID, GitHubConstants.CLIENT_SECRET ),
                    GitHubConstants.CLIENT_ID,
                    GitHubConstants.AUTHORIZATION_CODE_SERVER_URL);
            builder.setCredentialStore(credentialStore);
            builder.setScopes(Arrays.asList("user", "repo", "gist"));

            AuthorizationFlow flow = builder.build();

            AuthorizationUIController controller =
                    new DialogFragmentController(getFragmentManager()) {

                        @Override
                        public String getRedirectUri() throws IOException {
                            return GitHubConstants.REDIRECT_URL;
                        }

                        @Override
                        public boolean isJavascriptEnabledForWebView() {
                            return true;
                        }

                    };

            OAuthManager oauth = new OAuthManager(flow, controller);

            try {
                credential = oauth.authorizeExplicitly("userId", null, null).getResult();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return true;
        }

        protected void onPostExecute(Boolean result) {

            if( null != credential) {
                authToken = credential.getAccessToken();
                sp.edit().putString( "authToken", authToken ).commit();
                Log.w("TeddyHyde", "Credentials are OK: " +  authToken );
                new LoadUserTask().execute();
            } else {
                Log.w( "TeddyHyde", "Bad credentials!");
            }
        }
    }


    private class LoadUserTask extends AsyncTask<Void, Void, Boolean> {
        protected Boolean doInBackground(Void...voids) {
            Boolean rv = true;

            try {
                UserService userService = new UserService();
                userService.getClient().setOAuth2Token(authToken);
                String name = userService.getUser().getName();
                String login = userService.getUser().getLogin();
                sp.edit().putString( "name", name ).commit();
                sp.edit().putString( "login", login ).commit();

            } catch (IOException e) {
                e.printStackTrace();
                rv = false;
            }

            return rv;
        }

        protected void onPostExecute(Boolean result) {
            if( !result ) {
                Toast.makeText( getApplicationContext(), "Invalid credentials, try again.", Toast.LENGTH_LONG );
            }
            else {
                showRepoList();
            }

        }
    }

    public void showRepoList() {

        Intent i = new Intent(this, RepoListActivity.class);
        startActivity(i);

    }


}


