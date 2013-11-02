/*
 * Copyright 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.EditorHyde.app;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;

import com.petebevin.markdown.MarkdownProcessor;
// import org.asciidocj.*;
import org.asciidoctor.Asciidoctor;
import java.util.Collections;

import static com.EditorHyde.app.R.*;

public class ScreenSlidePageFragmentMarkup extends Fragment implements ViewPager.OnPageChangeListener {
    /**
     * The argument key for the page number this fragment represents.
     */
    public static final String ARG_PAGE = "page";

    /**
     * The fragment's page number, which is set to the argument value for {@link #ARG_PAGE}.
     */
    private int mPageNumber;

    private String theMarkdown;
    ViewGroup rootView;
    /**
     * Factory method for this fragment class. Constructs a new fragment for the given page number.
     */
    public static ScreenSlidePageFragmentMarkup create(int pageNumber ) {
        ScreenSlidePageFragmentMarkup fragment = new ScreenSlidePageFragmentMarkup();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, pageNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public ScreenSlidePageFragmentMarkup() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageNumber = getArguments().getInt(ARG_PAGE);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = (ViewGroup) inflater
                .inflate(layout.fragment_screen_slide_page, container, false);
        // getDialog().setTitle("Rendering markdown");

        return rootView;
    }

    /**
     * Returns the page number represented by this fragment object.
     */
    public int getPageNumber() {
        return mPageNumber;
    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {
        //To change body of implemented methods use File | Settings | File Templates.
    }


    private String addMetadataAndBody( String converted, String extras ) {
        String fullHtml =
                "<html><head>" +
                        "<base href=\"" + RemoteFileCache.getHttpRoot() + "\">" + extras +
                        "</head><body>" + converted + "</body></html>";

        return fullHtml;
    }

    @Override
    public void onPageSelected(int i) {
        // Set the title view to show the page number.
        WebView wv = (WebView)rootView.findViewById(id.webView);

        WebSettings webSettings = wv.getSettings();
        webSettings.setJavaScriptEnabled(true);

        wv.setWebChromeClient(new WebChromeClient() {
            public boolean onConsoleMessage(ConsoleMessage cm) {
                Log.d("TeddyHyde", cm.message() + " -- From line "
                        + cm.lineNumber() + " of "
                        + cm.sourceId());
                return true;
            }
        });

        // Get md text
        EditText et = (EditText)getActivity().findViewById(id.markdownEditor);
        TextView filenameTV = (TextView)getActivity().findViewById(id.currentFilename);
        String filename = filenameTV.getText().toString();
        String markup = et.getText().toString();
        String fullHtml = markup;

        if( filename.endsWith( ".md" ) || filename.endsWith( ".markdown" ) ) {
            MarkdownProcessor md = new MarkdownProcessor();
            String converted = "";
            String yfmStripped = MarkupUtilities.stripYFM(markup);

            // convert to HTML
            converted = md.markdown( yfmStripped );

            // Add some information to make images load correctly, etc.
            String extras =
                    "<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/css/markdown.css\"/>";

            fullHtml = addMetadataAndBody( converted, extras );

        }
       else if (filename.endsWith(".asciidoc")) {

            String extras =
                    "<script src=\"file:///android_asset/js/opal.js\"></script>"
                            + "<script src=\"file:///android_asset/js/asciidoctor.js\"></script>" +
                    "<script src=\"file:///android_asset/js/teddyhyde.js\"></script>"
            //+ "<script src=\"/js/teddyhyde.js\"></script>"
            //+ "<script src=\"teddyhyde.js\"></script>"
                    ;

//                            "<script src=\"teddyhyde.js\"></script>" +
//                            "<script src=\"\"teddyhyde.js\"></script>";
////
//                    "<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/js/opal.js\"/>\n" +
//            "<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/js/asciidoctor.js\"/>\n" +

//            extras = "<script>alert( 'Hi, we are inside Teddy Hyde.' );document.write( 'Hey there, this is great, thanks.' );</script>";


            fullHtml= "<html><body>Hi there<br/>" + extras + "</body></html>";

//            fullHtml = addMetadataAndBody( fullHtml, extras );
            // create the HTML with the opal and asciidoctor.js libraries
        }

        wv.loadDataWithBaseURL("file:///android_asset/", fullHtml, "text/html", "utf-8", "");
//        wv.loadData( fullHtml, "text/html", "utf-8" );

        // this.setShowsDialog(false);

    }

    @Override
    public void onPageScrollStateChanged(int i) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
