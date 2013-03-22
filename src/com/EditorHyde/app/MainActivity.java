package com.EditorHyde.app;

import android.app.Activity;
import android.content.Intent;
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
                        String username, password;

                        TextView tvU = (TextView)findViewById(R.id.githubUsername);
                        TextView tvP = (TextView)findViewById(R.id.githubUsername);
                        username = (String) tvU.getText();
                        password = (String)tvP.getText();

                        // Basic authentication
                        GitHubClient client = new GitHubClient();
                        client.setCredentials( username, password );
                        RepositoryService service = new RepositoryService();
                        List<Repository> repos = null;
                        try {
                            repos = service.getRepositories("slowgramming");
                        }
                        catch( IOException ioe ) {

                        }
                        showRepoList( repos );
                    }
                });

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
