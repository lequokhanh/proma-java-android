package com.nt118.proma.ui.task;

import static com.nt118.proma.ui.task.TaskDetail.setWindowFlag;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nt118.proma.R;
import com.nt118.proma.model.ImageArray;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Comment extends AppCompatActivity {
    private final MutableLiveData<ArrayList<Map<String, Object>>> comments = new MutableLiveData<>();
    private String taskId, projectId;
    private ImageView imgBack;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_comment);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        initUI();

        Intent intent = getIntent();
        taskId = intent.getStringExtra("taskId");
        projectId = intent.getStringExtra("projectId");
        imgBack.setOnClickListener(v -> {
            finish();
        });

        showComments();

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
            db = FirebaseFirestore.getInstance();
            db.collection("tasks").document(taskId).collection("comments").add(Map.of(
                    "name", name,
                    "email", email,
                    "message", etComment.getText().toString(),
                    "date", String.valueOf(System.currentTimeMillis())
            )).addOnSuccessListener(documentReference -> {
                db.collection("tasks").document(taskId).get().addOnSuccessListener(documentSnapshot -> {
                    ArrayList<Map<String, Object>> members = (ArrayList<Map<String, Object>>) documentSnapshot.get("members");
                    for (Map<String, Object> member : members) {
                        if (!member.get("email").equals(email)) {
                            db.collection("users").whereEqualTo("email", member).get().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Map<String, Object> user = task.getResult().getDocuments().get(0).getData();
                                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                                    db.collection("tasks").document(taskId).collection("notification_logs").add(new HashMap<String, Object>() {{
                                        put("type", 2);
                                        put("message", "commented on task " + taskId);
                                        put("date", sdf.format(new Date()));
                                        put("sender", email);
                                        put("taskId", taskId);
                                        put("projectId", projectId);
                                    }});
                                }
                            });
                        }
                    }
                });
                showComments();
                etComment.setText("");
            });
        });

        comments.observe(this, commentList -> {
            TextView commentTitle = findViewById(R.id.commentTitle);
            if(commentList.size() == 0){
                commentTitle.setText("Comments (0)");
            }else {
                commentTitle.setText("Comments (" + commentList.size() + ")");
            }
            LinearLayout commentContainer = findViewById(R.id.commentContainer);
            commentContainer.removeAllViews();
            for (Map<String, Object> comment : commentList) {
                View item_comment = getLayoutInflater().inflate(R.layout.item_comment, null);
                TextView name = item_comment.findViewById(R.id.nameCmt);
                TextView content = item_comment.findViewById(R.id.contentCmt);
                TextView date = item_comment.findViewById(R.id.dateCmt);
                ImageView avatar = item_comment.findViewById(R.id.imageView123);

                String email= (String) comment.get("email");
                name.setText((String) comment.get("name"));
                content.setText((String) comment.get("message"));

                // format date
                long timestamp = Long.parseLong((String) comment.get("date"));
                String formattedDate = formatDateTime(timestamp);
                date.setText(formattedDate);

                db.collection("users").whereEqualTo("email", email).get().addOnCompleteListener(task2 -> {
                            if (task2.isSuccessful()) {// Get the avatar ID and set the image resource
                                Map<String, Object> user = task2.getResult().getDocuments().get(0).getData();
                                Object avatarObj = user.get("avatar");
                                int avatarIndex = Math.toIntExact((Long) avatarObj);
                                avatar.setImageResource(new ImageArray().getAvatarImage().get(avatarIndex));
                            }
                        });

                commentContainer.addView(item_comment);
            }
        });
    }

    private void showComments() {
        db = FirebaseFirestore.getInstance();
        db.collection("tasks").document(taskId).collection("comments")
                .orderBy("date", Query.Direction.ASCENDING)   // sort by date in ascending order
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Map<String, Object>> commentList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            commentList.add(document.getData());
                        }
                        comments.setValue((ArrayList<Map<String, Object>>) commentList);  // update comments LiveData
                    } else {
                        Log.d("Firestore", "Error getting comments: ", task.getException());
                    }
                });

    }

    private void initUI() {
        imgBack = findViewById(R.id.img_Back);

    }

    public String formatDateTime(long timestamp) {
        //change timestamp to date
        Date date = new Date(timestamp);

        //format date
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy");
        return sdf.format(date);
    }

    private void loadDB() {
        db = FirebaseFirestore.getInstance();
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