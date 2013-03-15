package com.EditorHyde.app;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.eclipse.egit.github.core.Repository;

import org.eclipse.egit.github.core.client.*;
import org.eclipse.egit.github.core.service.*;


/**
 * Created with IntelliJ IDEA.
 * User: xrdawson
 * Date: 3/12/13
 * Time: 11:41 AM
 * To change this template use File | Settings | File Templates.
 */
public class FileListingActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_list);

        ListView listView;
        listView = (ListView) findViewById(R.id.repoFilesList);

//        //Basic authentication
//        GitHubClient client = new GitHubClient();
//        client.setCredentials("user", "passw0rd");
//
//        String[] values = new String[20];
//        RepositoryService service = new RepositoryService();
//        for (Repository repo : service.getRepositories("slowgramming")) {
//            values[0] = repo.getName();
//        }

        String[] values = new String[] { "_config.yml", "_posts", "_pages", "_attachments" };
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
//                R.layout.file_list_layout, values);

        FileListAdapter adapter = new FileListAdapter(this, values);

//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
//                R.layout.file_list_layout,
//                R.id.firstLine,
//                values);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                showEditor();
            }
        });


    }

    public void showEditor() {
        Intent i = new Intent(this, EditorActivity.class);
        startActivity(i);
    }

}