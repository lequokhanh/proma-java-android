package com.nt118.proma.ui.notification;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FirebaseFirestore;
import com.nt118.proma.R;

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
    private void InitUi(){
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
                ImageView actionBtn = item_notification.findViewById(R.id.action_btn);
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