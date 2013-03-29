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

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;

import com.petebevin.markdown.MarkdownProcessor;

import java.io.IOException;

public class ScreenSlidePageFragmentMarkdown extends Fragment {
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
    public static ScreenSlidePageFragmentMarkdown create(int pageNumber ) {
        ScreenSlidePageFragmentMarkdown fragment = new ScreenSlidePageFragmentMarkdown();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, pageNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public ScreenSlidePageFragmentMarkdown() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageNumber = getArguments().getInt(ARG_PAGE);
    }


    @Override
    public void onResume() {

        super.onResume();
        // Set the title view to show the page number.
        WebView wv = (WebView)rootView.findViewById(R.id.webView);

        // Get md text
        EditText et = (EditText)getActivity().findViewById(R.id.markdownEditor);
        String markdown = et.getText().toString();
        MarkdownProcessor md = new MarkdownProcessor();
        String converted = "";
        int yfmStart = markdown.indexOf( "---" );
        int yfmEnd = markdown.indexOf( "---", 4 );
        String withOutYFM = markdown;
        if( -1 != yfmStart && -1 != yfmEnd ) {
            withOutYFM = markdown.substring(yfmEnd+"---".length()+1);
        }
        converted = md.markdown(withOutYFM);
        wv.loadData(converted, "text/html", null );
       // this.setShowsDialog(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = (ViewGroup) inflater
                .inflate(R.layout.fragment_screen_slide_page, container, false);
        // getDialog().setTitle("Rendering markdown");

        return rootView;
    }

    /**
     * Returns the page number represented by this fragment object.
     */
    public int getPageNumber() {
        return mPageNumber;
    }
}
