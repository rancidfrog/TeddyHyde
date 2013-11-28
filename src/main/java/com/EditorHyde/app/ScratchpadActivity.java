package com.EditorHyde.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.roscopeco.ormdroid.Entity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import com.EditorHyde.app.Scratch;


/**
 * Created by xrdawson on 8/18/13.
 */
public class ScratchpadActivity extends Activity {


    public class ScratchAdapter extends ArrayAdapter<Scratch> {

        List<Scratch> scratches;
        int rid;

        public ScratchAdapter( Context ctx, int textViewResourceId, List<Scratch> scratches ) {
            super(ctx, textViewResourceId, scratches );
            this.scratches = scratches;
            this.rid = textViewResourceId;
        }

        @Override
        public long getItemId(int position) {
            Scratch it = this.scratches.get(position);
            return ( null != it ? it.id : 0 );
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }

    public static final int NEW_SCRATCH = 1;
    public static final int EXISTING_SCRATCH = 2;
    ListView scratches;
    List<Scratch> scratchList;
    ScratchAdapter adapter;

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    String theLogin;

    private void setDisplayOfNotification() {
        TextView noScratches = (TextView)findViewById(R.id.no_scratches);
        if( null != scratchList ) {
            noScratches.setVisibility( 0 == scratchList.size()? View.VISIBLE : View.GONE);
        }
        if( null != adapter ) {
            adapter.notifyDataSetChanged();
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.scratch);
        scratches = (ListView)findViewById(R.id.scratch_list);
        scratchList = Scratch.scratches();

        setDisplayOfNotification();

        adapter = new ScratchAdapter( this, android.R.layout.simple_list_item_1, scratchList );

        scratches.setAdapter(adapter);

        scratches.setTextFilterEnabled(true);
        scratches.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Scratch scratch = (Scratch) scratches.getItemAtPosition(position);
                String markdown = scratch.contents;
                startMarkdownActivity(markdown, EXISTING_SCRATCH, scratch.id);
            }
        });
        scratches.setLongClickable(true);
        scratches.setOnItemLongClickListener( new AdapterView.OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                new AlertDialog.Builder(ScratchpadActivity.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle( "Delete?")
                        .setMessage("Do you want to remove this scratch?")
                        .setPositiveButton( "Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Scratch scratch = scratchList.get( position );
                                scratch.delete();
                                scratchList.remove( position );
                                adapter.notifyDataSetChanged();
                            }
                        }
                        )
                        .setNegativeButton("No", null )
                        .show();
                return true;
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
            Bundle bundle = data.getExtras();
            String contents = bundle.getString("scratch");
            Scratch scratch = new Scratch();
            scratch.contents = contents;
            scratch.save();
            scratchList.add( scratch );
        }
        else {
            String contents = data.getStringExtra("scratch");
            String id = data.getStringExtra("scratch_id");
            Scratch scratch = Scratch.query(Scratch.class).where("id=" + id).execute();
            scratch.contents = contents;
            scratch.save();
        }

        setDisplayOfNotification();
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
            template = MarkupUtilities.process( template, "TITLE", "Markdown created " + prefix );
            startMarkdownActivity( template, NEW_SCRATCH, 0 );
        }

        return rv;
    }

    private void startMarkdownActivity( String markdown, int scratchType, int id ) {
        Intent i;
        i = new Intent(this, ScreenSlideActivity.class);
        Bundle extras = new Bundle(); // getIntent().getExtras();
        if( null != extras ) {
            extras.putString( "markdown", markdown );
            extras.putBoolean( "scratchpad", true );
            if( 0 != id ) {
            extras.putString( "scratch_id", Integer.toString( id ) );
            }

            i.putExtras(extras);
            startActivityForResult( i, scratchType );
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);  // Add this method.
    }

    @Override
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);  // Add this method.
    }

}

