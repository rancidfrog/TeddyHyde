package com.EditorHyde.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import org.eclipse.egit.github.core.*;

import org.eclipse.egit.github.core.service.*;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Created with IntelliJ IDEA.
 * User: xrdawson
 * Date: 3/12/13
 * Time: 11:41 AM
 * To change this template use File | Settings | File Templates.
 */
public class FileListingActivity extends Activity {

    private static final String MARKDOWN_EXTENSION = ".md";
    private static final int EDIT_EXISTING_FILE = 1;
    private static final int EDIT_NEW_FILE = 2;
    private ProgressDialog pd;
    private Cwd cwd;
    Tree repoTree;
    FileListAdapter adapter;
    private List<TreeEntry> values;
    String repoName;
    String authToken;
    String login;
    List<TreeEntry> entries;
    Context ctx;
    Repository theRepo;
    TextView repoTv;
    Button branchTv;
    String transformsJson;
    TreeEntry currentTree;
    String baseUrl;

    RepositoryBranch theBranch;

    @Override
    public void onBackPressed() {
        Log.d(MainActivity.logname, "onBackPressed Called");
        if( cwd.atRoot()) {
            finish();
        }
        else {
            ascend();
            rebuildFilesList();
        }

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String newSha = data.getExtras().getString( "sha" );
        String newPath = data.getExtras().getString( "path" );

        switch (requestCode) {
            case EDIT_EXISTING_FILE:
                // update the tree with the new sha
                String oldSha = currentTree.getSha();
                currentTree.setSha(newSha);
                break;
            case EDIT_NEW_FILE:
                // Create a new file in the tree
                TreeEntry newFile = new TreeEntry();
                newFile.setSha( newPath );
                newFile.setPath( newPath );
                newFile.setType( "blob" );
                repoTree.getTree().add(newFile);
                rebuildFilesList();
        }

    }

    private void promptForFilename( final String root, final String prefix, final String template, final String type ) {
        // Set an EditText view to get user input
        final String[] filename = new String[1];
        final LinearLayout ll = new LinearLayout(FileListingActivity.this);
        ll.setOrientation(LinearLayout.VERTICAL);
        final EditText input = new EditText(FileListingActivity.this);
        final TextView finalFilename = new TextView(FileListingActivity.this);
        ll.addView(finalFilename);
        ll.addView(input);
        final String[] processedTemplate = new String[1];

        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Editable text = input.getText();
                String title = text.toString();
                // Don't replace non-word at the end of the string.
                String whitespaceStripped = title.toLowerCase().replaceAll( "\\W+", "-").replaceAll( "\\W+$", "" );

                filename[0] = root + prefix + whitespaceStripped + MARKDOWN_EXTENSION;

                processedTemplate[0] = MarkupUtilities.process( template, "TITLE", title );

                finalFilename.setText( "Filename: " + filename[0] );
            }

