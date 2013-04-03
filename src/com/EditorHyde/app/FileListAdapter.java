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

import java.util.ArrayList;
import java.util.List;

public class FileListAdapter extends ArrayAdapter<TreeEntry> {
    private final Context context;
    private final List<TreeEntry> files;
    private String mPath;

    public FileListAdapter(Context context, List<TreeEntry>theFiles ) {
        super(context, R.layout.file_list_layout, theFiles);
        this.context = context;
        this.files = theFiles;

    }

    public void setFrontPath( String path ) {
        mPath = path;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.file_list_layout, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.firstLine);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);

        // Strip off the cwd
        String stripped;
        String path = files.get(position).getPath();

        if( null != mPath ) {
            stripped = path.replace( mPath,  "");
        }
        else {
            stripped = path;
        }

        textView.setText( stripped );

        TreeEntry treeEntry = files.get(position);

        String s = treeEntry.getPath();
        String type = treeEntry.getType();

        if( 0 ==  "tree".compareTo( type ) ) {
            imageView.setImageResource(R.drawable.directory);
        }
        else {
            imageView.setImageResource(R.drawable.icon);
        }

        return rowView;
    }
}