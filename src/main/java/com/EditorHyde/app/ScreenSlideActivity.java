/*
 * Copyright 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.EditorHyde.app;

import android.app.*;
import android.content.*;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.eclipse.egit.github.core.*;
import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

import java.util.*;

/**
 * Demonstrates a "screen-slide" animation using a {@link ViewPager}. Because {@link ViewPager}
 * automatically plays such an animation when calling {@link ViewPager#setCurrentItem(int)}, there
 * isn't any animation-specific code in this sample.
 *
 * <p>This sample shows a "next" button that advances the user to the next step in a wizard,
 * animating the current screen out (to the left) and the next screen in (from the right). The
 * reverse animation is played when the user presses the "previous" button.</p>
 *
 * @see ScreenSlidePageFragment
 */
public class ScreenSlideActivity extends FragmentActivity {
    /**
     * The number of pages (wizard steps) to show in this demo.
     */
    private static final int NUM_PAGES = 2;
    private static final int CHOOSE_IMAGE = 1 ;
    private static final int CHOOSE_IMAGE_TRANSFORM = 2;
    Tree repoTree;
    ProgressBar pb;
    ScreenSlidePageFragment editorFragment;
    String lastGistUrl = null;

    private static final int HYDE_TRANSFORMS_GROUP_ID = 1;

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager mPager;

    String theMarkdown;
    String theFile;
    String theRepo;
    String authToken;
    String theLogin;
    String theTransforms;
    String theSha;
    String[] images;
    String scratchId;
    Boolean isScratchpad;
    List<Transform> transforms;

    ScreenSlidePageFragmentMarkup md;
    ProgressDialog pd;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        theMarkdown = extras.getString("markdown");
        isScratchpad = extras.getBoolean( "scratchpad" );
        theLogin = extras.getString("login");

        if( isScratchpad ) {
            scratchId = extras.getString("scratch_id");
        }
        else {
            theFile = extras.getString("filename");
            theRepo = extras.getString("repo");
            theTransforms = extras.getString( "transforms" );
            theSha = extras.getString( "sha" );
        }

        SharedPreferences sp = this.getSharedPreferences( MainActivity.APP_ID, MODE_PRIVATE);
        authToken = sp.getString("authToken", null);

        setContentView(R.layout.activity_screen_slide);

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // When changing pages, reset the action bar actions since they are dependent
                // on which page is currently active. An alternative approach is to have each
                // fragment expose actions itself (rather than the activity exposing actions),
                // but for simplicity, the activity provides the actions in this sample.
                invalidateOptionsMenu();

