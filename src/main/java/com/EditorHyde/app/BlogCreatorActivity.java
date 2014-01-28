package com.EditorHyde.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

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

import java.io.IOException;
import java.util.Arrays;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by xrdawson on 1/24/14.
 */
public class BlogCreatorActivity extends Activity {

    SharedPreferencesCredentialStore credentialStore;
    Credential credential;
    Context ctx = null;
    SharedPreferences sp;
    String token  = null;
    private final String BLOG_CREATOR_TOKEN = "blogCreatorToken";
    String title;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate( savedInstanceState );

        ctx = this;
        sp = ctx.getSharedPreferences(MainActivity.APP_ID, MODE_PRIVATE);
        token = sp.getString( BLOG_CREATOR_TOKEN, null );

        setContentView(R.layout.blog_creator);

        // Add content to the spinners
        String [] themes = { "amelia", "cerulean", "cosmo", "cyborg", "journal", "readable", "simplex", "slate", "spacelab", "spruce", "superhero", "united" };
        String [] types = { "jekyll", "nanoc", "hyde (python)", "hakyll" };

        ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_item, themes );
        Spinner themeSpinner = (Spinner) findViewById( R.id.blogThemeSpinner );
        themeSpinner.setAdapter( adapter );

        ArrayAdapter adapter2 = new ArrayAdapter(this,
                android.R.layout.simple_spinner_item, types );
        Spinner typeSpinner = (Spinner) findViewById( R.id.blogThemeSpinner );
        typeSpinner.setAdapter( adapter2 );

        Button newBlog = (Button) findViewById( R.id.createNewBlogButton );
        newBlog.setOnClickListener( new Button.OnClickListener() {
            public void onClick(View v)
            {
                if( null == token ) {
                    // notify that we need a new safe token
                    new GetNewToken().execute();
                }
                else {
                    createBlogOnServer();
                }
            }
        });


    }

    private void finishWithResult( TeddyHydeService.Blog blog ) {
        Intent intent = new Intent();
        Bundle extras = new Bundle();
        if( null != blog ) {
            extras.putString("blog_id", String.valueOf(blog.id));
            extras.putString( "blog_title", title );
        }
        intent.putExtras(extras);
        setResult( RESULT_OK, intent );
        finish();

    }

     private void createBlogOnServer() {
        EditText titleEt = (EditText) findViewById( R.id.blogTitle );
        EditText subtitleEt = (EditText) findViewById( R.id.blogSubtitle );

        title = titleEt.getText().toString();
        String subtitle = subtitleEt.getText().toString();
        String type = "jekyll";
        String theme = "spacelab";

        // Hit Teddy Hyde and build out the blog
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setServer("https://teddyhyde.com")
                .build();

        TeddyHydeService service = restAdapter.create(TeddyHydeService.class);

        service.create(token, title, subtitle, type, theme, new Callback<TeddyHydeService.Blog>() {
            @Override
            public void success(TeddyHydeService.Blog blog, Response response) {
                finishWithResult(blog);
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                finishWithResult(null);
            }
        });
    }

    private class GetNewToken extends AsyncTask<Void, Void, Boolean> {

        private class GitHubConstants {
            public static final String CLIENT_ID = "1bbba465d624d41b2cfd";

            public static final String CLIENT_SECRET = "9c5fbfce51c4740a25218f64f2b4cf1beb8e3654";

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
            builder.setScopes(Arrays.asList("repo"));

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
            token = credential.getAccessToken();
            sp.edit().putString( BLOG_CREATOR_TOKEN, token ).commit();
            createBlogOnServer();
        }
    }

}
