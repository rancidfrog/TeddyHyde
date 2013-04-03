package com.EditorHyde.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.util.ArrayList;
import java.util.List;

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
        bundle.putString("repo", repo.getName());
        bundle.putString( "login", repo.getOwner().getLogin() );
        i.putExtras(bundle);
        startActivity(i);

    }

    RepoListAdapter adapter;
    private ProgressDialog pd;
    String authToken;
    ListView listView;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.repo_list);

        ctx = this;

        listView = (ListView) findViewById(R.id.listView);

        SharedPreferences sp = this.getSharedPreferences( MainActivity.APP_ID, MODE_PRIVATE);
        authToken = sp.getString("authToken", null);

        pd = ProgressDialog.show( this, "", "Loading all repositories...", true);

        new GetReposTask().execute();

    }

    private class GetReposTask extends AsyncTask<Void, Void, Boolean> {

        List<Repository> allRepos;

        protected Boolean doInBackground(Void...voids) {
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


            ArrayList<Repository> nonJekyll = new ArrayList<Repository>();
            ArrayList<Repository> possibleJekyll = new ArrayList<Repository>();
            for( int j = 0; j < repos.size(); j++ ) {

                Repository repo = repos.get(j);
                String name = repo.getName();

                if( name.contains( "github.com" ) || name.endsWith( ".com") )    {
                    possibleJekyll.add( repo );

                }
                else {
                    nonJekyll.add( repo );
                }
            }

            possibleJekyll.addAll( nonJekyll);
            allRepos = possibleJekyll;
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
            }

        }
    }
}