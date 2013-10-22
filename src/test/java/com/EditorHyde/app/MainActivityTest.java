package com.EditorHyde.app;
//import android.view.View.MeasureSpec;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import java.lang.Exception;
import java.lang.Override;

import static org.fest.assertions.api.ANDROID.assertThat;
import static org.junit.Assert.*;
//import org.junit.Assert;

@RunWith(RobolectricGradleTestRunner.class)
public class MainActivityTest {


    // @Before public void setUp() throws Exception {
    //     Runtime rt = Runtime.getRuntime();
    //     Process proc = rt.exec( "curl --data '{\"scopes\":[\"gist\"],\"note\":\"Demo\"}\' --user \"burning@burningon.com\" -i https://api.github.com/authorizations" );
    // }

    @Test public void heightRespectsWidth() {
        assertTrue( "This is true", true );
        assertTrue( "Nope, this is false", false );
        
        // SquaredImageView view = new SquaredImageView(Robolectric.application);

        // int width = MeasureSpec.makeMeasureSpec(100, MeasureSpec.EXACTLY);
        // int height = MeasureSpec.makeMeasureSpec(200, MeasureSpec.EXACTLY);

        // view.measure(width, height);

        // assertThat(view).hasMeasuredWidth(100);
        // assertThat(view).hasMeasuredHeight(100);
    }
}
