package com.EditorHyde.app;

import android.app.Activity;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

/**
 * Created with IntelliJ IDEA.
 * User: xrdawson
 * Date: 3/12/13
 * Time: 11:04 AM
 * To change this template use File | Settings | File Templates.
 */
public class RepoListActivity extends Activity {


    public void showFilesList( String repo ) {
        Intent i = new Intent(this, FileListingActivity.class);

        Bundle bundle = new Bundle();
        bundle.putString("repo", repo );
        i.putExtras(bundle);

        startActivity(i);


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.repo_list);

        ListView listView;
        listView = (ListView) findViewById(R.id.listView);

        Bundle extras = getIntent().getExtras();
        String[] values = extras.getStringArray("repos");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_expandable_list_item_1, values);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String repo = (String)adapterView.getItemAtPosition(i);
                showFilesList(repo);
            }
        });

    }

}
