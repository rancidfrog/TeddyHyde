package com.EditorHyde.app;

import android.graphics.Path;
import android.provider.MediaStore;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.EditText;

import java.io.File;
import java.util.Scanner;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class com.EditorHyde.app.MainActivityTest \
 * com.EditorHyde.app.tests/android.test.InstrumentationTestRunner
 */
public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private MainActivity mActivity;
    private EditText mLogin;
    private EditText mPassword;

    private String githubLogin;
    private String githubPassword;

    private void readPasswordFile() {
        Scanner scan = new Scanner("password.cfg");
        scan.useDelimiter("\\n");
        githubLogin = scan.next();
        githubPassword = scan.next();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        setActivityInitialTouchMode(false);

        mActivity = getActivity();
        mActivity.nukePreferences();

        mLogin = (EditText) mActivity.findViewById( R.id.githubEmail );
        mPassword = (EditText) mActivity.findViewById( R.id.githubPassword );

        // Load up the password file
        readPasswordFile();

    } // end of setUp() method definition

    public MainActivityTest() {
        super("com.EditorHyde.app", MainActivity.class);
    }

    public void testPreConditions() {
        String login = mLogin.getText().toString();
        assertTrue( login.equals("") );
    }
}
