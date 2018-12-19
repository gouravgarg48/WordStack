/* Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.engedu.wordstack;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;


public class GameFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<String> {

    /**
     * Tag for log messages
     */
    private static final String LOG_TAG = MainActivity.class.getName();

    private static int WORD_LENGTH = 5;
    private static int hintValue = 10;
    public static final int LIGHT_BLUE = Color.rgb(176, 200, 255);
    public static final int LIGHT_GREEN = Color.rgb(200, 255, 200);
    private ArrayList<String> words = new ArrayList<>();
    private Random random = new Random();
    private StackedLayout stackedLayout;
    private static String word1, word2, definition1, definition2;
    private Stack<LetterTile> placedTiles = new Stack<LetterTile>();
    private View rootView;

    private static final String ODAPI_REQUEST_URL = "https://od-api.oxforddictionaries.com:443/api/v1/entries";
    private static final int DEFINITION_LOADER_ID1 = 1;
    private static final int DEFINITION_LOADER_ID2 = 2;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootView = inflater.inflate(R.layout.fragment_game, container, false);

        ImageView start = (ImageView) rootView.findViewById(R.id.start);
        ImageView undo = (ImageView) rootView.findViewById(R.id.undo);
        ImageView reset = (ImageView) rootView.findViewById(R.id.reset);
        ImageView hint = (ImageView) rootView.findViewById(R.id.hint);
        ImageView instr = (ImageView) rootView.findViewById(R.id.instr);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStartGame();
            }
        });
        undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onUndo();
            }
        });
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reset();
            }
        });
        hint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getHint();
            }
        });
        instr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messageBox();
            }
        });

        LinearLayout verticalLayout = (LinearLayout) rootView.findViewById(R.id.vertical_layout);
        stackedLayout = new StackedLayout(rootView.getContext());
        verticalLayout.addView(stackedLayout, 3);

        View word1LinearLayout = rootView.findViewById(R.id.word1);
        word1LinearLayout.setOnTouchListener(new TouchListener());
        word1LinearLayout.setOnDragListener(new DragListener());

        View word2LinearLayout = rootView.findViewById(R.id.word2);
        word2LinearLayout.setOnTouchListener(new TouchListener());
        word2LinearLayout.setOnDragListener(new DragListener());
        return rootView;
    }

    private class TouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN && !stackedLayout.empty()) {
                LetterTile tile = (LetterTile) stackedLayout.peek();
                tile.moveToViewGroup((ViewGroup) v);
                if (stackedLayout.empty()) {
                    loadDefinition();
                    checkAns();

                    ImageView play = (ImageView) rootView.findViewById(R.id.start);
                    play.setEnabled(false);

                    ImageView hint = (ImageView) rootView.findViewById(R.id.hint);
                    hint.setVisibility(View.INVISIBLE);
                }

                placedTiles.push(tile);
                return true;
            }
            return false;
        }
    }

    private class DragListener implements View.OnDragListener {

        public boolean onDrag(View v, DragEvent event) {
            int action = event.getAction();
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    v.setBackgroundColor(LIGHT_BLUE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    v.setBackgroundColor(LIGHT_GREEN);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    v.setBackgroundColor(LIGHT_BLUE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    v.setBackgroundColor(Color.WHITE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DROP:
                    // Dropped, reassign Tile to the target Layout
                    LetterTile tile = (LetterTile) event.getLocalState();
                    if (!placedTiles.contains(tile)) {
                        tile.moveToViewGroup((ViewGroup) v);
                        placedTiles.push(tile);
                    }
                    if (stackedLayout.empty()) {
                        loadDefinition();
                        checkAns();

                        ImageView play = (ImageView) rootView.findViewById(R.id.start);
                        play.setEnabled(false);

                        ImageView hint = (ImageView) rootView.findViewById(R.id.hint);
                        hint.setVisibility(View.INVISIBLE);
                    }
                    return true;
            }
            return false;
        }
    }

    public void loadWords(int length) {
        AssetManager assetManager = rootView.getContext().getAssets();
        words.clear();
        try {
            InputStream inputStream = assetManager.open("words.txt");
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            while ((line = in.readLine()) != null) {
                String word = line.trim();
                if (word.length() == length) {
                    words.add(word);
                }
            }
        } catch (IOException e) {
            Toast.makeText(rootView.getContext(), "Could not load dictionary", Toast.LENGTH_LONG).show();
        }
    }

    public boolean onStartGame() {
        ImageView play = (ImageView) rootView.findViewById(R.id.start);
        play.setEnabled(false);

        TextView messageBox = (TextView) rootView.findViewById(R.id.message_box);
        messageBox.setText("Game started...");

        EditText size = (EditText) rootView.findViewById(R.id.size);
        WORD_LENGTH = Integer.parseInt(size.getText().toString());
        size.setVisibility(View.GONE);

        ImageView hint = (ImageView) rootView.findViewById(R.id.hint);
        hint.setVisibility(View.VISIBLE);

        loadWords(WORD_LENGTH);

        int index1 = random.nextInt(words.size());
        int index2 = random.nextInt(words.size());
        word1 = words.get(index1);
        word2 = words.get(index2);

        String word = "";
        for (int i = 0, j = 0; i < word1.length() || j < word2.length(); ) {
            if (random.nextInt(2) == 0 && i < word1.length()) {
                word += String.valueOf(word1.charAt(i));
                i++;
            } else if (j < word2.length()) {
                word += String.valueOf(word2.charAt(j));
                j++;
            }
        }

        ViewGroup word1LinearLayout = rootView.findViewById(R.id.word1);
        ViewGroup word2LinearLayout = rootView.findViewById(R.id.word2);
        word1LinearLayout.removeAllViews();
        word2LinearLayout.removeAllViews();

        stackedLayout.clear();

        for (int i = word.length() - 1; i >= 0; --i)
            stackedLayout.push(new LetterTile(rootView.getContext(), word.charAt(i)));

        return true;
    }

    public boolean checkAns() {
        View word1LinearLayout = rootView.findViewById(R.id.word1);
        View word2LinearLayout = rootView.findViewById(R.id.word2);

        Log.v(LOG_TAG, word1);
        Log.v(LOG_TAG, word2);

        String resultWord1 = getWord(word1LinearLayout);
        String resultWord2 = getWord(word2LinearLayout);

        if ((resultWord1.equals(word1) && resultWord2.equals(word2)) ||
                (resultWord1.equals(word2) && resultWord2.equals(word1))) {
            AlertDialog alertDialog = new AlertDialog.Builder(rootView.getContext()).create();
            alertDialog.setMessage("!! You Won !!");
            alertDialog.show();
            return true;
        }
        return false;
    }

    public boolean onUndo() {
        if (!placedTiles.empty()) {
            LetterTile tile = placedTiles.pop();
            tile.moveToViewGroup(stackedLayout);
            return true;
        }
        return false;
    }

    public boolean getHint() {
        if (hintValue > 0) {
            Toast.makeText(rootView.getContext(), "Hint: " + word1, Toast.LENGTH_SHORT).show();
            hintValue--;
        }
        return true;
    }

    public boolean reset() {
        TextView messageBox = (TextView) rootView.findViewById(R.id.message_box);
        messageBox.setText("Enter size of word: ");

        EditText size = (EditText) rootView.findViewById(R.id.size);
        size.setVisibility(View.VISIBLE);

        ImageView play = (ImageView) rootView.findViewById(R.id.start);
        play.setEnabled(true);

        ViewGroup word1LinearLayout = rootView.findViewById(R.id.word1);
        word1LinearLayout.removeAllViews();
        word1LinearLayout.setBackgroundColor(getResources().getColor(R.color.background));

        ViewGroup word2LinearLayout = rootView.findViewById(R.id.word2);
        word2LinearLayout.removeAllViews();
        word2LinearLayout.setBackgroundColor(getResources().getColor(R.color.background));

        words.removeAll(words);
        stackedLayout.removeAllViews();
        stackedLayout.clear();
        return true;
    }

    public boolean messageBox() {
        AlertDialog alertDialog = new AlertDialog.Builder(rootView.getContext()).create();
        alertDialog.setTitle("Instructions");
        alertDialog.setMessage(getResources().getString(R.string.instructions));
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "CLOSE",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
        return true;
    }

    public String getWord(View v) {
        String result = "";
        ViewGroup viewGroup = (ViewGroup) v;

        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            LetterTile child = (LetterTile) viewGroup.getChildAt(i);
            Stack<LetterTile> letterStack = new Stack<LetterTile>();
            letterStack.add(child);

            Character ch = child.getLetter();
            result += ch;
        }
        return result;
    }

    private void loadDefinition() {
        TextView message_Box_View = (TextView) rootView.findViewById(R.id.message_box);
        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager) rootView.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            // Get a reference to the LoaderManager, in order to interact with loaders.
            LoaderManager loaderManager = getLoaderManager();

            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
            Log.i(LOG_TAG, "TEST: calling initLoader() ...");

            // Create a bundle called queryBundle
            Bundle queryBundle1 = new Bundle();
            Bundle queryBundle2 = new Bundle();

            // Use putString with queryWord as the key and the String value of the word
            queryBundle1.putString("queryWord", word1);
            queryBundle2.putString("queryWord", word2);

            // Get our Loader by calling getLoader and passing the ID we specified
            Loader<String> loader1 = loaderManager.getLoader(DEFINITION_LOADER_ID1);
            Loader<String> loader2 = loaderManager.getLoader(DEFINITION_LOADER_ID2);

            // If the Loader was null, initialize it. Else, restart it.
            if (loader1 == null) {
                loaderManager.initLoader(DEFINITION_LOADER_ID1, queryBundle1, this);
            } else {
                loaderManager.restartLoader(DEFINITION_LOADER_ID1, queryBundle1, this);
            }

            if (loader2 == null) {
                loaderManager.initLoader(DEFINITION_LOADER_ID2, queryBundle2, this);
            } else {
                loaderManager.restartLoader(DEFINITION_LOADER_ID2, queryBundle2, this);
            }

        } else {
            //Update empty state with no connection error message
            message_Box_View.setText("No Internet Connection!!");
        }
    }

    @Override
    public Loader<String> onCreateLoader(int id, final Bundle args) {
        // Create a new loader for the given URL
        Log.i(LOG_TAG, "TEST: onCreateLoader() called ...");

        // Hide loading indicator because the data has been loaded
        View loadingIndicator = rootView.findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.VISIBLE);

        String word = args.getString("queryWord");

        Uri baseUri = Uri.parse(ODAPI_REQUEST_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        uriBuilder.appendQueryParameter("language", "en");
        uriBuilder.appendQueryParameter("word_id", word.toLowerCase());

        String alternateUri = ODAPI_REQUEST_URL + "/en/" + word.toLowerCase().trim();

        return new DefinitionLoader(rootView.getContext(), alternateUri, word);
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String definition) {
        // Hide loading indicator because the data has been loaded
        View loadingIndicator = rootView.findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);

        Log.i(LOG_TAG, "TEST: onLoadFinished() called ...");

        // If there is a valid {@link definition}, then update TextView
        if (definition != null && !definition.isEmpty()) {
            if (loader.getId() == 1) definition1 = definition;
            else if (loader.getId() == 2) definition2 = definition;
        } else {
            if (loader.getId() == 1) definition1 = null;
            else if (loader.getId() == 2) definition2 = null;
        }
        displayResults();
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {
        Log.i(LOG_TAG, "TEST: onLoaderReset() called ...");
        // Loader reset, so we can clear out our existing data.
    }

    public void displayResults() {
        TextView message_Box_View = (TextView) rootView.findViewById(R.id.message_box);
        message_Box_View.setText("Answer: ");
        if (definition1 == null || definition2 == null)
            message_Box_View.append(word1 + ", " + word2);
        else {
            message_Box_View.append("\n" + word1 + ": " + definition1);
            message_Box_View.append("\n" + word2 + ": " + definition2);
        }
    }
}
