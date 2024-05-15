package com.nt118.proma.ui.search;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

import com.nt118.proma.R;

import java.util.ArrayList;

public class SearchView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        EditText searchField = findViewById(R.id.search_field);
        ImageView closeBtn = findViewById(R.id.close_btn);
        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) {
                    closeBtn.setVisibility(View.GONE);
                } else {
                    closeBtn.setVisibility(View.VISIBLE);
                }
            }
        });
        closeBtn.setOnClickListener(v -> {
            searchField.setText("");
        });
        TextView cancelBtn = findViewById(R.id.cancel_btn);
        cancelBtn.setOnClickListener(v -> {
            finish();
        });
        // perform focus on search field and show keyboard
        searchField.requestFocus();
        MutableLiveData<ArrayList<String>> searchRecent = new MutableLiveData<>();
        ArrayList<String> recent = new ArrayList<>();
        recent.add("Recent search 1");
        recent.add("Recent search 2");
        recent.add("Recent search 3");
        searchRecent.setValue(recent);
        LinearLayout searchRecentContainer = findViewById(R.id.searchRecentContainer);
        searchRecent.observe(this, strings -> {
            searchRecentContainer.removeAllViews();
            for (String string : strings) {
                View view = getLayoutInflater().inflate(R.layout.search_recent_item, null);
                // set margin bottom for each item
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                // margin bottom 16dp
                // convert dp to pixel
                float dip = 16f;
                Resources r = getResources();
                float px = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        dip,
                        r.getDisplayMetrics()
                );
                params.setMargins(0, 0, 0, (int) px);
                view.setLayoutParams(params);
                TextView recentItem = view.findViewById(R.id.name);
                ImageView removeBtn = view.findViewById(R.id.removeBtn);
                recentItem.setText(string);
                searchRecentContainer.addView(view);
                view.setOnClickListener(v -> {
                    searchField.setText(string);
                    searchField.setSelection(string.length());
                    searchField.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(searchField, InputMethodManager.SHOW_IMPLICIT);
                });
                removeBtn.setOnClickListener(v -> {
                    recent.remove(string);
                    searchRecent.setValue(recent);
                });
            }
        });
        searchField.setOnEditorActionListener((v, actionId, event) -> {
            // hide keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(searchField.getWindowToken(), 0);
            // do search
            // add search string to recent search
            if (!recent.contains(searchField.getText().toString())) {
                recent.add(searchField.getText().toString());
                searchRecent.setValue(recent);
            }
            return true;
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}