            @Override
            public void afterTextChanged(Editable s) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });

        AlertDialog show = new AlertDialog.Builder(FileListingActivity.this)
                .setTitle(type + " title")
                .setMessage("Provide a title for your " + type.toLowerCase())
                .setView(ll)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Convert it to proper format
                        loadEditor(processedTemplate[0], filename[0], repoName, null );
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Do nothing.
                    }
                }).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        String filename;
        String template;

        switch ( itemId ) {

            case R.id.action_add_new_page:
                template = getString(R.string.page_template);
                promptForFilename( cwd.getFullPathWithTrailingSlash(), "", template, "Page" );

                return true;

            case R.id.action_add_new_post:
                cwd.descendTo("_posts");
                SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd-" );
                String prefix = sdf.format( new Date() );

                template = getString(R.string.post_template);
                promptForFilename("_posts/", prefix, template, "Post");

                // create a new post
                return true;

            case R.id.action_upload_image:
                Intent i;
                Bundle extras = getIntent().getExtras();
                extras.putString( "repo", repoName );
                extras.putString( "login", login );
                i = new Intent(this, PixActivity.class);
                i.putExtras(extras);
                startActivity( i );

        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.activity_file_listing, menu);
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        ctx = this;
        cwd = new Cwd();
        values = new ArrayList<TreeEntry>();
        setContentView(R.layout.file_list);

        SharedPreferences sp = this.getSharedPreferences( MainActivity.APP_ID, MODE_PRIVATE);
        authToken = sp.getString("authToken", null);

        Bundle extras = getIntent().getExtras();
        repoName = extras.getString("repo");
        login = extras.getString("login", null );
        repoTv = (TextView)findViewById(R.id.repoName);
        repoTv.setText( repoName );

        branchTv = (Button)findViewById(R.id.branchName);

        branchTv.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Allow user to choose the branch...
                // NYI

            }
        });
        pd = ProgressDialog.show( this, "", "Loading repository data..", true);

        new GetRepoFiles().execute();

    }


    class LoadImageTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... unused) {
            // Find all images in the repository
            // Only add items at the root for now
            String cnameContents = null;
            baseUrl = repoName;
            ArrayList<String> images = new ArrayList<String>();

            if( null != entries ) {
                for( TreeEntry entry: entries) {

                    String name = entry.getPath();

                    // Get the CNAME file. If not there, use the repository name as the URL
                    if( name.equals( "CNAME" ) ) {
                        try {
                            String fileSha = entry.getSha();
                            cnameContents = ThGitClient.GetFile(authToken, repoName, login, fileSha);
                        } catch (IOException e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }
                    }

                }
            }

            if( null != cnameContents) {
                baseUrl = cnameContents.trim();
            }

            RemoteFileCache.setHttpRoot( "http://" + baseUrl + "/" );

            if( null != entries ) {
                for( TreeEntry entry: entries) {

                    String name = entry.getPath();

                    if( name.contains( "assets/images") && name.contains( "thumb") ) {
                        images.add( name );
                    }
                }
            }

            RemoteFileCache.loadImageUriReferences( images );
            return true;
        }


        @Override
        protected void onPostExecute(Boolean result) {
            if (pd.isShowing())
                pd.dismiss();

            showFiles(values);
        }
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
                    currentTree = treeEntry;
                    showEditor( file, fileSha );
                }
            }
        });
    }

    private void filterArray() {
        values.clear();

        if( null != adapter ) {
            adapter.setFrontPath( cwd.getFullPathWithTrailingSlash() );
        }

        // Only add items at the root for now
        for( TreeEntry entry: entries) {

            String type = entry.getType();
            String name = entry.getPath();

            String path = cwd.getFullPathWithTrailingSlash();
            if( -1 != name.indexOf( path )  ) {

                int slashesInCwd = countOccurrences( path, '/' );
                int slashesInFile = countOccurrences( name, '/' );

                if( slashesInCwd == slashesInFile ) {
                    // if posts, sort in reverse order
                    if( 0 == path.compareTo( "_posts/") ) {
                        values.add( 0, entry );
                    }
                    else {
                        values.add( entry );
                    }
                }

            }
        }
    }

    // Thanks: http://stackoverflow.com/questions/275944/how-do-i-count-the-number-of-occurrences-of-a-char-in-a-string
    public static int countOccurrences(String haystack, char needle)
    {
        int count = 0;
        for (int i=0; i < haystack.length(); i++)
        {
            if (haystack.charAt(i) == needle)
            {
                count++;
            }
        }
        return count;
    }

    private void rebuildFilesList() {
        // filter out those with the proper path
        filterArray();

        // Update the repository name
        String theCwd = cwd.getFullPathWithTrailingSlash();
        String repoPlusCwd = repoName;
        if( !theCwd.equals("") ) {
            repoPlusCwd += "/" + theCwd;
        }
        repoTv.setText( repoPlusCwd  );
        branchTv.setText( theBranch.getName() );

        // update the list
        adapter.notifyDataSetChanged();
    }

    private void descend( String directory ) {
        cwd.descendTo(directory);
    }

    private void ascend() {
        cwd.ascendOne();
    }

    private class GetRepoFiles extends AsyncTask<Void, Void, Boolean> {

        List<User> users;

        protected Boolean doInBackground(Void...unused) {

            Boolean rv = true;

            RepositoryService repositoryService = new RepositoryService();
            repositoryService.getClient().setOAuth2Token(authToken);

            try {
                CommitService cs = new CommitService();
                cs.getClient().setOAuth2Token(authToken);
                theRepo = repositoryService.getRepository(login, repoName);

                List<RepositoryBranch> branches = repositoryService.getBranches(theRepo);
                theBranch = null;
                RepositoryBranch master = null;
                // Iterate over the branches and find gh-pages or master
                for( RepositoryBranch i : branches ) {
                    String theName = i.getName().toString();
                    if( theName.equalsIgnoreCase("gh-pages") ) {
                        theBranch = i;
                    }
                    else if( theName.equalsIgnoreCase("master") ) {
                        master = i;
                    }
                }
                if( null == theBranch ) {
                    theBranch = master;
                }

                String baseCommitSha = theBranch.getCommit().getSha();
                DataService ds = new DataService();
                ds.getClient().setOAuth2Token(authToken);
                repoTree = ds.getTree( theRepo, baseCommitSha, true );
                entries  = repoTree.getTree();
                filterArray();

                // See if we have multiple collaborators, and if so, warn the user
                // theRepo.
                CollaboratorService cos = new CollaboratorService();
                cos.getClient().setOAuth2Token(authToken);

                try {
                    users = cos.getCollaborators(theRepo);
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }

            } catch (Exception e) {
                e.printStackTrace();
                rv = false;
            }

            return rv;
        }

        protected void onPostExecute(Boolean result) {
            // Determine the images, and load them
            pd.setMessage( "Loading hyde transformations...");

            if( null != theBranch ) {
                branchTv.setText( theBranch.getName() );
                branchTv.setVisibility(View.VISIBLE);
            }

            new LoadHydeTransformsTask().execute();

            // This does not work. Why?
            if( null != users && users.size() > 1 ) {
                Toast.makeText( FileListingActivity.this, "WARNING: This repository has multiple collaborators. Teddy Hyde does not always detect recent file changes made via other collaborators. (bug #1)", Toast.LENGTH_LONG );
            }
        }
    }


    private class LoadHydeTransformsTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void...unused) {

            String transformsSha = null;
            transformsJson = null;

            if( null != entries ) {
                for( TreeEntry entry: entries ) {

                    String name = entry.getPath();

                    if( name.equals( "_hyde/transforms.json") ) {
                        transformsSha = entry.getSha();
                    }
                }

                if( null != transformsSha ) {
                    try {
                        transformsJson = ThGitClient.GetFile( authToken, repoName, login, transformsSha );
                    } catch (IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            }

            return true;

        }

        @Override
        protected void onPostExecute(Boolean result) {
            new LoadImageTask().execute();
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
        String fileSha;

        protected Boolean doInBackground(String...strings) {

            Boolean rv = true;
            String username = strings[0];
            String authToken = strings[1];
            String repoName = strings[2];
            theFilename = strings[3];
            fileSha = strings[4];

            try {
                theMarkdown = ThGitClient.GetFile(authToken, repoName, username, fileSha);
            } catch (IOException e) {
                e.printStackTrace();
                rv = false;
            }

            return rv;
        }

        protected void onPostExecute(Boolean result) {
            pd.hide();
            loadEditor( theMarkdown, theFilename, repoName, fileSha  );
        }
    }

    private void loadEditor( String theMarkdown, String theFilename, String repoName, String sha ) {
        Intent i;
        i = new Intent(ctx, ScreenSlideActivity.class);
        Bundle extras = getIntent().getExtras();
        extras.putString( "markdown", theMarkdown );
        extras.putString( "filename", theFilename );
        extras.putString( "repo", repoName );
        extras.putString( "login", login );
        extras.putString( "transforms", transformsJson );
        extras.putString( "sha", sha );

        i.putExtras(extras);

        startActivityForResult(i, null != sha ? EDIT_EXISTING_FILE : EDIT_NEW_FILE );


    }
}