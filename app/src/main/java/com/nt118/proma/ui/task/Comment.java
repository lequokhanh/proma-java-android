package com.nt118.proma.ui.task;

import static com.nt118.proma.ui.task.TaskDetail.setWindowFlag;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.nt118.proma.R;

import java.util.ArrayList;
import java.util.Map;

public class Comment extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_comment);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        ArrayList<Map<String, Object>> comments = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Map<String, Object> comment = new java.util.HashMap<>();
            comment.put("name", "Nguyen Van A");
            comment.put("content", "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.");
            comment.put("date", "11:00 AM 11/10/23");
            comments.add(comment);
        }

        TextView commentTitle = findViewById(R.id.commentTitle);
        commentTitle.setText("Comments (" + comments.size() + ")");
        LinearLayout commentContainer = findViewById(R.id.commentContainer);
        commentContainer.removeAllViews();
        for (Map<String, Object> comment : comments) {
            View item_comment = getLayoutInflater().inflate(R.layout.item_comment, null);
            TextView name = item_comment.findViewById(R.id.nameCmt);
            TextView content = item_comment.findViewById(R.id.contentCmt);
            TextView date = item_comment.findViewById(R.id.dateCmt);
            name.setText((String) comment.get("name"));
            content.setText((String) comment.get("content"));
            date.setText((String) comment.get("date"));
            commentContainer.addView(item_comment);
        }
    }
}