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
import org.eclipse.egit.github.core.TreeEntry;

import java.util.List;

public class FileListAdapter extends ArrayAdapter<TreeEntry> {
    private final Context context;
    private final List<TreeEntry> files;

    public FileListAdapter(Context context, List<TreeEntry>theFiles ) {
        super(context, R.layout.file_list_layout, theFiles);
        this.context = context;
        this.files = theFiles;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.file_list_layout, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.firstLine);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        textView.setText(files.get(position).getPath());

        TreeEntry treeEntry = files.get(position);

        String s = treeEntry.getPath();
        String type = treeEntry.getType();

        if( "blob".equals( type ) ) {
            if (s.startsWith("_conf")) {
                imageView.setImageResource(R.drawable.icon);
            } else {
                imageView.setImageResource(R.drawable.directory);
            }
        }
        else if( "tree".equals( type ) ) {

        }

        return rowView;
    }
}