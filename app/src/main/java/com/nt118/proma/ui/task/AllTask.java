package com.nt118.proma.ui.task;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Space;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nt118.proma.R;
import com.nt118.proma.model.ImageArray;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class AllTask extends AppCompatActivity {
    private String projectId;
    private String email;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_task);
        Intent intent = getIntent();

        SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
        projectId = intent.getStringExtra("projectId");
        email = sharedPreferences.getString("email", "");

        ImageView back = findViewById(R.id.back_button);
        back.setOnClickListener(v -> {
            finish();
        });
        ImageView menu_button = findViewById(R.id.menu_button);
        InitUI();
        menu_button.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(AllTask.this, v, 5);
            popup.getMenuInflater().inflate(R.menu.all_task_menu, popup.getMenu());
            popup.getMenu().setGroupDividerEnabled(true);
            try {
                Field[] fields = popup.getClass().getDeclaredFields();
                for (Field field : fields) {
                    if ("mPopup".equals(field.getName())) {
                        field.setAccessible(true);
                        Object menuPopupHelper = field.get(popup);
                        Class<?> classPopupHelper = Class.forName(menuPopupHelper
                                .getClass().getName());
                        Method setForceIcons = classPopupHelper.getMethod(
                                "setForceShowIcon", boolean.class);
                        setForceIcons.invoke(menuPopupHelper, true);
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            popup.show();
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_schedule_sort) {
                    showTaskList(findViewById(R.id.leftSide), findViewById(R.id.rightSide), 1);
                } else if (item.getItemId() == R.id.action_a_z_sort) {
                    showTaskList(findViewById(R.id.leftSide), findViewById(R.id.rightSide), 2);
                }
                return true;
            });
        });

    }

    private void InitUI() {
        LinearLayout leftSide = findViewById(R.id.leftSide);
        LinearLayout rightSide = findViewById(R.id.rightSide);
        leftSide.getLayoutParams().width = (int) (getResources().getDisplayMetrics().widthPixels * 0.5 - 68);
        rightSide.getLayoutParams().width = (int) (getResources().getDisplayMetrics().widthPixels * 0.5 - 68);
        showTaskList(leftSide, rightSide, 0);
    }

    private void showTaskList(LinearLayout leftSide, LinearLayout rightSide, int sortType) {
        leftSide.removeAllViews();
        rightSide.removeAllViews();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        ArrayList<Map<String, Object>> taskList = new ArrayList<>();
        Map<String, Object> memberTask = new HashMap<>();
        memberTask.put("email", email);
        memberTask.put("isLeader", false);
        Map<String, Object> memberLeader = new HashMap<>();
        memberLeader.put("email", email);
        memberLeader.put("isLeader", true);
        AtomicBoolean isOwner = new AtomicBoolean(false);
        db.collection("projects").document(projectId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Map<String, Object> project = task.getResult().getData();
                List<Map<String, Object>> members = (List<Map<String, Object>>) project.get("members");
                if (members != null) {
                    for (Map<String, Object> member : members) {
                        String _email = (String) member.get("email");
                        if (_email.equals(email)) {
                            isOwner.set(true);
                            break;
                        }
                    }
                }
            }
        });
        Filter filter = Filter.and(
                Filter.equalTo("projectId", projectId),
                Filter.or(
                        Filter.arrayContains("members", memberTask),
                        Filter.arrayContains("members", memberLeader)
                )
        );
        if (isOwner.get()) {
            filter = Filter.equalTo("projectId", projectId);
        }
        db.collection("tasks")
                .where(filter)
                .get()
                .addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().getDocuments().size() == 0) {
                    return;
                }
                for (int i = 0; i < task.getResult().getDocuments().size(); i++) {
                    Map<String, Object> taskItem = task.getResult().getDocuments().get(i).getData();
                    taskList.add(taskItem);
                }
                // sortType = 0: status, 1: deadline, 2: title
                if (sortType == 0) {
                    taskList.sort((o1, o2) -> {
                        int status1 = ((Long) o1.get("status")).intValue();
                        int status2 = ((Long) o2.get("status")).intValue();
                        return status1 - status2;
                    });
                } else if (sortType == 1) {
                    taskList.sort((o1, o2) -> {
                        String deadline1 = (String) o1.get("deadline");
                        String deadline2 = (String) o2.get("deadline");
                        return deadline1.compareTo(deadline2);
                    });
                } else if (sortType == 2) {
                    taskList.sort((o1, o2) -> {
                        String title1 = (String) o1.get("title");
                        String title2 = (String) o2.get("title");
                        return title1.compareTo(title2);
                    });
                }
                for (int i = 0; i < taskList.size(); i++) {
                    Map<String, Object> taskItem = taskList.get(i);
                    View taskView = LayoutInflater.from(this).inflate(R.layout.task_card, null);
                    ImageView taskIcon = taskView.findViewById(R.id.icon);
                    taskIcon.setImageResource(ImageArray.getIconTaskCard().get(taskItem.get("category").toString()));
                    TextView taskName = taskView.findViewById(R.id.taskName);
                    taskName.setText(taskItem.get("title").toString());
                    String deadline = (String) taskItem.get("deadline");
                    TextView taskDeadline = taskView.findViewById(R.id.deadline);
                    taskDeadline.setText(deadline);
                    TextView taskStatus = taskView.findViewById(R.id.status);
                    if ((Long) taskItem.get("status") == 0) {
                        taskStatus.setVisibility(View.GONE);
                    } else if ((Long) taskItem.get("status") == 1) {
                        taskStatus.setVisibility(View.VISIBLE);
                        taskStatus.setText("On going");
                    } else {
                        taskStatus.setBackgroundResource(R.drawable.rounded_corner_24_blue);
                        taskStatus.setVisibility(View.VISIBLE);
                        taskStatus.setText("Done");
                        taskStatus.setTextColor(getResources().getColor(R.color.white));
                    }
                    int finalI = i;
                    taskView.setOnClickListener(v -> {
                        Intent intent2 = new Intent(this, TaskDetail.class);
                        intent2.putExtra("taskId", task.getResult().getDocuments().get(finalI).getId());
                        intent2.putExtra("projectId", projectId);
                        startActivity(intent2);
                    });

                    // Check if the current user is a leader
                    boolean isLeader = false;
                    List<Map<String, Object>> members = (List<Map<String, Object>>) taskItem.get("members");
                    if (members != null) {
                        for (Map<String, Object> _member : members) {
                            String _email = (String) _member.get("email");
                            boolean leader = (boolean) _member.get("isLeader");
                            if (_email.equals(email) && leader) {
                                isLeader = true;
                                break;
                            }
                        }
                    }

                    // Set visibility of edit button based on isLeader

                    Space space = new Space(this);
                    space.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 20));
                    taskView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    if (i % 2 == 0) {
                        leftSide.addView(taskView);
                        leftSide.addView(space);
                    } else {
                        rightSide.addView(taskView);
                        rightSide.addView(space);
                    }
                }
            }
        });
    }

}
