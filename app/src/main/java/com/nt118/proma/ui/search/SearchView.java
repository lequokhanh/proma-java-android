package com.nt118.proma.ui.search;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nt118.proma.R;
import com.nt118.proma.model.ImageArray;
import com.nt118.proma.ui.project.ProjectDetail;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SearchView extends AppCompatActivity {

    Dialog loading;
    private FirebaseFirestore db;
    private MutableLiveData<ArrayList<String>> searchRecent;
    private ArrayList<String> recent;
    private EditText searchField;
    private ImageView closeBtn;
    private LinearLayout searchRecentContainer;
    private String user;
    private SharedPreferences sharedPreferences;
    private boolean isSearchActive = false; // follow status search


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initUi();
        db = FirebaseFirestore.getInstance();

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
        searchRecent = new MutableLiveData<>();
        recent = new ArrayList<>();
        searchRecent.setValue(recent);

        addItemSearch();

        searchField.setOnEditorActionListener((v, actionId, event) -> {
            // hide keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(searchField.getWindowToken(), 0);
            // do search
            loading.show();
            String searchText = searchField.getText().toString().trim();
            recent.add(searchText);
            searchRecent.setValue(recent);

            List<String> projectNames = new ArrayList<>();
            db.collection("projects").where(Filter.or(
                                    Filter.equalTo("user_created", user),
                                    Filter.arrayContains("members", Map.of("email", user,
                                            "isAccepted", true)
                                    )
                            )
                    )
                    .get()
                    .addOnSuccessListener(res -> {
                        loading.dismiss();
                        if (!res.isEmpty()) {
                            boolean hasMatch = false;
                            searchRecentContainer.removeAllViews();
                            for (QueryDocumentSnapshot doc : res) {
                                if (doc.get("name").toString().contains(searchText)) {
                                    hasMatch = true;
                                    View view = getLayoutInflater().inflate(R.layout.project_card, null);
                                    TextView name = view.findViewById(R.id.projectName);
                                    TextView description = view.findViewById(R.id.projectDescription);
                                    TextView deadline = view.findViewById(R.id.deadline);
                                    ImageView cover = view.findViewById(R.id.cover_project);
                                    view.findViewById(R.id.progressProject).setVisibility(View.GONE);
                                    name.setText(doc.get("name").toString());
                                    description.setText(doc.get("description").toString());
                                    deadline.setText(doc.get("deadline").toString());
                                    if (doc.get("cover") != null) {
                                        cover.setImageResource(ImageArray.getCoverProjectImage().get(doc.getLong("cover").intValue()));
                                    }
                                    float dip10 = 10f;
                                    Resources r = getResources();
                                    float px10 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip10, r.getDisplayMetrics());
                                    searchRecentContainer.addView(view);
                                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
                                    layoutParams.setMargins(0, 0, 0, (int) px10);
                                    String projectId = doc.getId();
                                    view.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intent = new Intent(SearchView.this, ProjectDetail.class);
                                            intent.putExtra("projectId", projectId);
                                            startActivity(intent);
                                        }
                                    });
                                }
                            }
                            isSearchActive = true;
                            if (!hasMatch) {
                                // No project matches the search term
                                showNoMatchDialog();
                                addItemSearch();
                            }
                        } else {
                            // No documents found
                            showNoMatchDialog();
                            addItemSearch();
                        }
                    })
                    .addOnFailureListener(e -> {
                        loading.dismiss();
                        // Handle any errors that occurred during the query
                        Log.e("SearchError", "Error searching projects", e);
                    });

            return true;
        });
    }

    // Function to show the "no match" dialog
    private void showNoMatchDialog() {
        Dialog noMatchDialog = new Dialog(SearchView.this);
        noMatchDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        noMatchDialog.setContentView(R.layout.no_match_result);
        noMatchDialog.show();
    }


    private void initUi() {
        searchField = findViewById(R.id.search_field);
        closeBtn = findViewById(R.id.close_btn);
        sharedPreferences = getSharedPreferences("user", Context.MODE_PRIVATE);
        user = sharedPreferences.getString("email", "");
        loading = createLoadingDialog();
    }

    private void addItemSearch() {
        searchRecentContainer = findViewById(R.id.searchRecentContainer);
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

    }

    public Dialog createLoadingDialog() {
        Dialog loading = new Dialog(this);
        loading.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        loading.setContentView(R.layout.loading);
        return loading;
    }

    @Override
    public void onBackPressed() {
        // Kiểm tra trạng thái tìm kiếm
        if (isSearchActive) {
            // Gọi hàm bạn muốn thực hiện khi nhấn nút "Back" trong trạng thái tìm kiếm
            addItemSearch();
            // Đặt lại trạng thái tìm kiếm để cho phép thoát nếu người dùng nhấn "Back" lần nữa
            isSearchActive = false;
        } else {
            // Gọi super.onBackPressed() để Activity thoát
            super.onBackPressed();
        }
    }

}
