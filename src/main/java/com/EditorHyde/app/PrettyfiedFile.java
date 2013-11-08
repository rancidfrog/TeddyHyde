package com.EditorHyde.app;

public class PrettyfiedFile {
    public String title;
    public String contents;
    public String root;
    public String prefix;
    public String template;
    public boolean doPrettify;

    public void prettify( String title, String type, PrettyfiedFile rv ) {

        if( doPrettify ) {

            // Don't replace non-word at the end of the string.
            String whitespaceStripped = title.toLowerCase().replaceAll( "\\W+", "-").replaceAll( "\\W+$", "" );

            String extension = type;
            if( type.equalsIgnoreCase("markdown") ) {
                extension = "md";
            }

            rv.title = rv.root + rv.prefix + whitespaceStripped + "." + extension.toLowerCase();

            if( type.equalsIgnoreCase("markdown")) {
                rv.contents = MarkupUtilities.process( rv.template, "TITLE", title );
            }
            else {
                rv.contents = "";
            }
        }
        else {
            rv.title = rv.root + title;
            rv.contents = "";
        }

    }


}

