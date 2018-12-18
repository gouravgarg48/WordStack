package com.google.engedu.wordstack;

import android.support.v4.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;
import java.io.IOException;

/**
 * Loads definitions by using an AsyncTask to perform the
 * network request to the given URL.
 */
public class DefinitionLoader extends AsyncTaskLoader<String> {

    /**
     * Tag for log messages
     */
    private static final String LOG_TAG = DefinitionLoader.class.getName();

    /**
     * Query URL
     */
    private String mUrl;

    /**
     * Query Word
     */
    private String mWord;

    /**
     * Constructs a new {@link DefinitionLoader}.
     *
     * @param context of the activity
     * @param url     to load data from
     * @param word    to load definition
     */
    public DefinitionLoader(Context context, String url, String word) {
        super(context);
        mUrl = url;
        mWord = word;
    }

    @Override
    protected void onStartLoading() {
        Log.i(LOG_TAG, "TEST: onStartLoading() called ...");
        forceLoad();
    }

    @Override
    public String loadInBackground() {
        Log.i(LOG_TAG, "TEST: loadInBackground() called ...");
        if (mUrl == null) {
            return null;
        }
        String definition="";
        try {
            // Perform the network request, parse the response, and extract definition.
            definition = QueryUtils.fetchDefinition(mUrl, mWord);
            if(definition != null)
                Log.v(LOG_TAG, definition);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return definition;
    }

}

