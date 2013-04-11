package com.EditorHyde.app;

import junit.framework.Test;
import junit.framework.TestCase;

/**
 * Created with IntelliJ IDEA.
 * User: xrdawson
 * Date: 4/11/13
 * Time: 9:27 AM
 * To change this template use File | Settings | File Templates.
 */
public class FormatterTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Load up the files for testing.

    } // end of setUp() method definition

    public void testForGenericEscaping() {
        String template;
        template = "{{IMAGE}}";
        String image = "http://foobar/foobar-thumb.png";

        String result = Placeholder.process(template, "IMAGE", image);

        assertEquals( result, image );

    }


    public void testForHtmlEscaping() {
        String template;
        template = "{{IMAGE|html}}";
        String image = "<img>http://foobar/foobar-thumb.png</img>";

        String result = Placeholder.process(template, "IMAGE", image);

        assertEquals( result, "&lt;img&gt;http://foobar/foobar-thumb.png&lt;/img&gt;" );

    }

    public void testForUrlEscaping() {
        String template;
        template = "{{IMAGE|url}}";
        String image = "http://foobar/foobar-thumb.png";

        String result = Placeholder.process(template, "IMAGE", image);

        assertEquals( result, "http%3A%2F%2Ffoobar%2Ffoobar-thumb.png" );

    }

    public void testFormatterForRegexes() {

        String template;
        template = "<a href='{{IMAGE|/\\-thumb//}}'>";
        String image = "foobar-thumb.png";

        String result = Placeholder.process(template, "IMAGE", image);

        assertEquals( result, "<a href='foobar.png}'>" );

    }
}


