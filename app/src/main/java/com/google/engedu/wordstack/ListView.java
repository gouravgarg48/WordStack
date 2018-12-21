package com.google.engedu.wordstack;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ListView extends AppCompatActivity {
    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Search");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();
                        Intent intent;
                        switch (menuItem.getItemId()) {
                            case R.id.nav_home:
                                startActivity(new Intent(getBaseContext(), MainActivity.class));
                                break;
                            case R.id.nav_search:
                                startActivity(new Intent(getBaseContext(), SearchActivity.class));
                                break;
                            case R.id.nav_favourites:
                                intent = new Intent(getBaseContext(), ListView.class);
                                intent.putExtra("listId", "fav");
                                startActivity(intent);
                                break;
                            case R.id.nav_recents:
                                intent = new Intent(getBaseContext(), ListView.class);
                                intent.putExtra("listId", "rec");
                                startActivity(intent);
                                break;
                            default:
                                startActivity(new Intent(getBaseContext(), MainActivity.class));
                        }
                        return true;
                    }
                });

        // Create a list of words
        final ArrayList<String> words = new ArrayList<>();
        String id = getIntent().getStringExtra("listId");
        if (id.equals("fav")) {
            actionBar.setTitle("Favourites");
            try {
                FileInputStream fileIn = openFileInput("fav_words.txt");
                InputStreamReader isr = new InputStreamReader(fileIn);
                BufferedReader reader = new BufferedReader(isr);

                String line = reader.readLine();
                while (line != null) {
                    words.add(line);
                    // read next line
                    line = reader.readLine();
                }
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (id.equals("rec")) {
            actionBar.setTitle("History");
            try {
                FileInputStream fileIn = openFileInput("recent_words.txt");
                InputStreamReader isr = new InputStreamReader(fileIn);
                BufferedReader reader = new BufferedReader(isr);

                String line = reader.readLine();
                while (line != null) {
                    words.add(line);
                    // read next line
                    line = reader.readLine();
                }
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Create an {@link WordAdapter}, whose data source is a list of {@link Word}s. The
        // adapter knows how to create list items for each item in the list.
        WordAdapter adapter = new WordAdapter(getBaseContext(), words);

        // Find the {@link ListView} object in the view hierarchy of the {@link Activity}.
        // There should be a {@link ListView} with the view ID called list, which is declared in the
        // word_list.xml layout file.
        android.widget.ListView listView = (android.widget.ListView) findViewById(R.id.list);

        // Make the {@link ListView} use the {@link WordAdapter} we created above, so that the
        // {@link ListView} will display list items for each {@link Word} in the list.
        listView.setAdapter(adapter);

        // Set a click listener to play the audio when the list item is clicked on
//            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                @Override
//                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {}
//            });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
