package com.google.engedu.wordstack;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Calendar;

public class DictionaryFragment extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState){
            View rootView = inflater.inflate(R.layout.fragment_dictionary, container, false);
            TextView word_of_day = (TextView) rootView.findViewById(R.id.word_of_day);
            word_of_day.setText(getDate());
            return rootView;
        }

    public String getDate(){
        Calendar calendar = Calendar.getInstance();
        String currentDate = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.getTime());
        return currentDate;
    }
}
