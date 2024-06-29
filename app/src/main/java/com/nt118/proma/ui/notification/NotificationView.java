package com.nt118.proma.ui.notification;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nt118.proma.R;
import com.nt118.proma.model.ImageArray;
import com.nt118.proma.ui.task.TaskDetail;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

public class NotificationView extends AppCompatActivity {

    ImageView imgBack;
    LinearLayout listContainers;
    MutableLiveData<ArrayList<Map<String, Object>>> listNotification = new MutableLiveData<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_notification);
        InitUi();
        onClickImageBack();
    }

    private void InitUi() {
        imgBack = findViewById(R.id.img_Back);
        listContainers = findViewById(R.id.listContainers);
        loadDB();
        listNotification.observe(this, maps -> {
            listContainers.removeAllViews();
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy", Locale.ENGLISH);
            View notificationContainer = getLayoutInflater().inflate(R.layout.container_notification, null);
            for (int i = 0; i < maps.size(); i++) {
                if (i != 0 && !maps.get(i).get("date").equals(maps.get(i - 1).get("date"))) {
                    listContainers.addView(notificationContainer);
                    notificationContainer = getLayoutInflater().inflate(R.layout.container_notification, null);
                }
                LinearLayout listNotiContainer = notificationContainer.findViewById(R.id.listNotiContainer);
                TextView date = notificationContainer.findViewById(R.id.dateView);
                View item_notification = getLayoutInflater().inflate(R.layout.item_notification, null);
                TextView message = item_notification.findViewById(R.id.message);
                ImageView avatar = item_notification.findViewById(R.id.avatar);
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("users").whereEqualTo("email", maps.get(i).get("sender")).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().getDocuments().size() > 0) {
                            int avatarIndex = (int) task.getResult().getDocuments().get(0).get("avatar");
                            avatar.setImageResource(new ImageArray().getAvatarImage().get(avatarIndex));
                        }
                    }
                });
                if ((Long) maps.get(i).get("type") == 1) {
                    if (maps.get(i).get("isAccepted") != null && !(boolean) maps.get(i).get("isAccepted")) {
                        int finalI = i;
                        item_notification.setOnClickListener(v -> {
                            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(NotificationView.this);
                            bottomSheetDialog.setContentView(R.layout.modal_invitation);
                            Button btnAccept = bottomSheetDialog.findViewById(R.id.AcceptBtn);
                            Button btnReject = bottomSheetDialog.findViewById(R.id.RejectBtn);
                            btnAccept.setOnClickListener(v1 -> {
                                SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
                                String email = sharedPreferences.getString("email", "");
                                db.collection("users").whereEqualTo("email", email).get().addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        if (task.getResult().getDocuments().size() > 0) {
                                            String id = task.getResult().getDocuments().get(0).getId();
                                            db.collection("users").document(id).collection("notification_logs").document((String) maps.get(finalI).get("id")).update("isAccepted", true);
                                            db.collection("projects").document(maps.get(finalI).get("projectId").toString()).get().addOnCompleteListener(task1 -> {
                                                if (task1.isSuccessful()) {
                                                    ArrayList<Map<String, Object>> list = (ArrayList<Map<String, Object>>) task1.getResult().get("members");
                                                    for (int j = 0; j < list.size(); j++) {
                                                        if (list.get(j).get("email").equals(email)) {
                                                            list.get(j).put("isAccepted", true);
                                                            break;
                                                        }
                                                    }
                                                    db.collection("projects").document(maps.get(finalI).get("projectId").toString()).update("members", list);
                                                }
                                            });
                                        }
                                    }
                                });
                                bottomSheetDialog.dismiss();
                            });
                            btnReject.setOnClickListener(v2 -> {
                                SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
                                String email = sharedPreferences.getString("email", "");
                                db.collection("users").whereEqualTo("email", email).get().addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        if (task.getResult().getDocuments().size() > 0) {
                                            String id = task.getResult().getDocuments().get(0).getId();
                                            db.collection("users").document(id).collection("notification_logs").document((String) maps.get(finalI).get("id")).delete();
                                            db.collection("projects").document(maps.get(finalI).get("projectId").toString()).get().addOnCompleteListener(task1 -> {
                                                if (task1.isSuccessful()) {
                                                    ArrayList<Map<String, Object>> list = (ArrayList<Map<String, Object>>) task1.getResult().get("members");
                                                    for (int j = 0; j < list.size(); j++) {
                                                        if (list.get(j).get("email").equals(email)) {
                                                            list.remove(j);
                                                            break;
                                                        }
                                                    }
                                                    db.collection("projects").document(maps.get(finalI).get("projectId").toString()).update("members", list);
                                                }
                                            });
                                        }
                                    }
                                });
                                bottomSheetDialog.dismiss();
                            });
                            bottomSheetDialog.setOnDismissListener(dialog -> loadDB());
                            bottomSheetDialog.show();
                        });
                    } else if (maps.get(i).get("isAccepted") != null && (boolean) maps.get(i).get("isAccepted")) {
                        item_notification.setOnClickListener(v -> {
                            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(NotificationView.this);
                            bottomSheetDialog.setContentView(R.layout.modal_invitation);
                            Button btnAccept = bottomSheetDialog.findViewById(R.id.AcceptBtn);
                            Button btnReject = bottomSheetDialog.findViewById(R.id.RejectBtn);
                            btnAccept.setVisibility(View.GONE);
                            btnReject.setVisibility(View.GONE);
                            TextView title = bottomSheetDialog.findViewById(R.id.textView3);
                            title.setText("You have accepted this invitation");
                            bottomSheetDialog.show();
                        });
                    }
                } else if ((Long) maps.get(i).get("type") == 2) {
                    int finalI1 = i;
                    item_notification.setOnClickListener(v -> {
                        Intent intent = new Intent(NotificationView.this, TaskDetail.class);
                        intent.putExtra("taskId", (String) maps.get(finalI1).get("taskId"));
                        intent.putExtra("projectId", (String) maps.get(finalI1).get("projectId"));
                        startActivity(intent);
                    });
                    date.setText((String) maps.get(i).get("date"));
                    message.setText((String) maps.get(i).get("message"));
                    listNotiContainer.addView(item_notification);
                } else if ((Long) maps.get(i).get("type") == 3) {
                    int finalI2 = i;
                    item_notification.setOnClickListener(v -> {
                        Intent intent = new Intent(NotificationView.this, TaskDetail.class);
                        intent.putExtra("taskId", (String) maps.get(finalI2).get("taskId"));
                        intent.putExtra("projectId", (String) maps.get(finalI2).get("projectId"));
                        startActivity(intent);
                    });
                    date.setText((String) maps.get(i).get("date"));
                    message.setText((String) maps.get(i).get("message"));
                    listNotiContainer.addView(item_notification);
                }
                date.setText((String) maps.get(i).get("date"));
                message.setText((String) maps.get(i).get("message"));
                listNotiContainer.addView(item_notification);
            }
            listContainers.addView(notificationContainer);
        });
    }

    private void loadDB() {
        // Load data from database
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
        String email = sharedPreferences.getString("email", "");
        db.collection("users").whereEqualTo("email", email).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().getDocuments().size() > 0) {
                    String id = task.getResult().getDocuments().get(0).getId();
                    db.collection("users").document(id).collection("notification_logs").get().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            ArrayList<Map<String, Object>> list = new ArrayList<>();
                            for (int i = 0; i < task1.getResult().getDocuments().size(); i++) {
                                list.add(task1.getResult().getDocuments().get(i).getData());
                                list.get(i).put("id", task1.getResult().getDocuments().get(i).getId());
                                task1.getResult().getDocuments().get(i).getReference().update("isRead", true);
                            }
                            list.sort((o1, o2) -> {
                                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy", Locale.ENGLISH);
                                String date1 = (String) o1.get("date");
                                String date2 = (String) o2.get("date");
                                try {
                                    return formatter.parse(date2).compareTo(formatter.parse(date1));
                                } catch (Exception e) {
                                    return 0;
                                }
                            });
                            System.out.println("99999999999999" + list);
                            listNotification.setValue(list);
                        }
                    });
                }
            }
        });
    }

    private void onClickImageBack() {
        imgBack.setOnClickListener(v -> finish());
    }
}