package com.google.engedu.wordstack;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Calendar;

public class DictionaryFragment extends Fragment {
    private static View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_dictionary, container, false);

        View search_view = (View) rootView.findViewById(R.id.search);
        search_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(rootView.getContext(), SearchActivity.class);
                startActivity(i);
            }
        });

        View word_of_day_view = (View) rootView.findViewById(R.id.word_of_day_view);
        word_of_day_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchActivity ob = new SearchActivity();
                TextView word_of_day = (TextView) rootView.findViewById(R.id.word_of_day);
//                ob.searchWord(word_of_day.getText().toString());
//                Intent i = new Intent(rootView.getContext(), SearchActivity.class);
//                startActivity(i);
            }
        });
        getDate();
        return rootView;
    }

    public void getDate() {
        Calendar calendar = Calendar.getInstance();
        String currentDate = DateFormat.getDateInstance(DateFormat.MEDIUM).format(calendar.getTime());

        TextView word_of_day = (TextView) rootView.findViewById(R.id.Date);
        word_of_day.setText(currentDate);
        return;
    }
}
