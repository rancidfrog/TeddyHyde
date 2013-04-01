package com.EditorHyde.app;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import org.apache.commons.codec.binary.Base64;

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
    private List<String> cwd;
    Tree repoTree;
    FileListAdapter adapter;
    private List<TreeEntry> values;
    String repoName;
    List<TreeEntry> entries;
    Context ctx;

    @Override
    public void onBackPressed() {
        Log.d("com.EditorHyde.app", "onBackPressed Called");
        if( cwd.isEmpty()) {

            Intent i;
            i = new Intent(this, RepoListActivity.class);
            startActivity(i);
        }
        else {
            ascend();
            rebuildFilesList();
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        ctx = this;
        cwd = new ArrayList<String>();
        values = new ArrayList<TreeEntry>();
        setContentView(R.layout.file_list);
        SharedPreferences sp = this.getSharedPreferences( MainActivity.APP_ID, MODE_PRIVATE);
        String authToken = sp.getString("authToken", null);
        String username = sp.getString("email", null);
        String name = sp.getString("name", null );
        String login = sp.getString("login", null );

        Bundle extras = getIntent().getExtras();
        repoName = extras.getString("repo");

        pd = ProgressDialog.show( this, "", "Loading repository data..", true);

        new GetRepoFiles().execute( login, authToken, repoName );
    }



    private void showFiles( List<TreeEntry> files ) {

        ListView listView;
        listView = (ListView) findViewById(R.id.repoFilesList);
        adapter = new FileListAdapter( this, files );
        listView.setAdapter(adapter);
        listView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                TreeEntry treeEntry = (TreeEntry) adapterView.getItemAtPosition(i);
                String type = treeEntry.getType();
                String file = treeEntry.getPath();

                if( 0 == "tree".compareTo( type  )) {
                    descend(file);
                    rebuildFilesList();
                }
                else {
                    String fileSha = treeEntry.getSha();
                    showEditor( file, fileSha );
                }
            }
        });
    }

    private void filterArray() {
        values.clear();

        // build up the path
        String path = "";
        for( String dir : cwd ) {
            path +=  dir + "/";
        }

        // Only add items at the root for now
        for( TreeEntry entry: entries) {

            String type = entry.getType();
            String name = entry.getPath();

            if( cwd.isEmpty()) {
                // Only look for items without a slash in them, at the root
                if( -1 == name.indexOf( "/" )  ) {
                    values.add( entry );
                }
            }
            else {
                if( -1 != name.indexOf( path )  ) {
                    values.add( entry );
                }
            }
        }
    }


    private void rebuildFilesList() {
        // filter out those with the proper path
        filterArray();
        adapter.notifyDataSetChanged();
    }

    private void descend( String directory ) {
        cwd.add(directory);
    }

    private void ascend() {
        cwd.remove(cwd.size()-1);
    }

    private class GetRepoFiles extends AsyncTask<String, Void, Boolean> {


        protected Boolean doInBackground(String...strings) {

            Boolean rv = true;
            String username = strings[0];
            String authToken = strings[1];
            String repoName = strings[2];

            RepositoryService repositoryService = new RepositoryService();
            repositoryService.getClient().setOAuth2Token(authToken);
            Repository repository;

            try {
                CommitService cs = new CommitService();
                cs.getClient().setOAuth2Token(authToken);
                Repository repo = repositoryService.getRepository(username, repoName);

                List<RepositoryBranch> branches = repositoryService.getBranches(repo);
                RepositoryBranch theBranch = null;
                RepositoryBranch master = null;
                // Iterate over the branches and find gh-pages or master
                for( RepositoryBranch i : branches ) {
                    String theName = i.getName().toString();
                    if( theName.equalsIgnoreCase( "gh-pages" ) ) {
                        theBranch = i;
                    }
                    else if( theName.equalsIgnoreCase( "master") ) {
                        master = i;
                    }
                }
                if( null == theBranch ) {
                    theBranch = master;
                }

                String baseCommitSha = theBranch.getCommit().getSha();
                RepositoryCommit baseCommit = cs.getCommit(repo, baseCommitSha);
                String treeSha = baseCommit.getSha();
                PageIterator<RepositoryCommit> pager = cs.pageCommits( repo, 1 );
                Collection<RepositoryCommit> commits = pager.next();
                RepositoryCommit rc = null;
                for( RepositoryCommit commit: commits) {
                    rc = commit;
                }

                String sha = rc.getSha();
                DataService ds = new DataService();
                ds.getClient().setOAuth2Token(authToken);
                repoTree = ds.getTree( repo, sha, true );
                entries  = repoTree.getTree();
                filterArray();

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


    public void showEditor( String filename, String fileSha ) {

        SharedPreferences sp = this.getSharedPreferences( MainActivity.APP_ID, MODE_PRIVATE);
        String authToken = sp.getString("authToken", null);
        String login = sp.getString("login", null );

        pd = ProgressDialog.show( this, "", "Loading file data...", true);

        new GetFileTask().execute( login, authToken, repoName, filename, fileSha );

    }


    private class GetFileTask extends AsyncTask<String, Void, Boolean> {

        String theMarkdown;
        String theFilename;

        protected Boolean doInBackground(String...strings) {

            Boolean rv = true;
            String username = strings[0];
            String authToken = strings[1];
            String repoName = strings[2];
            theFilename = strings[3];
            String fileSha = strings[4];

            try {
                RepositoryService repositoryService = new RepositoryService();
                repositoryService.getClient().setOAuth2Token(authToken);
                Repository repo = repositoryService.getRepository(username, repoName);
                DataService ds = new DataService();
                ds.getClient().setOAuth2Token(authToken);
                Blob blob = ds.getBlob(repo, fileSha);
                String theMarkdown64 = blob.getContent();
                String encoding = blob.getEncoding();
                byte[] decoded = Base64.decodeBase64(theMarkdown64.getBytes());
                theMarkdown = new String( decoded );


            } catch (IOException e) {
                e.printStackTrace();
                rv = false;
            }

            return rv;
        }

        protected void onPostExecute(Boolean result) {
            pd.hide();
            Intent i;
            i = new Intent(ctx, ScreenSlideActivity.class);
            Bundle extras = getIntent().getExtras();
            extras.putString( "markdown", theMarkdown );
            extras.putString( "filename", theFilename );
            extras.putString( "repo", repoName );

            i.putExtras(extras);
            startActivity(i);
        }
    }
}