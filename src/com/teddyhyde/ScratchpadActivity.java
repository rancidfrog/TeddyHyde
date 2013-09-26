package com.teddyhyde;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by xrdawson on 8/18/13.
 */
public class ScratchpadActivity extends ListActivity {

    private ScratchDataSource datasource;
    public static final int NEW_SCRATCH = 1;
    public static final int EXISTING_SCRATCH = 2;
    ArrayAdapter<Scratch> adapter;

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

    String theLogin;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.scratch);

        datasource = new ScratchDataSource(this);
        datasource.open();

        List<Scratch> values = datasource.getAllScratches();

        // Use the SimpleCursorAdapter to show the
        // elements in a ListView
       adapter = new ArrayAdapter<Scratch>(this,
                android.R.layout.simple_list_item_1, values);
        setListAdapter(adapter);

        final ListView lv = getListView();
        lv.setTextFilterEnabled(true);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Scratch scratch = (Scratch) lv.getItemAtPosition( position );
                String markdown = scratch.getScratch();
                startMarkdownActivity( markdown, EXISTING_SCRATCH, scratch.getId() );
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.scratchpad, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == NEW_SCRATCH ) {
            Scratch scratch = new Scratch();
            Bundle bundle = data.getExtras();
            String contents = bundle.getString( "scratch" );
            // String contents = data.getStringExtra("scratch");
            scratch.setScratch(contents);
            adapter.add(scratch);
        }
        else {
            String contents = data.getStringExtra("scratch");
            String id = data.getStringExtra("scratch_id");
            datasource.updateScratch( id, contents );
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        String template = "";
        boolean rv = false;

        switch( itemId ) {
            case R.id.action_scratchpad_new_post:
                template = getString(R.string.post_template);
                // create a new post
                rv = true;
                break;
            case R.id.action_scratchpad_new_page:
                template = getString(R.string.page_template);
        rv = true;
                break;
            case R.id.action_scratchpad_new_markdown:
                template = "";
                rv = true;
                break;
        }

        if( rv ) {
            SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd" );
            String prefix = sdf.format( new Date() );
            template = Placeholder.process( template, "TITLE", "Markdown created " + prefix );
            startMarkdownActivity( template, NEW_SCRATCH, 0L );
        }

        return rv;
    }

    private void startMarkdownActivity( String markdown, int scratchType, Long id ) {
        Intent i;
        i = new Intent(this, ScreenSlideActivity.class);
        Bundle extras = new Bundle(); // getIntent().getExtras();
        if( null != extras ) {
            extras.putString( "markdown", markdown );
            extras.putBoolean( "scratchpad", true );
            if( null != id ) {
            extras.putString( "scratch_id", id.toString() );
            }

            i.putExtras(extras);
            startActivityForResult( i, scratchType );
        }
    }

//    public void onClick(View view) {
//        @SuppressWarnings("unchecked")
//        ArrayAdapter<Scratch> adapter = (ArrayAdapter<Scratch>) getListAdapter();
//        Scratch Scratch = null;
//        switch (view.getId()) {
//            case R.id.add:
//                String markdown = "";
//               startMarkdownActivity( markdown, NEW_SCRATCH, 0L );
//                break;
////            case R.id.delete:
////                if (getListAdapter().getCount() > 0) {
////                    Scratch = (Scratch) getListAdapter().getItem(0);
////                    datasource.deleteScratch(Scratch);
////                    adapter.remove(Scratch);
////                }
////                break;
//        }
//        adapter.notifyDataSetChanged();
//    }

}
