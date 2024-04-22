package com.nt118.proma.ui.search;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.nt118.proma.R;

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
        searchField.setOnEditorActionListener((v, actionId, event) -> {
            // do nothing
            return true;
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
        showKeyboard(searchField);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void showKeyboard(View view) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }
}
