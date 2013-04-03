package com.EditorHyde.app;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Path;
import android.provider.MediaStore;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;
import android.widget.EditText;

import java.io.File;
import java.io.FileNotFoundException;
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
    private Button mLoginButton;

    private String githubLogin;
    private String githubPassword;

    private void setPasswords() {
        githubLogin = Passwords.login;
        githubPassword = Passwords.password;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        setActivityInitialTouchMode(false);

        mActivity = getActivity();
        mActivity.nukePreferences();

        mLogin = (EditText) mActivity.findViewById( R.id.githubEmail );
        mPassword = (EditText) mActivity.findViewById( R.id.githubPassword );
        mLoginButton = (Button) mActivity.findViewById( R.id.button );

        // Load up the password file
        setPasswords();

    } // end of setUp() method definition

    public MainActivityTest() {
        super("com.EditorHyde.app", MainActivity.class);
    }

    public void testPreConditions() {
        String login = mLogin.getText().toString();
        assertTrue( login.equals("") );
    }

    public void testLogin() {
        mLogin.setText( githubLogin );
        mPassword.setText(githubPassword);

        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mLoginButton.performClick();
            }
        });

        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        SharedPreferences sp = mActivity.getSharedPreferences(MainActivity.APP_ID, Activity.MODE_PRIVATE);
        String authToken = sp.getString( "authToken", null );
        assertNotNull(authToken);

    }
}
