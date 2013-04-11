package com.EditorHyde.app;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Path;
import android.provider.MediaStore;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;
import android.widget.EditText;

// import com.jayway.android.robotium.solo.Solo;

import junit.framework.Assert;

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

//    private Solo solo;

    private void setPasswords() {
        githubLogin = Passwords.login;
        githubPassword = Passwords.password;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        setActivityInitialTouchMode(false);

    } // end of setUp() method definition
//
//    @Override
//    public void tearDown() throws Exception {
//        solo.finishOpenedActivities();
//    }

    public MainActivityTest() {
        super(MainActivity.class);
    }

//    public void testLogin() {
//        solo.typeText(R.id.githubEmail, "xrdawson@gmail.com");
//        solo.typeText(R.id.githubPassword, "coins0nTable");
//        solo.clickOnButton(R.id.loginMessage);
//        solo.waitForActivity("RepoListActivity");
//        Assert.assertEquals(solo.getCurrentActivity().getClass(), RepoListActivity.class);
//
//    }
}
