package com.EditorHyde.app;

import android.app.Activity;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * Created with IntelliJ IDEA.
 * User: xrdawson
 * Date: 3/12/13
 * Time: 11:04 AM
 * To change this template use File | Settings | File Templates.
 */
public class RepoListActivity extends Activity {


    public void showFilesList() {
        Intent i = new Intent(this, FileListingActivity.class);
        startActivity(i);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.repo_list);

        ListView listView;
        listView = (ListView) findViewById(R.id.listView);

        String[] values = new String[] { "xrd.github.com", "slowgramming.github.com", "webiphany.github.com" };
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_expandable_list_item_1, values);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
             showFilesList();
            }
        });

    }

}
