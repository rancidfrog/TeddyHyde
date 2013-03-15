package com.EditorHyde.app;

/**
 * Created with IntelliJ IDEA.
 * User: xrdawson
 * Date: 3/13/13
 * Time: 2:11 PM
 * To change this template use File | Settings | File Templates.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FileListAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final String[] values;

    public FileListAdapter(Context context, String[] values) {
        super(context, R.layout.file_list_layout, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.file_list_layout, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.firstLine);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        textView.setText(values[position]);
        // Change the icon for Windows and iPhone
        String s = values[position];
        if (s.startsWith("_conf")) {
            imageView.setImageResource(R.drawable.icon);
        } else {
            imageView.setImageResource(R.drawable.directory);
        }

        return rowView;
    }
}