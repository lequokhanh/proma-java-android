package com.nt118.proma.ui.member;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.nt118.proma.R;
import com.nt118.proma.model.ImageArray;

import java.util.ArrayList;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewMembers extends AppCompatActivity {

    private LinearLayout listMembers;
    private ImageView imgBack;
    private TextView numMembers;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_members);

        showMembers();

        initUI();
        imgBack.setOnClickListener(v -> {
            finish();
        });

    }

    private void initUI() {
        imgBack = findViewById(R.id.img_Back);
        listMembers = findViewById(R.id.list_members);
        numMembers = findViewById(R.id.num_members);
    }

    private void showMembers() {
        //get data from another activity
        Intent intent = getIntent();
        String taskId = intent.getStringExtra("taskId");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("tasks").document(taskId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ArrayList<Map<String, Object>> members = (ArrayList<Map<String, Object>>) task.getResult().get("members");
                numMembers.setText("Project members (" + members.size() + ")");
                for (Map<String, Object> member : members) {
                    String email = (String) member.get("email");
                    db.collection("users").whereEqualTo("email", email).get().addOnCompleteListener(task2 -> {
                        if (task2.isSuccessful()) {
                            Map<String, Object> user = task2.getResult().getDocuments().get(0).getData();
                            View fileTextView = LayoutInflater.from(this).inflate(R.layout.item_member, null);
                            TextView name = fileTextView.findViewById(R.id.name);
                            name.setText(user.get("name").toString());
                            TextView email_member = fileTextView.findViewById(R.id.email);
                            email_member.setText(user.get("email").toString());
                            ImageView rm_btn = fileTextView.findViewById(R.id.removeBtn);
                            rm_btn.setVisibility(View.GONE);
                            ImageView avatar = fileTextView.findViewById(R.id.avatar);

                            // Get the avatar ID and set the image resource
                            Object avatarObj = user.get("avatar");
                            int avatarIndex = Math.toIntExact((Long) avatarObj);
                            avatar.setImageResource(new ImageArray().getAvatarImage().get(avatarIndex));
                            listMembers.addView(fileTextView);
                            float dip10 = 10f;
                            Resources r = getResources();
                            float px10 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip10, r.getDisplayMetrics());
                            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) fileTextView.getLayoutParams();
                            layoutParams.setMargins(0, 0, 0, (int) px10);
                        }
                    });
                }
            }
        });
    }
}