package com.mishaki.searchspinnertest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.mishaki.searchspinner.adapter.SearchSpinnerAdapter;
import com.mishaki.searchspinner.view.SearchSpinner;
import com.mishaki.searchspinner.view.StringSearchSpinner;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button search_spinner_shot_btn = (Button) findViewById(R.id.search_spinner_shot_btn);
        final StringSearchSpinner search_spinner_ss = (StringSearchSpinner) findViewById(R.id.search_spinner_ss);
//        final SearchSpinner<String> search_spinner_ss = (SearchSpinner<String>) findViewById(R.id.search_spinner_ss);
        ArrayList<String> list = new ArrayList<>();
        final int max = 11;
        for (int i = 0; i < max; i++) {
            list.add(String.valueOf(i));
        }
        for (int i = 0; i < max; i++) {
            list.add(String.valueOf(i));
        }
        for (int i = 0; i < max; i++) {
            list.add(String.valueOf(i));
        }
        for (int i = 0; i < max; i++) {
            list.add(String.valueOf(i));
        }
        list.add(null);
        search_spinner_ss.setList(list);
        Button search_spinner_btn = (Button) findViewById(R.id.search_spinner_btn);
        search_spinner_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectIndex = search_spinner_ss.getSelectIndex();
                search_spinner_shot_btn.setText(String.valueOf(selectIndex));
            }
        });
        search_spinner_ss.setOnSelectListener(new SearchSpinner.OnSelectListener() {
            @Override
            public void onSelect(@NotNull SearchSpinner spinner, int position) {
                search_spinner_shot_btn.setText(String.valueOf(position));
            }
        });
        search_spinner_ss.setSelectIndex(5);
        search_spinner_shot_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchSpinnerAdapter<String> adapter = new SearchSpinnerAdapter<String>(MainActivity.this);
                search_spinner_ss.setAdapter(adapter);
            }
        });
    }

}
