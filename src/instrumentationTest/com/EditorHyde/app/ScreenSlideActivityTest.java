package com.EditorHyde.app;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import com.jayway.android.robotium.solo.Solo;

import junit.framework.Assert;

import java.util.ArrayList;

/**
 * Created by xrdawson on 8/20/13.
 */
@TargetApi(Build.VERSION_CODES.CUPCAKE)
public class ScreenSlideActivityTest extends ActivityInstrumentationTestCase2<ScreenSlideActivity> {

    private Solo solo;

    @TargetApi(Build.VERSION_CODES.FROYO)
    public ScreenSlideActivityTest() {
        super(ScreenSlideActivity.class);

        Intent i = new Intent();
        i.putExtra("markdown", "foobar");
        i.putExtra( "isScratchpad", false );
        setActivityIntent(i);
    }

    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }

    public void setUp() throws Exception {
        solo = new Solo(getInstrumentation(), getActivity());
    }

    public void testScratchpad() throws Exception {

        boolean hasSaveAsGistMenu = false;
        ArrayList<View> views = solo.getCurrentViews();
        for( View view : views ) {
            if( R.id.action_save_as_gist == view.getId() ) {
                hasSaveAsGistMenu = true;
            }
        }

        Assert.assertEquals(hasSaveAsGistMenu, true);
//
//        solo.sendKey(Solo.MENU);
//        solo.clickOnText("More");
//        solo.clickOnText("Preferences");
//        solo.clickOnText("Edit File Extensions");
//        Assert.assertTrue(solo.searchText("rtf"));
//
//        solo.clickOnText("txt");
//        solo.clearEditText(2);
//        solo.enterText(2, "robotium");
//        solo.clickOnButton("Save");
//        solo.goBack();
//        solo.clickOnText("Edit File Extensions");
//        Assert.assertTrue(solo.searchText("application/robotium"));

    }

}