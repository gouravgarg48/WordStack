package com.google.engedu.wordstack;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

import javax.net.ssl.HttpsURLConnection;

/**
 * Helper methods related to requesting and receiving definition data from OD.
 */
public final class QueryUtils {

    public static final String LOG_TAG = MainActivity.class.getName();

    final static String app_id = "20f00228";
    final static String app_key = "497efb5ac3f5c2d366b3e6125e73368e";

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private QueryUtils() {
    }

    /**
     * Query the OD dataset and return a {@link String} object.
     */
    public static String fetchDefinition(String requestUrl, String queryWord) throws IOException{
        Log.i(LOG_TAG, "TEST: fetchDefinition() called ...");

        URL url = createUrl(requestUrl);
        HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();

        urlConnection.addRequestProperty("Accept","application/json");
        urlConnection.addRequestProperty("app_id",app_id);
        urlConnection.addRequestProperty("app_key",app_key);

        Log.v(LOG_TAG,  "For Url: " + url.toString());

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;

        try {
            jsonResponse = makeHttpRequest(urlConnection);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        // Extract relevant fields from the JSON response and create a {@link String}
        String definition = extractFeatureFromJson(jsonResponse);

        // Return the {@link String}
        return definition;
    }

    /**
     * Return a list of {@link String} objects that has been built up from
     * parsing a JSON response.
     */
    public static String extractFeatureFromJson(String result) {

        // If the JSON string is empty or null, then return early.
        if(TextUtils.isEmpty(result))
            return null;

        // Create an empty String to store definition
        String definition = new String();

        // Try to parse the JSON response string. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {

            // Create a JSONObject from the JSON response string
            JSONObject baseJSONResponse = new JSONObject(result);

            // Extract the JSONObject associated with the key called "results",
            // which represents a list of results.
            JSONObject resultsArrayJSONObject = baseJSONResponse.getJSONArray("results").getJSONObject(0);

            JSONObject lexicalEntriesJSONObject = resultsArrayJSONObject.getJSONArray("lexicalEntries").getJSONObject(0);

            JSONObject entriesJSONObject = lexicalEntriesJSONObject.getJSONArray("entries").getJSONObject(0);

            JSONObject sensesJSONObject = entriesJSONObject.getJSONArray("senses").getJSONObject(0);

            definition = (String) sensesJSONObject.getJSONArray("definitions").get(0);

            Log.v("QueryUtils", "Definition = " + definition);

        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("QueryUtils", "Problem parsing the definitions JSON results", e);
        }

        // Return the list of definitions
        return definition;
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(HttpsURLConnection urlConnection) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (urlConnection == null) {
            return jsonResponse;
        }

        InputStream inputStream = null;
        try {
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
                return jsonResponse;
            } else {
                Log.e(LOG_TAG,"Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the definition JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies than an IOException
                // could be thrown.
                inputStream.close();
            }
        }
        return null;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

}