                if( 1 == position && null != md  ) {
                    md.onPageSelected(position);
                }
            }
        });
    }

    private void loadHydeTransformsIntoMenu( Menu menu ) {
        int index = 0;

        if( null != theTransforms ) {
            Gson gson = new Gson();
            List<Map<String,String>> objects = gson.fromJson(theTransforms, new TypeToken<List<Map<String, String>>>() {
            }.getType());

            transforms = new ArrayList<Transform>();
            for( Map tr: objects ) {
                Transform transform = new Transform();
                transform.code = (String)tr.get( "code" );
                transform.type = (String)tr.get( "type" );
                transform.prompt = (String)tr.get( "prompt" );
                transform.name = (String)tr.get( "name" );
                transform.version = Integer.parseInt( (String)tr.get( "version" ) );
                if( transform.version == 1 ) {
                    transforms.add( transform );
                }
                else {
                    Log.d(MainActivity.logname, "Unsupported transform version");
                }
            }

            if( !transforms.isEmpty() )       {
                SubMenu hydeMenu = menu.addSubMenu("Hyde Transform...");

                for( Transform item : transforms ) {
                    hydeMenu.add(HYDE_TRANSFORMS_GROUP_ID, index, index, item.name);
                    index++;
                }

            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.activity_screen_slide, menu);
        boolean isNumberOne = mPager.getCurrentItem() == 0;

        if( !isScratchpad ){
            // If we are in the editor menu, then enable the save with commit message...
            menu.findItem(R.id.action_save_with_commit).setEnabled( isNumberOne ).setVisible( isNumberOne );
            menu.findItem(R.id.action_save_file).setEnabled(isNumberOne).setVisible(isNumberOne);
            loadHydeTransformsIntoMenu( menu );
        }
        else {
            menu.findItem(R.id.action_save_as_gist).setEnabled(isNumberOne).setVisible(isNumberOne);
            menu.findItem(R.id.action_save_into_repository).setEnabled(isNumberOne).setVisible(isNumberOne);
            menu.findItem(R.id.action_close_scratchpad).setEnabled(isNumberOne).setVisible(isNumberOne);
            if( null != lastGistUrl) {
                menu.findItem(R.id.action_share_last_gist).setEnabled( isNumberOne ).setVisible( isNumberOne );
            }
        }

        return true;
    }

    private void promptForCommitMessage( final String contents ) {
        // Set an EditText view to get user input
        final LinearLayout ll = new LinearLayout(ScreenSlideActivity.this);
        ll.setOrientation(LinearLayout.VERTICAL);
        final EditText input = new EditText(ScreenSlideActivity.this);
        final CheckBox cb = new CheckBox(ScreenSlideActivity.this);
        cb.setText( "append 'Teddy Hyde' to end of commit ");
        cb.setChecked( true );
        ll.addView( input );
        ll.addView( cb );

        new AlertDialog.Builder(ScreenSlideActivity.this)
                .setTitle("Commit message")
                .setMessage( "Enter your commit message: ")
                .setView(ll)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        startSaveProgressIndicator();
                        Editable text = input.getText();
                        String message = text.toString();

                        if (cb.isChecked()) {
                            message += " (edited by Teddy Hyde teddyhyde.com)";
                        }

                        // deal with the editable
                        new SaveFileTask().execute( contents, message);

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Do nothing.
                    }
                }).show();
    }

    private void startSaveProgressIndicator() {
        pb = (ProgressBar) findViewById(R.id.editProgressBar);
        pb.setVisibility(View.VISIBLE);
    }

    private void pasteCode( String preprocessed ) {
        String code = preprocessed.replaceAll( "\n", "\n    ");
        insertAtCursor("\n    " + code + "\n");
    }

    private void insertAtCursor( String text )  {
        EditText et = (EditText) findViewById(R.id.markdownEditor);
        int start = et.getSelectionStart();
        int end = et.getSelectionEnd();
        et.getText().replace(Math.min(start, end), Math.max(start, end),
                text, 0, text.length());
    }

    private void pasteLink( final String preprocessed ) {

        final String link = preprocessed.replaceAll( "\\)", "\\)");

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Link text");
        alert.setMessage("Enter link text, or leave blank to use link as text");

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String name = input.getText().toString();
                if( name == null || name == "" ) {
                    name = link;
                }
                // make sure first line is also done
                insertAtCursor( "[" + name + "](" + link + ")" );
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();
    }

    private void pasteQuote( String preprocessed ) {
        String quote = preprocessed.replaceAll( "\n", "\n> ");
        // make sure first line is also done
        insertAtCursor( "\n> " + quote + "\n" );
    }

    private void promptAndInsert( final Transform transform, final String imageUrl ) {
        final EditText input = new EditText(ScreenSlideActivity.this);

        new AlertDialog.Builder(ScreenSlideActivity.this)
                .setTitle("Insert image code")
                .setMessage( transform.prompt )
                .setView(input)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String processed = MarkupUtilities.process(transform.code, "IMAGE", imageUrl);
                        processed = MarkupUtilities.process(processed, "PROMPT", input.getText().toString() );
                        insertAtCursor(processed);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Do nothing.
                    }
                }).show();
    }

    private String getScaledImage( String url, String scaled ) {
        String replaced;
        replaced = url.replace(".png", "-" + scaled + ".png");
        return replaced;
    }

    private void finishWithResult() {
        Intent intent = new Intent();
        Bundle extras = new Bundle();

        if( !isScratchpad) {
            extras.putString( "sha", theSha );
            extras.putString( "path", theFile);
        }
        else {
            extras.putString( "scratch", theMarkdown );
            extras.putString( "scratch_id", scratchId );
        }
        intent.putExtras(extras);

        setResult(RESULT_OK, intent );

        finish();
    }

    @Override
    public void onBackPressed() {

        if( editorFragment.isDirty() ) {
            // Notify the user they will lose work...
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle( "Unsaved work")
                    .setMessage("You have not saved your changes. If you leave this page, you will lose your changes since the last save. Do you want to return to the page to save your edits?")
                    .setPositiveButton( "Yes, return to editing", null )
                    .setNegativeButton( "No, discard my edits", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finishWithResult();
                        }
                    } )
                    .show();
        }
        else {
            finishWithResult();
        }
    }

    @Override
    public void onActivityResult( int reqCode, int resCode, Intent data ) {
        if( RESULT_OK == resCode  ){
            if( CHOOSE_IMAGE == reqCode ) {
                Bundle extras = data.getExtras();
                String uri = extras.getString( "imageUri" );
                int size = extras.getInt( "size" );

                if( size == R.id.add_image_thumbnail ) {
                    uri = getScaledImage( uri, "thumb" );
                }
                else if( size == R.id.add_image_resized ) {
                    uri = getScaledImage( uri, "resized" );
                }

                if( null != uri ) {
                    insertAtCursor( "!["+ uri + "](" + uri +")" );
                }
            }
            else if (CHOOSE_IMAGE_TRANSFORM == reqCode) {
                Bundle extras = data.getExtras();
                String uri = extras.getString("imageUri");
                // replace the url
                if (null != uri) {
                    int transformIndex = extras.getInt("transformIndex");
                    Transform transform = transforms.get(transformIndex);

                    if( null != transform.prompt ) {
                        promptAndInsert( transform, uri );
                    }
                    else {
                        String processed = MarkupUtilities.process(transform.code, "IMAGE", uri );
                        insertAtCursor(processed);
                    }
                }
            }
        }

    }



    private void getImage( int returnCode, int transformIndex, int size ) {
        Intent i;
        Bundle extras = getIntent().getExtras();
        extras.putString( "repo", theRepo );
        extras.putString( "login", theLogin );
        extras.putInt( "transformIndex", transformIndex );
        extras.putInt( "size", size );

        if( null != images ) {
            extras.putStringArray( "images", images );
        }
        i = new Intent(this, PixActivity.class);
        i.putExtras(extras);

        startActivityForResult( i, returnCode );
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        int groupId = item.getGroupId();
        boolean rv = false;

        if( groupId == HYDE_TRANSFORMS_GROUP_ID ) {
            // Get the transform and handle it
            Transform theTransform = transforms.get( itemId );
            if( 0 == "image".compareTo( theTransform.type ) ) {
                getImage(CHOOSE_IMAGE_TRANSFORM, itemId, 0 );
                rv = true;
            }
            else if( 0 == "insert".compareTo( theTransform.type ) ) {
                if( null != theTransform.prompt ) {
                    promptAndInsert( theTransform, null );
                }
                else {
                    insertAtCursor( theTransform.code );
                }
                rv = true;
            }

        }
        else {
            String contents;
            EditText et;

            switch ( itemId ) {

                case R.id.add_image_full:
                case R.id.add_image_resized:
                case R.id.add_image_thumbnail:
                    getImage( CHOOSE_IMAGE, 0, itemId );
                    rv = true;
                    break;

                case R.id.action_paste_code:
                case R.id.action_paste_quote:
                case R.id.action_paste_link:

                    // grab whatever is in the clipboard
                    ClipboardManager cm = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
                    if( cm.hasPrimaryClip() ) {
                        ClipData cd = cm.getPrimaryClip();
                        ClipData.Item clipItem  = cd.getItemAt(0);
                        if( null != item ) {
                            CharSequence theText = clipItem.getText();

                            if( itemId == R.id.action_paste_quote ) {
                                pasteQuote( theText.toString() );
                            }
                            else if( itemId == R.id.action_paste_code ) {
                                pasteCode( theText.toString() );
                            }
                            else if( itemId == R.id.action_paste_link ) {
                                pasteLink(theText.toString());
                            }
                        }
                    }

                    rv = true;
                    break;

                case R.id.action_save_with_commit:
                case R.id.action_save_file:
                    et = (EditText) findViewById(R.id.markdownEditor);
                    EditText yfmEt = (EditText) findViewById(R.id.yfmEditText);
                    String yfm = yfmEt.getText().toString();
                    String markup = et.getText().toString();
                    contents = yfm + markup;

                    if( R.id.action_save_with_commit == itemId ) {
                        promptForCommitMessage( contents );
                    }
                    else {
                        startSaveProgressIndicator();
                        new SaveFileTask().execute( contents, null );
                    }
                    rv = true;
                    break;

                case R.id.action_share_last_gist:
//                    startActivity(Intent.createChooser(new Intent(Intent.ACTION_SEND,
//                            Uri.parse(lastGistUrl)),"Scratchpad Gist"));
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_TEXT, lastGistUrl );
                    startActivity(shareIntent);
                    break;

                case R.id.action_save_as_gist:
                    startSaveProgressIndicator();
                    et = (EditText) findViewById(R.id.markdownEditor);
                    contents = et.getText().toString();
                    new SaveGistTask().execute( contents );
                    break;

                case R.id.action_close_scratchpad:
                    finishWithResult();
                    break;

                case R.id.action_save_into_repository:
                    saveIntoRepository();
                    break;

                case android.R.id.home:
                    // Navigate "up" the demo structure to the launchpad activity.
                    // See http://developer.android.com/design/patterns/navigation.html for more.
                    NavUtils.navigateUpTo(this, new Intent(this, MainActivity.class));
                    rv = true;
                    break;

                case R.id.action_next:
                    // Advance to the next step in the wizard. If there is no next step, setCurrentItem
                    // will do nothing.
                    mPager.setCurrentItem(mPager.getCurrentItem() + 1);
                    rv = true;
                    break;
            }
        }

        return rv || super.onOptionsItemSelected(item);
    }


    private void saveIntoRepository() {
        final LinearLayout ll = new LinearLayout(ScreenSlideActivity.this);
        ll.setOrientation(LinearLayout.VERTICAL);
        final EditText input = new EditText(ScreenSlideActivity.this);
        final Spinner spinner = new Spinner(ScreenSlideActivity.this);
        SharedPreferences sp = sp = this.getSharedPreferences( MainActivity.APP_ID, MODE_PRIVATE);

        Set<String> repositorySet;
        repositorySet = sp.getStringSet( getString(R.string.cached_repositories), null );
        List<String> repositoryList = new ArrayList<String>();
        for( String s : repositorySet ) {
            repositoryList.add( s );
        }

        if( 0 < repositorySet.size() ) {
            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, repositoryList);
            spinner.setAdapter(spinnerArrayAdapter);
            ll.addView( input );
            ll.addView( spinner );

            new AlertDialog.Builder(ScreenSlideActivity.this)
                    .setTitle("Filename")
                    .setMessage( "Enter filename to save under...")
                    .setView(ll)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            startSaveProgressIndicator();
                            EditText et = (EditText) findViewById(R.id.markdownEditor);
                            String contents = et.getText().toString();
                            String filename = input.getText().toString();
                            String repository = spinner.getSelectedItem().toString();
                            new SaveIntoRepository().execute( authToken, repository, theLogin, contents, filename );

                            finishWithResult();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // Do nothing.
                        }
                    }).show();
        }
        else {
            Toast.makeText( this, "No repositories into which save is possible.", Toast.LENGTH_LONG );
        }
    }

    /**
     * A simple pager adapter that represents 2 {@link ScreenSlidePageFragment} objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment rv;
            if( position == 0 ) {

                editorFragment = ScreenSlidePageFragment.create( position, theMarkdown, theFile );
                rv = editorFragment;

                // ((TextView)findViewById(R.id.currentFilename)).setText( theFile );

            }
            else {
                md = ScreenSlidePageFragmentMarkup.create(position);
                rv = md;

            }
            return (Fragment)rv;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

    private class SaveGistTask extends AsyncTask<String, Void, Boolean> {
        String URL;

        protected Boolean doInBackground(String...strings) {
            Boolean rv = true;
            String contents = strings[0];
            URL = ThGitClient.SaveGist( authToken, contents, "some description", "filename1.txt" );
            return rv;
        }

        protected void onPostExecute(Boolean result) {
            pb.setVisibility(View.GONE);

            if( !result ) {
                Toast.makeText( ScreenSlideActivity.this, "Unable to save file, please try again later.", Toast.LENGTH_LONG );
            }
            else {
                Toast.makeText( ScreenSlideActivity.this, "Copied Gist URL to clipboard.", Toast.LENGTH_LONG );
                addShareGistLink(URL);
            }

            editorFragment.makeClean();
        }

        private void addShareGistLink( String url ) {
            invalidateOptionsMenu();
            lastGistUrl = url;
        }
    }

    private class SaveIntoRepository extends AsyncTask<String, Void, Boolean> {

        protected Boolean doInBackground(String...strings) {
            Boolean rv = true;

            String authToken = strings[0];
            String repo = strings[1];
            String contents = strings[3];
            String filename = strings[4];
            String commitMessage = "Created using Teddy Hyde (teddyhyde.com) at " + new Date(System.currentTimeMillis()).toLocaleString();
            SharedPreferences sp = getSharedPreferences( MainActivity.APP_ID, MODE_PRIVATE );
            String theLogin = sp.getString( "login", null );

            String base64ed = Base64.encodeToString( contents.getBytes(), Base64.DEFAULT );

            theSha = ThGitClient.SaveFile( authToken, repo, theLogin, base64ed, filename, commitMessage);

            return rv;

        }


        protected void onPostExecute(Boolean result) {
            pb.setVisibility(View.GONE);

            if( !result ) {
                Toast.makeText( ScreenSlideActivity.this, "Unable to save file, please try again later.", Toast.LENGTH_LONG );
            }

            editorFragment.makeClean();
        }
    }


    private class SaveFileTask extends AsyncTask<String, Void, Boolean> {

        protected Boolean doInBackground(String...strings) {
            Boolean rv = true;

            String contents = strings[0];
            String commitMessage = strings[1];

            if( null == commitMessage ) {
                commitMessage = "Edited by Teddy Hyde (teddyhyde.com) at " + new Date(System.currentTimeMillis()).toLocaleString();
            }

            String base64ed = Base64.encodeToString( contents.getBytes(), Base64.DEFAULT );

            theSha = ThGitClient.SaveFile(authToken, theRepo, theLogin, base64ed, theFile, commitMessage);

            return rv;

        }


        protected void onPostExecute(Boolean result) {
            pb.setVisibility(View.GONE);

            if( !result ) {
                Toast.makeText( ScreenSlideActivity.this, "Unable to save file, please try again later.", Toast.LENGTH_LONG );
            }

            editorFragment.makeClean();
        }
    }
}