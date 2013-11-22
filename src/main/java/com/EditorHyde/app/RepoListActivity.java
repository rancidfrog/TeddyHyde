package com.EditorHyde.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.analytics.tracking.android.EasyTracker;

import org.eclipse.egit.github.core.Authorization;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.service.OAuthService;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.simonvt.menudrawer.*;

/**
 * Created with IntelliJ IDEA.
 * User: xrdawson
 * Date: 3/12/13
 * Time: 11:04 AM
 * To change this template use File | Settings | File Templates.
 */
public class RepoListActivity extends Activity {

    Context ctx;

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

    private MenuDrawer mDrawer;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.repo_list);

        mDrawer = MenuDrawer.attach(this);
        mDrawer.setContentView(R.layout.activity_sample);
        mDrawer.setMenuView(R.layout.menu_sample);

        ctx = this;

        listView = (ListView) findViewById(R.id.listView);

        sp = this.getSharedPreferences( MainActivity.APP_ID, MODE_PRIVATE);
        authToken = sp.getString("authToken", null);

        RemoteFileCache.clear();

        pd = ProgressDialog.show( this, "", getString(R.string.loading_all_repositories), true);

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