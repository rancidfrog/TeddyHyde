package com.EditorHyde.app;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

import java.util.List;
import java.util.Random;

/**
 * Created by xrdawson on 8/18/13.
 */
public class ScratchpadActivity extends ListActivity {

    private ScratchDataSource datasource;

    @Override
    protected void onResume() {
        datasource.open();
        super.onResume();
    }

    @Override
    protected void onPause() {
        datasource.close();
        super.onPause();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.scratch);

        datasource = new ScratchDataSource(this);
        datasource.open();

        List<Scratch> values = datasource.getAllScratches();

        // Use the SimpleCursorAdapter to show the
        // elements in a ListView
        ArrayAdapter<Scratch> adapter = new ArrayAdapter<Scratch>(this,
                android.R.layout.simple_list_item_1, values);
        setListAdapter(adapter);
//
//        Intent i;
//        i = new Intent(this, ScreenSlideActivity.class);
//        Bundle extras = new Bundle(); // getIntent().getExtras();
//        if( null != extras ) {
//        extras.putString( "markdown", "## Enter a title here ##" );
////        extras.putString( "filename", theFilename );
////        extras.putString( "repo", repoName );
////        extras.putString( "login", login );
////        extras.putString( "transforms", transformsJson );
////        extras.putString( "sha", sha );
//        i.putExtras(extras);
//        startActivityForResult( i, 1 );
       // }
    }

    // Will be called via the onClick attribute
    // of the buttons in main.xml
    public void onClick(View view) {
        @SuppressWarnings("unchecked")
        ArrayAdapter<Scratch> adapter = (ArrayAdapter<Scratch>) getListAdapter();
        Scratch Scratch = null;
        switch (view.getId()) {
            case R.id.add:
                String[] Scratches = new String[] { "Cool", "Very nice", "Hate it" };
                int nextInt = new Random().nextInt(3);
                // Save the new Scratch to the database
                Scratch = datasource.createScratch(Scratches[nextInt]);
                adapter.add(Scratch);
                break;
//            case R.id.delete:
//                if (getListAdapter().getCount() > 0) {
//                    Scratch = (Scratch) getListAdapter().getItem(0);
//                    datasource.deleteScratch(Scratch);
//                    adapter.remove(Scratch);
//                }
//                break;
        }
        adapter.notifyDataSetChanged();
    }

}