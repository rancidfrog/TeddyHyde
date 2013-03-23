package com.EditorHyde.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.io.IOException;
import java.util.List;

public class MainActivity extends Activity {

    SharedPreferences sp;
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        sp = getPreferences( MODE_PRIVATE );
        String username = sp.getString( "username", null );
        String password = sp.getString( "password", null );

        if( null != username && null != password ) {
            TextView tvU = (TextView)findViewById(R.id.githubUsername);
            TextView tvP = (TextView)findViewById(R.id.githubPassword);
            tvU.setText( username );
            tvP.setText( password );
        }

        findViewById(R.id.button).setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
//
                        TextView tvU = (TextView)findViewById(R.id.githubUsername);
                        TextView tvP = (TextView)findViewById(R.id.githubPassword) ;

                        sp.edit().putString( "username", tvU.getText().toString() );
                        sp.edit().putString( "password", tvP.getText().toString() );

                        new GetReposTask().execute();
                    }
                });

    }

    private class GetReposTask extends AsyncTask<Void, Void, Boolean> {
        protected Boolean doInBackground(Void...voids) {
            Boolean rv = true;
            String username, password;

            TextView tvU = (TextView)findViewById(R.id.githubUsername);
            TextView tvP = (TextView)findViewById(R.id.githubPassword);
            username = tvU.getText().toString();
            password = tvP.getText().toString();

            // Basic authentication
            GitHubClient client = new GitHubClient();
            client.setCredentials( username, password );

            RepositoryService service = new RepositoryService();
            List<Repository> repos = null;
            try {
                repos = service.getRepositories();
            }
            catch( IOException ioe ) {
                rv = false;
            }
            showRepoList( repos );
            return rv;
        }
    }

    public void showRepoList( List<Repository> repos ) {
        String [] repoNames = new String[repos.size()];
        for( int i = 0; i < repos.size(); i++ ) {
            repoNames[i] = repos.get(i).getName();
        }
        Intent i = new Intent(this, RepoListActivity.class);
        Bundle bundle = new Bundle();
        bundle.putStringArray("repos", repoNames);
        i.putExtras(bundle);
        startActivity(i);

    }
}
