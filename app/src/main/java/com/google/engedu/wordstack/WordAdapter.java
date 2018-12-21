package com.google.engedu.wordstack;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class WordAdapter extends ArrayAdapter<String> {
    /**
     * Create a new {@link WordAdapter} object.
     *
     * @param context is the current context (i.e. Activity) that the adapter is being created in.
     * @param words   is the list of {@link String}s to be displayed.
     */
    public WordAdapter(Context context, ArrayList<String> words) {
        super(context, 0, words);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Check if an existing view is being reused, otherwise inflate the view
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_item, parent, false);
        }

        // Get the {@link Word} object located at this position in the list
        String currentWord = getItem(position);

        // Find the TextView in the list_item.xml layout with the ID word_view.
        TextView wordView = (TextView) listItemView.findViewById(R.id.word);
        wordView.setText(currentWord);

        // Return the whole list item layout so that it can be shown in the ListView.
        return listItemView;
    }
}