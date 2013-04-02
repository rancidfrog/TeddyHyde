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
import android.text.SpannableString;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import org.eclipse.egit.github.core.*;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.DataService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;

import java.io.IOException;
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
    Tree repoTree;
    ProgressBar pb;


    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager mPager;

    String theMarkdown;
    String theFile;
    String theRepo;
    String authToken;
    String[] images;

    ScreenSlidePageFragmentMarkdown md;
    ProgressDialog pd;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        theMarkdown = getIntent().getExtras().getString( "markdown" );
        theFile = getIntent().getExtras().getString( "filename" );
        theRepo = getIntent().getExtras().getString( "repo" );
        images = getIntent().getExtras().getStringArray( "images" );

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

                if( 1 == position ) {
                    md.onPageSelected(position);
                }

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.activity_screen_slide, menu);

        menu.findItem(R.id.action_previous).setEnabled(mPager.getCurrentItem() > 0);

        // Add either a "next" or "finish" button to the action bar, depending on which page
        // is currently selected.
        MenuItem item = menu.add(Menu.NONE, R.id.action_next, Menu.NONE,
                (mPager.getCurrentItem() == mPagerAdapter.getCount() - 1)
                        ? R.string.action_finish
                        : R.string.action_next);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

        // If we are in the editor menu, then enable the save with commit message...
        menu.findItem(R.id.action_save_with_commit).setEnabled(mPager.getCurrentItem() == 0);

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
                        new SaveFileTask().execute(authToken, theRepo, contents, message);

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

    @Override
    public void onActivityResult( int reqCode, int resCode, Intent data ) {
        if( resCode == RESULT_OK ){
            if( reqCode == CHOOSE_IMAGE ) {
                Bundle extras = data.getExtras();
                String uri = extras.getString( "imageUri" );
                if( null != uri ) {
                    insertAtCursor( "!(" + uri +")" );
                }
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch ( itemId ) {

            case R.id.add_image:
                Intent i;
                Bundle extras = getIntent().getExtras();
                extras.putString( "repo", theRepo );
                extras.putStringArray( "images", images );
                i = new Intent(this, PixActivity.class);
                i.putExtras(extras);

                startActivityForResult(i, CHOOSE_IMAGE );
                return true;

            case R.id.action_paste_code:
            case R.id.action_paste_quote:

                // grab whatever is in the clipboard
                ClipboardManager cm = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
                if( cm.hasPrimaryClip() ) {
                    ClipData cd = cm.getPrimaryClip();
                    ClipData.Item clipItem  = cd.getItemAt(0);
                    if( null != item ) {
                        SpannableString theText = (SpannableString) clipItem.getText();

                        if( itemId == R.id.action_paste_quote ) {
                            pasteQuote( theText.toString() );
                        }
                        else if( itemId == R.id.action_paste_code ) {
                            pasteCode( theText.toString() );
                        }
                    }
                }

                return true;


            case R.id.action_save_with_commit:
            case R.id.save_file:
                EditText et = (EditText) findViewById(R.id.markdownEditor);
                String contents = et.getText().toString();
                if( R.id.action_save_with_commit == itemId ) {
                    promptForCommitMessage( contents );
                }
                else {

                    startSaveProgressIndicator();
                    new SaveFileTask().execute( authToken, theRepo, contents, null );
                }
                return true;

            case android.R.id.home:
                // Navigate "up" the demo structure to the launchpad activity.
                // See http://developer.android.com/design/patterns/navigation.html for more.
                NavUtils.navigateUpTo(this, new Intent(this, MainActivity.class));
                return true;

            case R.id.action_previous:
                // Go to the previous step in the wizard. If there is no previous step,
                // setCurrentItem will do nothing.
                mPager.setCurrentItem(mPager.getCurrentItem() - 1);
                return true;

            case R.id.action_next:
                // Advance to the next step in the wizard. If there is no next step, setCurrentItem
                // will do nothing.
                mPager.setCurrentItem(mPager.getCurrentItem() + 1);
                return true;
        }

        return super.onOptionsItemSelected(item);
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
                rv = ScreenSlidePageFragment.create( position, theMarkdown );
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

            String authToken = strings[0];
            String repoName = strings[1];
            String contents = strings[2];
            String commitMessage = strings[3];

            if( null == commitMessage ) {
                commitMessage = "Edited by Teddy Hyde at " + new Date(System.currentTimeMillis()).toLocaleString();
            }

            String base64ed = Base64.encodeToString( contents.getBytes(), Base64.DEFAULT );

            rv = Github.SaveFile( authToken, repoName, base64ed, theFile, commitMessage );

            return rv;

        }


        protected void onPostExecute(Boolean result) {
            pb.setVisibility(View.GONE);

            if( !result ) {
                Toast.makeText( ScreenSlideActivity.this, "Unable to save file, please try again later.", Toast.LENGTH_LONG );
            }
        }
    }
}