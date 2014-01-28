package com.EditorHyde.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.roscopeco.ormdroid.ORMDroidApplication;

import org.eclipse.egit.github.core.Authorization;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.service.OAuthService;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created with IntelliJ IDEA.
 * User: xrdawson
 * Date: 3/12/13
 * Time: 11:04 AM
 * To change this template use File | Settings | File Templates.
 */
public class RepoListActivity extends Activity {

    final int NEW_BLOG_RESULT = 1;
    Context ctx;
    int retryCount = 0;

    public void showFilesList( Repository repo ) {
        Intent i = new Intent(this, FileListingActivity.class);

        Bundle bundle = new Bundle();
        String repoName = repo.getName();
        String login = repo.getOwner().getLogin();
        bundle.putString( "repo", repoName );
        bundle.putString( "login", login );
        i.putExtras(bundle);
        startActivity(i);

    }

    RepoListAdapter adapter;
    private ProgressDialog pd;
    String authToken;
    ListView listView;
    SharedPreferences sp;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if( requestCode == NEW_BLOG_RESULT ) {
            retryCount = 0;
            String blogId = data.getStringExtra( "blog_id" );
            String blogTitle = data.getStringExtra( "blog_title" );
            if( !blogId.equals( "-1" ) ) {
                displayNewBlogStatus( true, blogTitle );
                new GetRepoStatus().execute( blogId, blogTitle );
            }
        }
    }

    private void displayNewBlogStatus( boolean onOff, String title ) {
        RelativeLayout rl = (RelativeLayout)findViewById( R.id.newBlogStatus );
        TextView tv = (TextView)findViewById( R.id.newBlogTitle );
        if( null != rl && null != tv ) {
            if( null != title ) {
            tv.setText( "Verifying status of new blog \"" + title + "\"..." );
            }
            rl.setVisibility( onOff ? View.VISIBLE : View.GONE );
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.repo_list);

        ctx = this;

        Button newRepoButton = (Button) findViewById(R.id.new_blog_button );

        newRepoButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent( ctx, BlogCreatorActivity.class);

                startActivityForResult( i, NEW_BLOG_RESULT );
            }
        });

        listView = (ListView) findViewById(R.id.listView);

        sp = this.getSharedPreferences( MainActivity.APP_ID, MODE_PRIVATE);
        authToken = sp.getString("authToken", null);

        RemoteFileCache.clear();

        pd = ProgressDialog.show( this, "", getString(R.string.loading_all_repositories), true);
        ORMDroidApplication.initialize(this);
        new GetReposTask().execute();

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


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        int groupId = item.getGroupId();
        boolean rv = false;

        if( itemId == R.id.action_logout ) {
            pd = ProgressDialog.show( this, "", "Logging out from GitHub...", true);
            new LogoutTask().execute();
        }
        else if( itemId == R.id.action_scratchpad ) {
            Intent i = new Intent(this, ScratchpadActivity.class);
            startActivity(i);
        }

        return rv;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    private class GetRepoStatus extends AsyncTask<String, Void, Boolean> {

        String theId = null;
        String theTitle = null;

        protected Boolean doInBackground(String... args) {

            theId = args[0];
            theTitle = args[1];

            final Boolean[] rv = {false};

            // Hit Teddy Hyde and build out the blog
            RestAdapter restAdapter = new RestAdapter.Builder()
                    .setServer("https://teddyhyde.com")
                    .build();

            TeddyHydeService service = restAdapter.create(TeddyHydeService.class);

            service.status( theId, new Callback<TeddyHydeService.Blog>() {
                @Override
                public void success(TeddyHydeService.Blog blog, Response response) {
                    if( "success".equals( blog.status ) ) {
                        rv[0] = true;
                    }
                    else {
                        rv[0] = false;
                    }
                }

                @Override
                public void failure(RetrofitError retrofitError) {


                }
            });

            return rv[0];
        }

        protected void onPostExecute(Boolean result) {
            if( result ) {
                displayNewBlogStatus( false, theTitle );
            }
            else {
                if( retryCount < 20 ) {
                retryCount++;
                    Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        new GetRepoStatus().execute( theId, theTitle );
                    }
                }, 5000);
                }
                else {
                 displayNewBlogStatus( false, null );
                }
            }
        }

    }



    private class GetReposTask extends AsyncTask<Void, Void, Boolean> {

        List<Repository> allRepos;
        Set<String> repositorySet;

        protected Boolean doInBackground(Void...voids) {
            repositorySet = new HashSet<String>();
            Boolean rv = true;
            List<Repository> repos = null;

            RepositoryService service = new RepositoryService();
            service.getClient().setOAuth2Token(authToken);
            try {
                repos = service.getRepositories();
            }
            catch( Exception e) {
                e.printStackTrace();
                rv = false;
            }

            if( rv ) {
                ArrayList<Repository> nonJekyll = new ArrayList<Repository>();
                ArrayList<Repository> possibleJekyll = new ArrayList<Repository>();
                for( int j = 0; j < repos.size(); j++ ) {

                    Repository repo = repos.get(j);
                    String name = repo.getName();

                    int length = name.length();
                    if( name.indexOf( "." ) != -1 && ( ( length-4 == name.lastIndexOf(".") ) || length-3 == name.lastIndexOf(".") ) ) {
                        possibleJekyll.add( repo );
                    }
                    else {
                        nonJekyll.add( repo );
                    }

                    repositorySet.add( name );
                }

                possibleJekyll.addAll( nonJekyll);
                allRepos = possibleJekyll;
            }
            return rv;

        }


        protected void onPostExecute(Boolean result) {
            pd.hide();

            if( result ) {
                adapter = new RepoListAdapter(ctx, allRepos );
                listView.setAdapter(adapter);

                listView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Repository repo = (Repository)adapterView.getItemAtPosition(i);
                        showFilesList(repo);
                    }
                });

                SharedPreferences.Editor editor = sp.edit();
                editor.putStringSet(getString(R.string.cached_repositories), repositorySet );
                editor.commit();
            }

        }
    }



    @Override
    public void onPause(){

        super.onPause();
        if(pd != null) {
            pd.dismiss();
        }
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