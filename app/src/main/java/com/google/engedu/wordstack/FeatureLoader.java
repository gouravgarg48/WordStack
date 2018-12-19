package com.google.engedu.wordstack;

import android.support.v4.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;
import java.io.IOException;

/**
 * Loads definitions by using an AsyncTask to perform the
 * network request to the given URL.
 */
public class FeatureLoader extends AsyncTaskLoader<String> {

    /**
     * Tag for log messages
     */
    private static final String LOG_TAG = FeatureLoader.class.getName();

    /**
     * Query URL
     */
    private String mUrl;

    /**
     * Query Word
     */
    private String mWord;

    /**
     * Query Type
     */
    private int mQType;

    /**
     * Constructs a new {@link FeatureLoader}.
     *
     * @param context of the activity
     * @param url     to load data from
     * @param word    to load feature
     * @param QType   for type of Query
     */
    public FeatureLoader(Context context, String url, String word, int QType) {
        super(context);
        mUrl = url;
        mWord = word;
        mQType = QType;
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
        String result="";
        try {
            // Perform the network request, parse the response, and extract definition.
            if(mQType == 1)
                result = QueryUtils.fetchDetails(mUrl, mWord);
            else
                result = QueryUtils.fetchDefinition(mUrl, mWord);
//            if(definition != null)
//                Log.v(LOG_TAG, definition);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

}

