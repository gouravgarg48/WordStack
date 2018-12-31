package com.google.engedu.wordstack;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class WordDetails extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String> {
    private static final String LOG_TAG = MainActivity.class.getName();
    private DrawerLayout mDrawerLayout;

    private static String word;
    private static final String ODAPI_ENTRIES_REQUEST_URL = "https://od-api.oxforddictionaries.com:443/api/v1/entries";
    private static final int FEATURE_LOADER_ID1 = 1;
    private ArrayList<String> wordsList = new ArrayList<>();
    private boolean isFav = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_details);
        word = getIntent().getStringExtra("word");
        searchWord(word);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Search");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);

        final ImageView fav = findViewById(R.id.fav);
        try {
            FileInputStream fileIn = openFileInput("fav_words.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(fileIn));

            String line = reader.readLine().trim();
            while (line != null) {
                wordsList.add(line);
                line = reader.readLine().trim();
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (wordsList.contains(word)) {
            isFav = true;
            fav.setImageResource(R.drawable.ic_fav);
        }
        fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFav == false) {
                    isFav = true;
                    fav.setImageResource(R.drawable.ic_fav);
                    addToFavourites();
                } else {
                    isFav = false;
                    fav.setImageResource(R.drawable.ic_fav_border);
                    removeFromFavourites();
                }
            }
        });

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
                                break;
                        }
                        return true;
                    }
                });
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

    public void addToFavourites() {
        try {
            FileOutputStream fileout = openFileOutput("fav_words.txt", MODE_PRIVATE);
            OutputStreamWriter outputWriter = new OutputStreamWriter(fileout);
            outputWriter.write("");
            outputWriter.append(word + "\n");
            for (String str : wordsList)
                outputWriter.append(str + "\n");
            outputWriter.close();

            Toast.makeText(getBaseContext(), "Added to favourites!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return;
    }

    public void removeFromFavourites() {
        wordsList.remove(word);
        try {
            FileOutputStream fileout = openFileOutput("fav_words.txt", MODE_PRIVATE);
            OutputStreamWriter outputWriter = new OutputStreamWriter(fileout);
            outputWriter.write("");
            for (String str : wordsList)
                outputWriter.append(str + "\n");
            outputWriter.close();

            Toast.makeText(getBaseContext(), "Removed from favourites!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return;
    }

    public void addToRecents() {
        String line;
        ArrayList<String> recentWords = new ArrayList<>();
        try {
            FileInputStream fileIn = openFileInput("recent_words.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(fileIn));

            line = reader.readLine().trim();
            while (line != null) {
                recentWords.add(line);
                line = reader.readLine().trim();
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!recentWords.isEmpty() && recentWords.get(0).equals(word))
            return;

        try {
            FileOutputStream fileout = openFileOutput("recent_words.txt", MODE_PRIVATE);
            OutputStreamWriter outputWriter = new OutputStreamWriter(fileout);
            outputWriter.write("");
            outputWriter.append(word + "\n");
            for (String str : recentWords)
                outputWriter.append(str + "\n");
            outputWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return;
    }

    public void searchWord(String word) {
        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            // Get a reference to the LoaderManager, in order to interact with loaders.
            LoaderManager loaderManager = getSupportLoaderManager();

            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
            Log.i(LOG_TAG, "TEST: calling initLoader() ...");

            // Create a bundle called queryBundle
            Bundle queryBundle = new Bundle();

            // Use putString with queryWord as the key and the String value of the word
            queryBundle.putString("queryWord", word);

            // Get our Loader by calling getLoader and passing the ID we specified
            Loader<String> loader = loaderManager.getLoader(FEATURE_LOADER_ID1);

            // If the Loader was null, initialize it. Else, restart it.
            if (loader == null) {
                loaderManager.initLoader(FEATURE_LOADER_ID1, queryBundle, this);
            } else {
                loaderManager.restartLoader(FEATURE_LOADER_ID1, queryBundle, this);
            }
        } else {
            //Update empty state with no connection error message
            Toast.makeText(this, "No Internet Connection!!", Toast.LENGTH_SHORT).show();
        }
    }

    public void onBackPressed() {
        Intent startMain = new Intent(getBaseContext(), MainActivity.class);
        startMain.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(startMain);
    }

    @Override
    public Loader<String> onCreateLoader(int id, final Bundle args) {
        // Create a new loader for the given URL
        Log.i(LOG_TAG, "TEST: onCreateLoader() called ...");

        // Hide loading indicator because the data has been loaded
        View loadingIndicator = findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.VISIBLE);

        String word = args.getString("queryWord");

        Uri baseUri = Uri.parse(ODAPI_ENTRIES_REQUEST_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        uriBuilder.appendPath("en");
        uriBuilder.appendPath(word.toLowerCase());

        String alternateUri = uriBuilder.toString();
        return new FeatureLoader(this, alternateUri, word, 1);
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String definition) {
        // Hide loading indicator because the data has been loaded
        View loadingIndicator = findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);
        ImageView fav = findViewById(R.id.fav);

        Log.i(LOG_TAG, "TEST: onLoadFinished() called ...");

        // If there is a valid {@link definition}, then update TextView
        if (definition != null && !definition.isEmpty()) {
            if (loader.getId() == 1) {
                TextView word_View = (TextView) findViewById(R.id.word);
                TextView def_View = (TextView) findViewById(R.id.def);
                word_View.setText(word);
                def_View.setText(definition);
                addToRecents();
                fav.setVisibility(View.VISIBLE);
            }
        } else {
            Toast.makeText(this, "No results found!!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, SearchActivity.class));
        }
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {
        Log.i(LOG_TAG, "TEST: onLoaderReset() called ...");
        // Loader reset, so we can clear out our existing data.
    }
}