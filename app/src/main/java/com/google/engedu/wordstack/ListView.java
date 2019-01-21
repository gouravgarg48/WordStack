package com.google.engedu.wordstack;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class ListView extends AppCompatActivity {
    private DrawerLayout mDrawerLayout;
    private String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);
        id = getIntent().getStringExtra("listId");

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
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
                                if (id.equals("rec")) {
                                    intent = new Intent(getBaseContext(), ListView.class);
                                    intent.putExtra("listId", "fav");
                                    startActivity(intent);
                                }
                                break;
                            case R.id.nav_recents:
                                if (id.equals("fav")) {
                                    intent = new Intent(getBaseContext(), ListView.class);
                                    intent.putExtra("listId", "rec");
                                    startActivity(intent);
                                }
                                break;
                            default:
                                startActivity(new Intent(getBaseContext(), MainActivity.class));
                        }
                        return true;
                    }
                });

        loadWords(id);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.list_toolbar_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.action_delete:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.remove)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) { deleteAll(); }
                        })
                        .setNegativeButton("No", null);
                builder.show();
                break;
            case R.id.action_search:
                Intent i = new Intent(getBaseContext(), SearchActivity.class);
                startActivity(i);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void loadWords(String id) {
        ActionBar actionBar = getSupportActionBar();
        final ArrayList<String> words = new ArrayList<>();
        if (id.equals("fav")) {
            actionBar.setTitle("Favourites");
            try {
                FileInputStream fileIn = openFileInput("fav_words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(fileIn));

                String line = reader.readLine();
                while (line != null) {
                    words.add(line);
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
                BufferedReader reader = new BufferedReader(new InputStreamReader(fileIn));

                String line = reader.readLine();
                while (line != null) {
                    words.add(line);
                    line = reader.readLine();
                }
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, words);
        android.widget.ListView listView = (android.widget.ListView) findViewById(R.id.list);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent intent = new Intent(getBaseContext(), WordDetails.class);
                intent.putExtra("word", words.get(position));
                startActivity(intent);
            }
        });
    }

    public void deleteAll() {
        try {
            FileOutputStream fileout;
            OutputStreamWriter outputWriter;
            if (id.equals("fav"))
                fileout = openFileOutput("fav_words.txt", MODE_PRIVATE);
            else
                fileout = openFileOutput("recent_words.txt", MODE_PRIVATE);
            outputWriter = new OutputStreamWriter(fileout);
            outputWriter.write("");
            outputWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        loadWords(id);
        return;
    }

    public void onBackPressed() {
        Intent startMain = new Intent(getBaseContext(), MainActivity.class);
        startMain.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(startMain);
    }

}
