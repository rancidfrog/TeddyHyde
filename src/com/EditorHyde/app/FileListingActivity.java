package com.EditorHyde.app;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import org.eclipse.egit.github.core.*;

import org.eclipse.egit.github.core.client.*;
import org.eclipse.egit.github.core.service.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;


/**
 * Created with IntelliJ IDEA.
 * User: xrdawson
 * Date: 3/12/13
 * Time: 11:41 AM
 * To change this template use File | Settings | File Templates.
 */
public class FileListingActivity extends Activity {

    private ProgressDialog pd;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_list);
        SharedPreferences sp = this.getSharedPreferences( MainActivity.APP_ID, MODE_PRIVATE);
        String authToken = sp.getString("authToken", null);
        String username = sp.getString("email", null);
        String name = sp.getString("name", null );
        String login = sp.getString("login", null );


        Bundle extras = getIntent().getExtras();
        String repoName = extras.getString("repo");

        pd = ProgressDialog.show( this, "", "Loading repository data..", true);

        new GetRepoFiles().execute( login, authToken, repoName );
    }

    private void showFiles( List<TreeEntry> files ) {

        ListView listView;
        listView = (ListView) findViewById(R.id.repoFilesList);

        FileListAdapter adapter = new FileListAdapter( this, files );
        listView.setAdapter(adapter);
        listView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                showEditor();
            }
        });
    }

    private class GetRepoFiles extends AsyncTask<String, Void, Boolean> {
        private List<TreeEntry> values;

        protected Boolean doInBackground(String...strings) {


            Boolean rv = true;
            String username = strings[0];
            String authToken = strings[1];
            String repoName = strings[2];

            RepositoryService repositoryService = new RepositoryService();
            repositoryService.getClient().setOAuth2Token(authToken);
            String master = "";

            Repository repository;

            try {
                CommitService cs = new CommitService();
                cs.getClient().setOAuth2Token(authToken);
                Repository repo = repositoryService.getRepository(username, repoName);
//                // Get first commit
                PageIterator<RepositoryCommit> pager = cs.pageCommits( repo, 1 );
                Collection<RepositoryCommit> commits = pager.next();
                RepositoryCommit rc = null;
                for( RepositoryCommit commit: commits) {
                    rc = commit;
                }

                String sha = rc.getSha();
                DataService ds = new DataService();
                ds.getClient().setOAuth2Token(authToken);
                Tree tree = ds.getTree( repo, sha, true );

                List<TreeEntry> entries  = tree.getTree();

                values = new ArrayList<TreeEntry>();
                for( TreeEntry entry: entries) {
                 values.add(entry);
                }


            } catch (Exception e) {
                e.printStackTrace();
                rv = false;
            }

            return rv;
        }

        protected void onPostExecute(Boolean result) {
            pd.hide();
            showFiles( values );
        }

    }


    public void showEditor() {
        Intent i;
        i = new Intent(this, ScreenSlideActivity.class);
        startActivity(i);
    }

}