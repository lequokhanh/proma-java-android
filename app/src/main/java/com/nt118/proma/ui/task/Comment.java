package com.nt118.proma.ui.task;

import static com.nt118.proma.ui.task.TaskDetail.setWindowFlag;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FirebaseFirestore;
import com.nt118.proma.R;

import java.util.ArrayList;
import java.util.Map;

public class Comment extends AppCompatActivity {
    private final MutableLiveData<ArrayList<Map<String, Object>>> comments = new MutableLiveData<>();
    private String taskId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_comment);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        Intent intent = getIntent();
        taskId = intent.getStringExtra("taskId");
        loadDB();

        ImageView sendBtn = findViewById(R.id.sendBtn);
        sendBtn.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
            String name = sharedPreferences.getString("name", "");
            String email = sharedPreferences.getString("email", "");
            TextView etComment = findViewById(R.id.etComment);
            if (etComment.getText().toString().isEmpty()) {
                etComment.setError("Comment cannot be empty");
                return;
            }
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("tasks").document(taskId).collection("comments").add(Map.of(
                    "name", name,
                    "email", email,
                    "message", etComment.getText().toString(),
                    "date", String.valueOf(System.currentTimeMillis())
            )).addOnSuccessListener(documentReference -> {
                loadDB();
                etComment.setText("");
            });
        });

        comments.observe(this, commentList -> {
            TextView commentTitle = findViewById(R.id.commentTitle);
            commentTitle.setText("Comments (" + commentList.size() + ")");
            LinearLayout commentContainer = findViewById(R.id.commentContainer);
            commentContainer.removeAllViews();
            for (Map<String, Object> comment : commentList) {
                View item_comment = getLayoutInflater().inflate(R.layout.item_comment, null);
                TextView name = item_comment.findViewById(R.id.nameCmt);
                TextView content = item_comment.findViewById(R.id.contentCmt);
                TextView date = item_comment.findViewById(R.id.dateCmt);
                name.setText((String) comment.get("name"));
                content.setText((String) comment.get("message"));
                date.setText((String) comment.get("date"));
                commentContainer.addView(item_comment);
            }
        });
    }

    private void loadDB() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("tasks").document(taskId).collection("comments").get().addOnSuccessListener(queryDocumentSnapshots -> {
            ArrayList<Map<String, Object>> commentList = new ArrayList<>();
            for (int i = 0; i < queryDocumentSnapshots.size(); i++) {
                commentList.add(queryDocumentSnapshots.getDocuments().get(i).getData());
                int finalI = i;
                db.collection("users").whereEqualTo("email", queryDocumentSnapshots.getDocuments().get(i).get("email")).get().addOnSuccessListener(queryDocumentSnapshots1 -> {
                    commentList.get(finalI).put("name", queryDocumentSnapshots1.getDocuments().get(0).get("name"));
                    db.collection("tasks").document(taskId).collection("comments").document(queryDocumentSnapshots.getDocuments().get(finalI).getId()).update("name", queryDocumentSnapshots1.getDocuments().get(0).get("name"));
                });
            }
            comments.setValue(commentList);
        });
    }
}