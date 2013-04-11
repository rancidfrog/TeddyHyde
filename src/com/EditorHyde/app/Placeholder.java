package com.EditorHyde.app;

import java.net.URLEncoder;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

/**
 * Created with IntelliJ IDEA.
 * User: xrdawson
 * Date: 4/11/13
 * Time: 9:30 AM
 * To change this template use File | Settings | File Templates.
 */
public class Placeholder {

    public static String process( String text, String placeholder, String replacement ) {
        String processed = "";
        // These will be ignored if they don't exist

        // Process for regexp
        // {{placeholder|/input/output/}}


        // Process for URL escaping
        processed = text.replace( "{{" + placeholder + "|url}}", URLEncoder.encode(replacement) );
        // Process for HTML escaping
        processed = processed.replace( "{{" + placeholder + "|html}}", escapeHtml(replacement) );

        // Process for regular placeholders
        processed = processed.replace( "{{" + placeholder + "}}", replacement );

        return processed;
    }
}
