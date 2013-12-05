package com.EditorHyde.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.wuman.android.auth.AuthorizationFlow;
import com.wuman.android.auth.AuthorizationUIController;
import com.wuman.android.auth.DialogFragmentController;
import com.wuman.android.auth.OAuthManager;
import com.wuman.android.auth.oauth2.store.SharedPreferencesCredentialStore;

import org.eclipse.egit.github.core.service.UserService;

import java.io.IOException;
import java.util.Arrays;

public class MainActivity extends Activity implements View.OnClickListener {

    SharedPreferences sp;
    ProgressDialog pd = null;
    String authToken;
    Button loginButton;
    String foobar;
    SharedPreferencesCredentialStore credentialStore;
    Credential credential;
    public static final String APP_ID = "com.EditorHyde.app";

    public void nukePreferences() {
        sp = this.getSharedPreferences( APP_ID, MODE_PRIVATE );
        SharedPreferences.Editor edit = sp.edit();
        edit.clear();
        edit.commit();
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Crashlytics.start(this);

        pd = ProgressDialog.show( this, "", "Verifying login token...", true);

        sp = this.getSharedPreferences( APP_ID, MODE_PRIVATE );

        new VerifyUser().execute();
    }

    @Override
    public void onResume() {
        super.onResume();
        setupLogin();
        TextView tv = (TextView)findViewById(R.id.logoutWarning);
        tv.setVisibility(View.VISIBLE);
        //new VerifyUser().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        int groupId = item.getGroupId();
        boolean rv = false;

        if( itemId == R.id.action_scratchpad ) {
            Intent i = new Intent(this, ScratchpadActivity.class);
            startActivity(i);
        }

        return rv;
    }

    private void setupLogin() {
        setContentView(R.layout.main);
        loginButton = (Button)findViewById(R.id.login);
        loginButton.setOnClickListener(this);

//        new DoLogin().execute();
    }

    @Override
    public void onClick(View v) {
        if( v == loginButton) {
            boolean warnedAboutFirstTime = sp.getBoolean( getString(R.string.warnedFirstTime), false );

            if( !warnedAboutFirstTime ) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Warning about oAuth redirection")
                        .setMessage( "If this is your first time using this application and you are using 2-factor authentication, you will need to hit the back button after logging in on GitHub and entering your security code. GitHub does not properly redirect to the auth callback. This message will not be displayed again.")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                sp.edit().putBoolean( getString(R.string.warnedFirstTime), true).commit();
                                new DoLogin().execute();
                            }
                        }).show();
            }
            else {
                new DoLogin().execute();
            }
        }
    }

    private class DoLogin extends AsyncTask<Void, Void, Boolean> {

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
            builder.setScopes(Arrays.asList( "user", "repo" ));

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

    private class VerifyUser extends AsyncTask<Void, Void, Boolean> {

        protected Boolean doInBackground(Void...voids) {
            Boolean rv = false;

            authToken = sp.getString( "authToken", null );

            if( null != authToken ) {
                // If we succeeded, get the user information and store it
                UserService userService = new UserService();
                userService.getClient().setOAuth2Token(authToken);
                String name = null;
                try {
                    name = userService.getUser().getName();
                    String login = userService.getUser().getLogin();
                    sp.edit().putString( "name", name ).commit();
                    sp.edit().putString( "login", login ).commit();
                    rv = true;

                } catch (IOException e) {

                }
            }

            return rv;
        }

        protected void onPostExecute(Boolean result) {
            pd.hide();

            if( !result ) {
                setupLogin();
            }
            else {
                showRepoList();
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


    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);  // Add this method.
    }

    @Override
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);  // Add this method.
    }
}
