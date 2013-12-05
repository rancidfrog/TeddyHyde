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

    public ScratchpadListAdapter(Context context, List<Scratch> scratches) {
        super(context, R.layout.scratchpad_list_layout, scratches);
        this.context = context;
        mScratches = scratches;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.scratchpad_list_layout, parent, false);
        TextView repoView = (TextView) rowView.findViewById(R.id.scratchpadText);
        TextView ownerView = (TextView) rowView.findViewById(R.id.scratchpadDate);
        Scratch scratch= mScratches.get(position);
        repoView.setText( scratch.contents );
        ownerView.setText( "Updated: " +  scratch.updatedAt );
        return rowView;
    }
}