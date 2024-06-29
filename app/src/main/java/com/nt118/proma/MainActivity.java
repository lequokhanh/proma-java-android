package com.nt118.proma;

import static android.Manifest.permission.POST_NOTIFICATIONS;
import static androidx.navigation.ui.NavigationUI.setupWithNavController;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nt118.proma.databinding.ActivityMainBinding;
import com.nt118.proma.ui.login.CompleteProfile;
import com.nt118.proma.ui.login.Login;
import com.nt118.proma.ui.member.AddMember;
import com.nt118.proma.ui.notification.NotificationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ArrayList<String> members = new ArrayList<>();
    private ArrayList<String> names = new ArrayList<>();
    private LinearLayout member_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
        String email = sharedPreferences.getString("email", null);
        if (email == null) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(MainActivity.this, Login.class);
            startActivity(intent);
            finishAffinity();
        } else {
            if (!sharedPreferences.getBoolean("isCompletedProfile", false)) {
                Intent intent = new Intent(MainActivity.this, CompleteProfile.class);
                startActivity(intent);
                finishAffinity();
            } else {
                listenNotiToSend();
                binding = ActivityMainBinding.inflate(getLayoutInflater());
                setContentView(binding.getRoot());
                BottomNavigationView navView = findViewById(R.id.nav_view);
                AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(R.id.navigation_home, R.id.navigation_project, R.id.navigation_schedule, R.id.navigation_profile).build();
                NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
                setupWithNavController(navView, navController);
                navView.setItemIconTintList(null);
                FloatingActionButton create_btn = findViewById(R.id.create_button);
                AtomicReference<Boolean> isDialogShowing = new AtomicReference<>(false);
                create_btn.setOnClickListener(v -> {
                    members.clear();
                    BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MainActivity.this);
                    View view1 = LayoutInflater.from(MainActivity.this).inflate(R.layout.modal_create_project, null);
                    bottomSheetDialog.setContentView(view1);
                    if (isDialogShowing.get()) {
                        return;
                    }
                    isDialogShowing.set(true);
                    bottomSheetDialog.show();
                    bottomSheetDialog.setOnDismissListener(dialog -> {
                        isDialogShowing.set(false);
                    });
                    bottomSheetDialog.setOnCancelListener(dialog -> {
                        isDialogShowing.set(false);
                    });
                    member_list = view1.findViewById(R.id.memberList2);
                    Button addMemberBtn = view1.findViewById(R.id.addMemberBtn);
                    addMemberBtn.setOnClickListener(v1 -> {
                        Intent intent = new Intent(MainActivity.this, AddMember.class);
                        intent.putStringArrayListExtra("members", members);
                        intent.putStringArrayListExtra("name", names);
                        intent.putExtra("category", 1);
                        startActivityForResult(intent, 1);
                    });
                    Button deadlineBtn = view1.findViewById(R.id.deadlineBtn);
                    TextView deadlineView = view1.findViewById(R.id.deadlineView);
                    AtomicReference<Boolean> isDatePickerShowing = new AtomicReference<>(false);
                    deadlineBtn.setOnClickListener(v1 -> {
                        BottomSheetDialog bottomSheetDialog1 = new BottomSheetDialog(v.getContext());
                        View view2 = LayoutInflater.from(MainActivity.this).inflate(R.layout.modal_date_picker, null);
                        bottomSheetDialog1.setContentView(view2);
                        if (isDatePickerShowing.get()) {
                            return;
                        }
                        CalendarView datePicker = view2.findViewById(R.id.datePicker);
                        if (deadlineView.getVisibility() == View.VISIBLE) {
                            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy", Locale.ENGLISH);
                            try {
                                Date date = formatter.parse(deadlineView.getText().toString());
                                datePicker.setDate(date.getTime());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        isDatePickerShowing.set(true);
                        bottomSheetDialog1.show();
                        View timeContainer = view2.findViewById(R.id.timeContainer);
                        timeContainer.setVisibility(View.GONE);
                        MutableLiveData<Date> deadlineDate = new MutableLiveData<>(new Date(datePicker.getDate()));
                        datePicker.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
                            deadlineDate.setValue(new Date(year - 1900, month, dayOfMonth));
                        });
                        Button monthBtn = view2.findViewById(R.id.monthBtn);
                        AtomicReference<Boolean> isMonthPicker = new AtomicReference<>(false);
                        monthBtn.setOnClickListener(v2 -> {
                            if (isMonthPicker.get()) {
                                return;
                            }
                            isMonthPicker.set(true);
                            BottomSheetDialog monthPicker = new BottomSheetDialog(this);
                            View monthPickerView = getLayoutInflater().inflate(R.layout.modal_month_picker, null);
                            monthPicker.setContentView(monthPickerView);
                            NumberPicker monthPickerNumber = monthPickerView.findViewById(R.id.monthPicker);
                            NumberPicker yearPicker = monthPickerView.findViewById(R.id.yearPicker);
                            monthPickerNumber.setMinValue(1);
                            monthPickerNumber.setMaxValue(12);
                            monthPickerNumber.setDisplayedValues(new String[]{"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"});
                            monthPickerNumber.setValue(deadlineDate.getValue().getMonth() + 1);
                            yearPicker.setMinValue(1970);
                            yearPicker.setMaxValue(2100);
                            yearPicker.setValue(new Date().getYear() + 1900);
                            yearPicker.setWrapSelectorWheel(false);
                            Button applyBtn = monthPickerView.findViewById(R.id.applyBtn);
                            Button cancelBtn = view2.findViewById(R.id.cancelBtn);
                            applyBtn.setOnClickListener(v3 -> {
                                Date date = new Date(yearPicker.getValue() - 1900, monthPickerNumber.getValue() - 1, 1);
                                deadlineDate.setValue(date);
                                monthPicker.dismiss();
                            });
                            cancelBtn.setOnClickListener(v3 -> monthPicker.dismiss());
                            monthPicker.setOnDismissListener(dialog -> isMonthPicker.set(false));
                            monthPicker.show();
                        });
                        Button applyBtn = view2.findViewById(R.id.applyBtn);
                        Button cancelBtn = view2.findViewById(R.id.cancelBtn);
                        deadlineDate.observe(this, date -> {
                            datePicker.setDate(date.getTime());
                        });
                        datePicker.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
                            deadlineDate.setValue(new Date(year - 1900, month, dayOfMonth));
                        });
                        applyBtn.setOnClickListener(v2 -> {
                            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy", Locale.ENGLISH);
                            String deadline = formatter.format(deadlineDate.getValue());
                            deadlineView.setText(deadline);
                            deadlineView.setVisibility(View.VISIBLE);
                            bottomSheetDialog1.dismiss();
                            isDatePickerShowing.set(false);
                        });
                        cancelBtn.setOnClickListener(v2 -> {
                            bottomSheetDialog1.dismiss();
                            isDatePickerShowing.set(false);
                        });
                    });
                    Button createProjectBtn = view1.findViewById(R.id.createProjectBtn);
                    createProjectBtn.setOnClickListener(v1 -> {
                        EditText etNameProject = view1.findViewById(R.id.etNameProject);
                        EditText etDescProject = view1.findViewById(R.id.etDescProject);
                        String nameProject = etNameProject.getText().toString();
                        String descProject = etDescProject.getText().toString();
                        if (nameProject.isEmpty()) {
                            etNameProject.setError("Name is required");
                            return;
                        }
                        if (descProject.isEmpty()) {
                            etDescProject.setError("Description is required");
                            return;
                        }
                        if (members.isEmpty()) {
                            addMemberBtn.setError("Members are required");
                            return;
                        }
                        if (deadlineView.getText().toString().isEmpty()) {
                            deadlineBtn.setError("Deadline is required");
                            return;
                        }
                        Map<String, Object> project = new HashMap<>();
                        project.put("name", nameProject);
                        project.put("description", descProject);
                        ArrayList<Map<String, Object>> memberProject = new ArrayList<>();
                        for (String member : members) {
                            Map<String, Object> memberMap = new HashMap<>();
                            memberMap.put("email", member);
                            memberMap.put("isAccepted", false);
                            memberProject.add(memberMap);
                        }
                        project.put("members", memberProject);
                        project.put("deadline", deadlineView.getText().toString());
                        project.put("user_created", FirebaseAuth.getInstance().getCurrentUser().getProviderData().get(1).getEmail());
                        db.collection("projects").add(project).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                bottomSheetDialog.dismiss();
                                Toast.makeText(MainActivity.this, "Project created", Toast.LENGTH_SHORT).show();
                                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy", Locale.ENGLISH);
                                for (String member : members) {
                                    db.collection("users").whereEqualTo("email", member).get().addOnCompleteListener(task2 -> {
                                        if (task2.isSuccessful()) {
                                            System.out.println("999999999999999999" + task2.getResult().getDocuments().get(0).getId());
                                            db.collection("users").document(task2.getResult().getDocuments().get(0).getId()).collection("notification_logs").add(new HashMap<String, Object>() {{
                                                put("type", 1);
                                                put("message", "You have been invited to join project " + nameProject);
                                                put("date", formatter.format(new Date()));
                                                put("sender", email);
                                                put("projectId", task1.getResult().getId());
                                                put("isRead", false);
                                                put("isAccepted", false);
                                            }});
                                        }
                                    });
                                }
                            }
                        });
                    });
                });
                if (ContextCompat.checkSelfPermission(this, POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {
                    ActivityCompat.requestPermissions(this, new String[]{POST_NOTIFICATIONS}, 1);
                }
                createNotificationChannel();
            }
        }
    }

    private void listenNotiToSend() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
        String email = sharedPreferences.getString("email", "");
        db.collection("users").whereEqualTo("email", email).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().getDocuments().size() > 0) {
                    String id = task.getResult().getDocuments().get(0).getId();
                    db.collection("users").document(id).collection("notification_logs").addSnapshotListener((value, error) -> {
                        if (error != null) {
                            return;
                        }
                        if (value != null) {
                            for (int i = 0; i < value.getDocumentChanges().size(); i++) {
                                Map<String, Object> noti = value.getDocumentChanges().get(i).getDocument().getData();
                                if (noti.get("isRead") != null && !((boolean) noti.get("isRead"))) {
                                    Intent intent = new Intent(MainActivity.this, NotificationView.class);
                                    PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
                                    NotificationCompat.Builder builder = null;
                                    if (noti.get("type").equals(1)) {
                                        builder = new NotificationCompat.Builder(this, "Invite")
                                                .setSmallIcon(R.drawable.ic_launcher_foreground)
                                                .setContentTitle("Proma")
                                                .setContentText((String) noti.get("message"))
                                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                                .setContentIntent(pendingIntent)
                                                .setAutoCancel(true);
                                    } else if (noti.get("type").equals(2)) {
                                        builder = new NotificationCompat.Builder(this, "Comment")
                                                .setSmallIcon(R.drawable.ic_launcher_foreground)
                                                .setContentTitle("Proma")
                                                .setContentText((String) noti.get("message"))
                                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                                .setContentIntent(pendingIntent)
                                                .setAutoCancel(true);
                                    } else if (noti.get("type").equals(3)) {
                                        builder = new NotificationCompat.Builder(this, "Assign")
                                                .setSmallIcon(R.drawable.ic_launcher_foreground)
                                                .setContentTitle("Proma")
                                                .setContentText((String) noti.get("message"))
                                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                                .setContentIntent(pendingIntent)
                                                .setAutoCancel(true);
                                    }
                                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
                                    notificationManager.cancelAll();
                                    notificationManager.notify(value.getDocumentChanges().get(i).getDocument().getId().hashCode(), builder.build());
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Invite";
            String description = "Invite to join project";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("Invite", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            // create notification channel for comment
            CharSequence nameComment = "Comment";
            String descriptionComment = "Comment on task";
            NotificationChannel channelComment = new NotificationChannel("Comment", nameComment, importance);
            channelComment.setDescription(descriptionComment);
            notificationManager.createNotificationChannel(channelComment);
            // create notification channel for assign to task
            CharSequence nameAssign = "Assign";
            String descriptionAssign = "Assign to task";
            NotificationChannel channelAssign = new NotificationChannel("Assign", nameAssign, importance);
            channelAssign.setDescription(descriptionAssign);
            notificationManager.createNotificationChannel(channelAssign);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            members = data.getStringArrayListExtra("members");
            names = data.getStringArrayListExtra("names");
            member_list.removeAllViews();
            for (String name_member : names.subList(0, Math.min(names.size(), 2))) {
                TextView itemMember = createItemMember(name_member);
                member_list.addView(itemMember);
            }
            if (members.size() > 2) {
                TextView moreMember = createItemMember((members.size() - 2) + " more...");
                member_list.addView(moreMember);
            }
        }
    }

    public TextView createItemMember(String name) {
        TextView textView = new TextView(this);
        textView.setText(name);
        textView.setBackgroundResource(R.drawable.rounded_corner_24_bw);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        // add padding
        float dip10 = 10f;
        Resources r = getResources();
        float px10 = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dip10,
                r.getDisplayMetrics()
        );
        float dip12 = 12f;
        float px12 = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dip12,
                r.getDisplayMetrics()
        );
        float dip4 = 4f;
        float px4 = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dip4,
                r.getDisplayMetrics()
        );
        textView.setPadding((int) px12, (int) px4, (int) px12, (int) px4);
        // add margin
        params.setMargins(0, 0, 0, (int) px10);
        //config text size
        textView.setTextSize(12);
        textView.setTextColor(getResources().getColor(R.color.black));
        // set font family
        textView.setTypeface(getResources().getFont(R.font.roboto_bold));
        textView.setLayoutParams(params);
        return textView;
    }

    @Override
    public void onResume() {
        super.onResume();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(MainActivity.this, Login.class);
            startActivity(intent);
            finishAffinity();

        } else {
            handleProfileCompletion();
        }
        Locale locale = new Locale("en");
        Locale.setDefault(locale);
        Configuration config = getBaseContext().getResources().getConfiguration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (!sharedPreferences.getBoolean("rememberMe", false)) {
            FirebaseAuth.getInstance().signOut();
            editor.clear().apply();
        }
        moveTaskToBack(true);
        finishAffinity();
    }

    public void handleProfileCompletion() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String email = FirebaseAuth.getInstance().getCurrentUser().getProviderData().get(1).getEmail();
        db.collection("users").whereEqualTo("email", email).get().addOnCompleteListener(task -> {
            if (task.getResult().isEmpty()) {
                Intent intent = new Intent(MainActivity.this, CompleteProfile.class);
                startActivity(intent);
                finishAffinity();
            } else {
                if (task.getResult().getDocuments().get(0).getString("name") == null || task.getResult().getDocuments().get(0).getString("phone_number") == null || task.getResult().getDocuments().get(0).getString("dob") == null) {
                    Intent intent = new Intent(MainActivity.this, CompleteProfile.class);
                    startActivity(intent);
                    finishAffinity();
                }
            }
        });
    }
}