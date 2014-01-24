package com.EditorHyde.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

/**
 * Created by xrdawson on 1/24/14.
 */
public class BlogCreatorActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {

        setContentView( R.layout.blog_creator );

        // Add content to the spinners
        String [] themes = { "amelia", "cerulean", "cosmo", "cyborg", "journal", "readable", "simplex", "slate", "spacelab", "spruce", "superhero", "united" };
        String [] types = { "jekyll", "nanoc", "hyde (python)", "hakyll" };

        ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_item, themes );
        Spinner themeSpinner = (Spinner) findViewById( R.id.blogThemeSpinner );
        themeSpinner.setAdapter( adapter );

        ArrayAdapter adapter2 = new ArrayAdapter(this,
                android.R.layout.simple_spinner_item, types );
        Spinner typeSpinner = (Spinner) findViewById( R.id.blogThemeSpinner );
        typeSpinner.setAdapter( adapter2 );

        Button newBlog = (Button) findViewById( R.id.createNewBlogButton );
        newBlog.setOnClickListener( new Button.OnClickListener() {
            public void onClick(View v)
            {
                EditText titleEt = (EditText) findViewById( R.id.blogTitle );
                EditText subtitleEt = (EditText) findViewById( R.id.blogSubtitle );


                String title = titleEt.getText().toString();
                String subtitle = subtitleEt.getText().toString();
                String type = "jekyll";
                String theme = "spacelab";

                // Hit Teddy Hyde and build out the blog
                TeddyHydeClient.createNewBlog(title, subtitle, type, theme );

                finish();

            }
        });


    }
}
