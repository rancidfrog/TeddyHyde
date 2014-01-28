package com.EditorHyde.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.eclipse.egit.github.core.Repository;

import java.util.List;

/**
 * Created by xrdawson on 12/5/13.
 */
public class ScratchpadListAdapter extends ArrayAdapter<Scratch> {
    private final Context context;
    private final List<Scratch> mScratches;
    private int SUBSTRING_LENGTH = 35;

    public ScratchpadListAdapter(Context context, List<Scratch> scratches) {
        super(context, R.layout.scratchpad_list_layout, scratches);
        this.context = context;
        mScratches = scratches;
    }

    private String stripContents( String contents ) {
        String rv = "";
        if( null != contents && "" != contents ) {
            String yamlStripped = MarkupUtilities.stripYFM( contents );
            String stripped = yamlStripped.replace( '\n', ' ');
            if( stripped.length() < SUBSTRING_LENGTH ) {
                rv += stripped;
            }
            else {
                rv += stripped.substring(0, SUBSTRING_LENGTH) + "...";
            }
        }
        else {
            rv = "Unknown contents";
        }

        return rv;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.scratchpad_list_layout, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.scratchpadText);
        TextView dateView = (TextView) rowView.findViewById(R.id.scratchpadDate);
        Scratch scratch= mScratches.get(position);
        String stripped = stripContents( scratch.contents );
        textView.setText( stripped );
        dateView.setText( "Updated: " +  scratch.updatedAt );
        return rowView;
    }
}