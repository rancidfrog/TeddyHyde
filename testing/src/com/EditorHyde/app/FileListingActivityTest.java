package com.EditorHyde.app;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created with IntelliJ IDEA.
 * User: xrdawson
 * Date: 4/5/13
 * Time: 10:20 AM
 * To change this template use File | Settings | File Templates.
 */
public class FileListingActivityTest extends ActivityInstrumentationTestCase2<FileListingActivity> {

    private FileListingActivity mActivity;
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
        setPasswords();
        Passwords.login( mActivity );

    } // end of setUp() method definition

    public FileListingActivityTest() {
        super("com.EditorHyde.app", FileListingActivity.class);
    }

}