package com.EditorHyde.app;

/**
 * Created with IntelliJ IDEA.
 * User: xrdawson
 * Date: 4/3/13
 * Time: 9:18 AM
 * To change this template use File | Settings | File Templates.
 */
public class Cwd {

    private String mCwd;

    public Cwd() {
        mCwd = "";
    }

    public boolean atRoot() {
        return( "" == mCwd );
    }

    public void ascendOne() {
        // Strip off one of the paths
        int last = mCwd.lastIndexOf("/");
        if( -1 != last && last > 0 ) {
            mCwd = mCwd.substring( 0, last );
        }
        else {
            mCwd = "";
        }
    }

    public void descendTo( String cwd ) {
        mCwd = cwd;
    }

    public String getFullPathWithTrailingSlash() {
        String withEndingSlash = mCwd;
        if( withEndingSlash.length() > 0 ) {
            if( '/' != withEndingSlash.charAt(withEndingSlash.length()-1)) {
                withEndingSlash += "/";
            }
        }
        return withEndingSlash;
    }
}
