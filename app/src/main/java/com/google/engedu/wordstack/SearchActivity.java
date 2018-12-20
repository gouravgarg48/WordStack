package com.google.engedu.wordstack;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SearchActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String> {
    private static final String LOG_TAG = MainActivity.class.getName();
    private static String word;

    private static final String ODAPI_ENTRIES_REQUEST_URL = "https://od-api.oxforddictionaries.com:443/api/v1/entries";
    private static final int FEATURE_LOADER_ID1 = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        final EditText search = (EditText) findViewById(R.id.search);
        search.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    word = search.getText().toString();
                    searchWord(word);
                    return true;
                }
                return false;
            }
        });
    }

    public void searchWord(String word) {
        View SearchView = findViewById(R.id.search_view);
        SearchView.setVisibility(View.GONE);

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
            SearchView.setVisibility(View.VISIBLE);
        }
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

        Log.i(LOG_TAG, "TEST: onLoadFinished() called ...");

        // If there is a valid {@link definition}, then update TextView
        if (definition != null && !definition.isEmpty()) {
            if (loader.getId() == 1) {
                TextView word_View = (TextView) findViewById(R.id.word);
                TextView def_View = (TextView) findViewById(R.id.def);
                word_View.setText(word);
                def_View.setText(definition);
            }
        } else {
            Toast.makeText(this, "No results found!!", Toast.LENGTH_SHORT).show();
            View SearchView = findViewById(R.id.search_view);
            SearchView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {
        Log.i(LOG_TAG, "TEST: onLoaderReset() called ...");
        // Loader reset, so we can clear out our existing data.
    }
}
