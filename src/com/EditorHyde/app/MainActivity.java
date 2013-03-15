package com.EditorHyde.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);


        findViewById(R.id.button).setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        showRepoList();
                    }
                });

    }

    public void showRepoList() {
            Intent i = new Intent(this, RepoListActivity.class);
            startActivity(i);

    }
}
