package com.teddyhyde;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import org.eclipse.egit.github.core.Repository;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: xrdawson
 * Date: 4/3/13
 * Time: 1:44 PM
 * To change this template use File | Settings | File Templates.
 */


public class RepoListAdapter extends ArrayAdapter<Repository> {
    private final Context context;
    private final List<Repository> mRepositories;

    public RepoListAdapter(Context context, List<Repository> repositories ) {
        super(context, R.layout.repo_list_layout, repositories);
        this.context = context;
        mRepositories = repositories;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.repo_list_layout, parent, false);
        TextView repoView = (TextView) rowView.findViewById(R.id.repoName);
        TextView ownerView = (TextView) rowView.findViewById(R.id.repoOwner);

        Repository theRepo = mRepositories.get(position);
        repoView.setText( theRepo.getName() );
        ownerView.setText( "Owned by: " + theRepo.getOwner().getLogin());

        return rowView;
    }
}