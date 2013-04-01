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

    public void showFilesList( String repo ) {
        Intent i = new Intent(this, FileListingActivity.class);

        Bundle bundle = new Bundle();
        bundle.putString("repo", repo);
        i.putExtras(bundle);

        startActivity(i);

    }

    ArrayAdapter<String> adapter;
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

        List<String> repoNames;

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

            repoNames = new ArrayList<String>();
            for( int j = 0; j < repos.size(); j++ ) {
                Repository repo = repos.get(j);
                String name = repo.getName();
                repoNames.add(name);
            }

            return rv;

        }


        protected void onPostExecute(Boolean result) {
            pd.hide();

            if( result ) {
                adapter = new ArrayAdapter<String>(ctx,
                        android.R.layout.simple_dropdown_item_1line, android.R.id.text1, repoNames);
                listView.setAdapter(adapter);

                listView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        String repo = (String)adapterView.getItemAtPosition(i);
                        showFilesList(repo);
                    }
                });
            }

        }
    }
}