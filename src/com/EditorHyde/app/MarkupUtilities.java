package com.EditorHyde.app;

import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

/**
 * Created with IntelliJ IDEA.
 * User: xrdawson
 * Date: 4/11/13
 * Time: 9:30 AM
 * To change this template use File | Settings | File Templates.
 */
public class MarkupUtilities {

    public static String stripYFM( String markdown ) {
        int yfmStart = markdown.indexOf( "---" );
        int yfmEnd = markdown.indexOf( "---", 4 );
        String withOutYFM = markdown;
        if( -1 != yfmStart && -1 != yfmEnd ) {
            withOutYFM = markdown.substring(yfmEnd+"---".length()+1);
        }

        return withOutYFM;
    }

    public static String getYFM( String markdown ) {
        int yfmStart = markdown.indexOf( "---" );
        int yfmEnd = markdown.indexOf( "---", 4 );
        String yfm = markdown;
        if( -1 != yfmStart && -1 != yfmEnd ) {
            yfm = markdown.substring(0,yfmEnd+4);
        }

        return yfm;
    }

    private static String processForRegex( String text, String placeholder, String replacement ) {

        String rv = text;

        // Search for the regex pattern plus a regex matcher
        String toMatch =  "\\{\\{" +
                placeholder
                         + "\\|/(.+?)/(.*?)/"
                + "\\}\\}"
                ;
        Pattern p = Pattern.compile( toMatch );
        Matcher m = p.matcher( text );

        if( m.find() ) {
            String first = m.group(1);
            String second = m.group(2);

            Pattern p2 = Pattern.compile( first );
            // Take $1 out of the replacement string and insert $2 in its place
            Matcher m2 = p2.matcher( replacement );

            String replaced = m2.replaceAll( second );

            // Now replace this in the original text
            rv = text.replace( "{{" + placeholder + "|/" + first + "/" + second + "/}}", replaced );
        }
        return rv;
    }

    public static String process( String text, String placeholder, String replacement ) {
        String processed = "";
        // These will be ignored if they don't exist

        // Process for regexp
        // {{placeholder|/input/output/}}
        processed = processForRegex( text, placeholder , replacement );

        // Process for URL escaping
        processed = processed.replace( "{{" + placeholder + "|url}}", URLEncoder.encode(replacement) );
        // Process for HTML escaping
        processed = processed.replace( "{{" + placeholder + "|html}}", escapeHtml(replacement) );


        // Process for dbl-quotes
        processed = processed.replace( "{{" + placeholder + "|escdblquotes}}", replacement.replace( "\"", "\\\"") );

        // This is wrong, PercentEscaper is not the right thing to use
        // Process for YAML escaping
        // processed = processed.replace( "{{" + placeholder + "|yaml}}", new PercentEscaper(PercentEscaper.SAFECHARS_URLENCODER, false ).escape(replacement) );

        // Process for regular placeholders
        processed = processed.replace( "{{" + placeholder + "}}", replacement );

        return processed;
    }
}
