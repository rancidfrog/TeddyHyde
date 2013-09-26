package com.teddyhyde;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

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

import org.eclipse.egit.github.core.Authorization;
import org.eclipse.egit.github.core.service.OAuthService;
import org.eclipse.egit.github.core.service.UserService;

import java.io.IOException;
import java.util.Arrays;

//import com.wuman.oauth.samples.OAuth;

public class MainActivity extends Activity {

    SharedPreferences sp;
    TextView loginMessage = null;
    ProgressDialog pd = null;
    String authToken;
    String foobar;
    public static String logname = "com.teddyhyde.app";

    public static final String APP_ID = "com.teddyhyde.app";

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

        pd = ProgressDialog.show( this, "", "Verifying login token...", true);

        sp = this.getSharedPreferences( APP_ID, MODE_PRIVATE );
        new VerifyUser().execute();
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

//        setContentView(R.layout.main);
//
//        loginMessage = (TextView)findViewById(R.id.loginMessage);
//
//        String email = sp.getString( "email", null );
//        String password = sp.getString( "password", null );
//
//        if( null != email && null != password ) {
//            EditText etU = (EditText)findViewById(R.id.githubEmail);
//            EditText etP = (EditText)findViewById(R.id.githubPassword);
//            etU.setText( email );
//            etP.setText(password);
//        }
//
//        findViewById(R.id.button).setOnClickListener(
//                new View.OnClickListener() {
//                    public void onClick(View v) {
////
//                        loginMessage.setText( "Logging in...");
//                        EditText etU = (EditText)findViewById(R.id.githubEmail);
//                        EditText etP = (EditText)findViewById(R.id.githubPassword) ;
//
//                        String email = etU.getText().toString();
//                        String password = etP.getText().toString();
//                        sp.edit().putString( "email", email ).commit();
//                        sp.edit().putString( "password", password ).commit();
//                        new LoginTask().execute();
//                    }
//                });

          // getAuthFromGoogleAccounts();

//            getAuthFromOauth();

        new DoLogin().execute();

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

        Credential credential;

        protected Boolean doInBackground(Void...voids) {


            SharedPreferencesCredentialStore credentialStore =
                    new SharedPreferencesCredentialStore(getApplicationContext(),
                            "preferenceFileName", new JacksonFactory());

            AuthorizationFlow.Builder builder = new AuthorizationFlow.Builder(
                    BearerToken.authorizationHeaderAccessMethod(),
                    AndroidHttp.newCompatibleTransport(),
                    new JacksonFactory(),
                    new GenericUrl(GitHubConstants.TOKEN_SERVER_URL),
                    new ClientParametersAuthentication( GitHubConstants.CLIENT_ID, GitHubConstants.CLIENT_SECRET ),
                    GitHubConstants.CLIENT_ID,
                    GitHubConstants.AUTHORIZATION_CODE_SERVER_URL);
            builder.setCredentialStore(credentialStore);
            builder.setScopes(Arrays.asList( "user", "repo", "gist" ));

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

                credential = oauth.authorizeImplicitly("userId", null, null).getResult();
            } catch (IOException e) {
                e.printStackTrace();
            }

            String bar = "";
            if( null == flow ) {
                bar = "asdasd";
            }
            else {
                bar = "a4234sdasd";
            }


            return true;
        }

        protected void onPostExecute(Boolean result) {


            if( null != credential) {
                authToken = credential.getAccessToken();
                Log.w("TeddyHyde", "Credentials are OK!: " + credential.getAccessToken());
            } else {
                Log.w( "TeddyHyde", "Bad credentials!");
            }
        }
    }


    private void getAuthFromGoogleAccounts() {
        AccountManager am = AccountManager.get(this);
        //Bundle options = new Bundle();

        Account[] accounts;
        accounts = am.getAccountsByType("com.github");

        if( accounts.length > 0 ) {
            am.getAuthToken(
                    accounts[0],                     // Account retrieved using getAccountsByType()
                    "repo, user",            // Auth scope
                    null,                        // Authenticator-specific options
                    this,                           // Your activity
                    new OnTokenAcquired(),          // Callback called when a token is successfully acquired
                    new Handler(new OnError()));    // Callback called if an error occurs
        }
        else {
            startActivity(new Intent(Settings.ACTION_ADD_ACCOUNT));
        }
    }

    private class OnTokenAcquired implements AccountManagerCallback<Bundle> {
        @Override
        public void run(AccountManagerFuture<Bundle> result) {
            // Get the result of the operation from the AccountManagerFuture.
            Bundle bundle = null;
            try {
                bundle = result.getResult();
            } catch (OperationCanceledException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (AuthenticatorException e) {
                e.printStackTrace();
            }

            // The token is a named value in the bundle. The name of the value
            // is stored in the constant AccountManager.KEY_AUTHTOKEN.
            authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);
            foobar = authToken;
        }
    }


    private class VerifyUser extends AsyncTask<Void, Void, Boolean> {

        protected Boolean doInBackground(Void...voids) {
            Boolean rv = false;

            String authToken = sp.getString( "authToken", null );

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


    private class LoginTask extends AsyncTask<Void, Void, Boolean> {
        protected Boolean doInBackground(Void...voids) {
            Boolean rv = true;
            String email, password;

            EditText etU = (EditText)findViewById(R.id.githubEmail);
            EditText etP = (EditText)findViewById(R.id.githubPassword);
            email = etU.getText().toString();
            password = etP.getText().toString();

            OAuthService oauthService = new OAuthService();
            // Replace with actual login and password
            oauthService.getClient().setCredentials(email, password);

            // Create authorization with 'gist' scope only
            Authorization auth = new Authorization();
            auth.setScopes(Arrays.asList("gist", "repo"));
            String authToken = null;
            try {
                auth = oauthService.createAuthorization(auth);
                authToken = auth.getToken();

                // Store it for other activities
                sp.edit().putString( "authToken", authToken ).commit();

                // If we succeeded, get the user information and store it
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
                loginMessage.setText( "Invalid credentials, try again.");
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
