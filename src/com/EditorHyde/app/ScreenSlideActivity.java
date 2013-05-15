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

import java.net.URLEncoder;
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
    List<Transform> transforms;

    ScreenSlidePageFragmentMarkdown md;
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
        theFile = extras.getString("filename");
        theRepo = extras.getString("repo");
        theLogin = extras.getString("login");
        theTransforms = extras.getString( "transforms" );
        theSha = extras.getString( "sha" );

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

        // If we are in the editor menu, then enable the save with commit message...
        menu.findItem(R.id.action_save_with_commit).setEnabled(mPager.getCurrentItem() == 0);

        loadHydeTransformsIntoMenu( menu );

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
                            message += " (edited by Teddy Hyde)";
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
        insertAtCursor( "\n    " + code + "\n" );
    }

    private void insertAtCursor( String text )  {
        EditText et = (EditText) findViewById(R.id.markdownEditor);
        int start = et.getSelectionStart();
        int end = et.getSelectionEnd();
        et.getText().replace(Math.min(start, end), Math.max(start, end),
                text, 0, text.length());
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
                        String processed = Placeholder.process(transform.code, "IMAGE", imageUrl);
                        processed = Placeholder.process(processed, "PROMPT", input.getText().toString() );
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
        extras.putString( "sha", theSha );
        extras.putString( "path", theFile);
        intent.putExtras( extras );
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
                        String processed = Placeholder.process(transform.code, "IMAGE", uri );
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
            switch ( itemId ) {

                case R.id.add_image_full:
                case R.id.add_image_resized:
                case R.id.add_image_thumbnail:
                    getImage( CHOOSE_IMAGE, 0, itemId );
                    rv = true;
                    break;

                case R.id.action_paste_code:
                case R.id.action_paste_quote:

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
                        }
                    }

                    rv = true;
                    break;

                case R.id.action_save_with_commit:
                case R.id.save_file:
                    EditText et = (EditText) findViewById(R.id.markdownEditor);
                    String contents = et.getText().toString();
                    if( R.id.action_save_with_commit == itemId ) {
                        promptForCommitMessage( contents );
                    }
                    else {

                        startSaveProgressIndicator();
                        new SaveFileTask().execute( contents, null );
                    }
                    rv = true;
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
                md = ScreenSlidePageFragmentMarkdown.create( position );
                rv = md;

            }
            return (Fragment)rv;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }


    private class SaveFileTask extends AsyncTask<String, Void, Boolean> {

        protected Boolean doInBackground(String...strings) {
            Boolean rv = true;

            String contents = strings[0];
            String commitMessage = strings[1];

            if( null == commitMessage ) {
                commitMessage = "Edited by Teddy Hyde at " + new Date(System.currentTimeMillis()).toLocaleString();
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