package com.EditorHyde.app;

import android.annotation.TargetApi;
import android.os.Build;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;
import android.widget.EditText;

import com.jayway.android.robotium.solo.Solo;

import junit.framework.Assert;

/**
 * Created with IntelliJ IDEA.
 * User: xrdawson
 * Date: 4/5/13
 * Time: 10:20 AM
 * To change this template use File | Settings | File Templates.
 */
@TargetApi(Build.VERSION_CODES.CUPCAKE)
public class FileListingActivityTest extends ActivityInstrumentationTestCase2<FileListingActivity> {

    private FileListingActivity mActivity;
    private String githubLogin;
    private String githubPassword;

    private void setPasswords() {
        githubLogin = Passwords.login;
        githubPassword = Passwords.password;
    }

    private Solo solo;

    //    @TargetApi(Build.VERSION_CODES.FROYO)
    public FileListingActivityTest() {
        super(FileListingActivity.class);
    }

    public void setUp() throws Exception {
        solo = new Solo(getInstrumentation(), getActivity());
    }

    public void testPreferenceIsSaved() throws Exception {
        solo.assertCurrentActivity("Is the file listing activity", FileListingActivity.class);

        // solo.sendKey(Solo.MENU);
        // solo.clickOnText("More");
        // solo.clickOnText("Preferences");
        // solo.clickOnText("Edit File Extensions");
        // Assert.assertTrue(solo.searchText("rtf"));

        // solo.clickOnText("txt");
        // solo.clearEditText(2);
        // solo.enterText(2, "robotium");
        // solo.clickOnButton("Save");
        // solo.goBack();
        // solo.clickOnText("Edit File Extensions");
        // Assert.assertTrue(solo.searchText("application/robotium"));

    }

    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }

}
