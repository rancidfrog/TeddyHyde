package com.EditorHyde.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by xrdawson on 8/18/13.
 */
public class ScratchpadActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i;
        i = new Intent(this, ScreenSlideActivity.class);
        Bundle extras = new Bundle(); // getIntent().getExtras();
        if( null != extras ) {
        extras.putString( "markdown", "## Enter a title here ##" );
//        extras.putString( "filename", theFilename );
//        extras.putString( "repo", repoName );
//        extras.putString( "login", login );
//        extras.putString( "transforms", transformsJson );
//        extras.putString( "sha", sha );
        i.putExtras(extras);
        startActivityForResult( i, 1 );
        }
    }
}