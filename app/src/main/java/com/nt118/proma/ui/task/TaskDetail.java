package com.nt118.proma.ui.task;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nt118.proma.R;
import com.nt118.proma.model.ImageArray;
import com.nt118.proma.ui.member.AddMember;
import com.nt118.proma.ui.member.ViewMembers;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class TaskDetail extends AppCompatActivity {
    private static final int PICK_FILE_REQUEST = 1;
    private final MutableLiveData<ArrayList<Map<String, Object>>> comments = new MutableLiveData<>();
    AtomicReference<TextView> currentTab;
    private LinearLayout container;
    private String taskId, projectId, parentId;
    private ArrayList<String> task_members = new ArrayList<>();
    private ArrayList<String> task_names = new ArrayList<>();
    private ArrayList<String> membersProject = new ArrayList<>();
    private ArrayList<String> memberNames = new ArrayList<>();
    private String leaderEmail;
    private String leaderName;
    private FloatingActionButton create_btn;
    private ProgressBar loadingBar;
    private Dialog currentDialog;
    private LinearLayout listItem, listComments;
    private LinearLayout listItemAttached;
    private LinearLayout memberList;
    private FirebaseFirestore db;
    private TextView leader;
    private String email;

    public static void setWindowFlag(Activity activity, final int bits, boolean on) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_detail);
        Intent intent = getIntent();
        SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
        email = sharedPreferences.getString("email", "");
        taskId = intent.getStringExtra("taskId");
        projectId = intent.getStringExtra("projectId");
        parentId = intent.getStringExtra("parentId") == null ? "" : intent.getStringExtra("parentId");
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        InitUI();
        handleTabClick();
    }

    private void InitUI() {
        Dialog loading = createLoadingDialog();
        ImageView cover = findViewById(R.id.cover);
        TextView title = findViewById(R.id.title);
        TextView desc = findViewById(R.id.desc);
        TextView deadline = findViewById(R.id.deadlineView);
        create_btn = findViewById(R.id.create_task_btn);
        container = findViewById(R.id.container);
        loadingBar = findViewById(R.id.loadingTaskDetail);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        ImageView imageView = findViewById(R.id.img_Back);
        ImageView threeDot = findViewById(R.id.menu_btn);
        threeDot.setVisibility(View.GONE);
        threeDot.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, v, 5);
            popup.getMenuInflater().inflate(R.menu.task_menu, popup.getMenu());
            SpannableString s = new SpannableString(popup.getMenu().getItem(2).getTitle());
            s.setSpan(new ForegroundColorSpan(Color.parseColor("#FF3B30")), 0, s.length(), 0);
            popup.getMenu().getItem(2).setTitle(s);
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
                if (item.getItemId() == R.id.action_mark) {
                    db.collection("tasks").document(taskId).update("status", 2).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            loadUI();
                        }
                    });
                }
                if (item.getItemId() == R.id.action_edit) {
                    AtomicReference<Boolean> isDialogShowing = new AtomicReference<>(false);
                    showEditTaskDialog(isDialogShowing);
                }
                if (item.getItemId() == R.id.action_delete) {
                    db.collection("tasks").document(taskId).delete().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            finish();
                        }
                    });
                }
                return true;
            });
        });
        imageView.setOnClickListener(v -> {
            finish();
        });
        db.collection("tasks").document(taskId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                title.setText(task.getResult().getString("title"));
                desc.setText(task.getResult().getString("description"));
                deadline.setText(task.getResult().getString("deadline"));
                for (Map<String, Object> member : (ArrayList<Map<String, Object>>) task.getResult().get("members")) {
                    if ((Boolean) member.get("isLeader") && member.get("email").equals(email)) {
                        threeDot.setVisibility(View.VISIBLE);
                        break;
                    }
                }
                db.collection("projects").document(projectId).get().addOnCompleteListener(task2 -> {
                    if (task.isSuccessful()) {
                        if (task2.getResult().get("cover") != null) {
                            cover.setImageResource(ImageArray.getCoverProjectImage().get(Math.toIntExact((Long) task2.getResult().get("cover"))));
                        }
                        loading.dismiss();
                    }
                });
            }
        });
    }

    private void showEditTaskDialog(AtomicReference<Boolean> isDialogShowing) {
        task_members = new ArrayList<>();
        task_names = new ArrayList<>();
        membersProject = new ArrayList<>();
        memberNames = new ArrayList<>();
        leaderEmail = null;
        leaderName = null;
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view1 = LayoutInflater.from(this).inflate(R.layout.modal_create_task, null);
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
        // handle deadline button
        TextView titleDialog = view1.findViewById(R.id.titleDialog);
        titleDialog.setText("Edit Task");
        Button deadlineBtn = view1.findViewById(R.id.deadlineBtn);
        TextView deadlineView = view1.findViewById(R.id.deadlineView);
        Button addCategoryBtn = view1.findViewById(R.id.addCategoryBtn);
        TextView categoryView = view1.findViewById(R.id.categoryView);
        Button addMemberBtn = view1.findViewById(R.id.addMemberBtn);
        memberList = view1.findViewById(R.id.memberList2);
        Button addLeaderBtn = view1.findViewById(R.id.addLeaderBtn);
        leader = view1.findViewById(R.id.leaderTV);
        db.collection("tasks").document(taskId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Map<String, Object> taskItem = task.getResult().getData();
                EditText taskTitle = view1.findViewById(R.id.taskTitleET);
                taskTitle.setText(taskItem.get("title").toString());
                EditText desc = view1.findViewById(R.id.descET);
                desc.setText(taskItem.get("description").toString());
                deadlineView.setText(taskItem.get("deadline").toString());
                deadlineView.setVisibility(View.VISIBLE);
                categoryView.setText(taskItem.get("category").toString());
                categoryView.setVisibility(View.VISIBLE);
                ArrayList<Map<String, Object>> members = (ArrayList<Map<String, Object>>) taskItem.get("members");
                for (Map<String, Object> member : members) {
                    if ((Boolean) member.get("isLeader")) {
                        leaderEmail = member.get("email").toString();
                        db.collection("users").whereEqualTo("email", leaderEmail).get().addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                leaderName = task1.getResult().getDocuments().get(0).get("name").toString();
                                leader.setText(leaderName);
                                leader.setVisibility(View.VISIBLE);
                            }
                        });
                    } else {
                        task_members.add(member.get("email").toString());
                        db.collection("users").whereEqualTo("email", member.get("email")).get().addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                task_names.add(task1.getResult().getDocuments().get(0).get("name").toString());
                                TextView memberView = createItemMember(task1.getResult().getDocuments().get(0).get("name").toString());
                                memberList.addView(memberView);
                            }
                        });
                    }
                }
            }
        });
        AtomicReference<Boolean> isDatePickerShowing = new AtomicReference<>(false);
        deadlineBtn.setOnClickListener(v1 -> {
            showDatePicker(isDatePickerShowing, deadlineView, Boolean.TRUE);
        });
        // handle choose category button
        addCategoryBtn.setOnClickListener(v1 -> {
            showCategoryDialog(categoryView);
        });
        // handle add member button
        addMemberBtn.setOnClickListener(v1 -> {
            showAddMemberActivity();
        });
        // handle choose leader button
        addLeaderBtn.setOnClickListener(v1 -> {
            showChooseLeaderActivity();
        });
        // handle create task button
        Button createTaskBtn = view1.findViewById(R.id.createTaskBtn);
        createTaskBtn.setText("Save");
        createTaskBtn.setOnClickListener(v1 -> {
            EditText taskTitle = view1.findViewById(R.id.taskTitleET);
            EditText desc = view1.findViewById(R.id.descET);
            if (taskTitle.getText().toString().isEmpty())
                taskTitle.setError("Task title is required");
            if (desc.getText().toString().isEmpty()) desc.setError("Description is required");
            if (leader.getVisibility() == View.GONE)
                Toast.makeText(this, "Leader is required", Toast.LENGTH_SHORT).show();
            else if (task_members.size() == 0)
                Toast.makeText(this, "Members are required", Toast.LENGTH_SHORT).show();
            else if (categoryView.getVisibility() == View.GONE)
                Toast.makeText(this, "Category is required", Toast.LENGTH_SHORT).show();
            else if (deadlineView.getVisibility() == View.GONE)
                Toast.makeText(this, "Deadline is required", Toast.LENGTH_SHORT).show();
            if (taskTitle.getText().toString().isEmpty() || desc.getText().toString().isEmpty() || leader.getVisibility() == View.GONE || task_members.size() == 0 || deadlineView.getVisibility() == View.GONE || categoryView.getVisibility() == View.GONE)
                return;
            bottomSheetDialog.dismiss();
            handleEditTaskBtn(taskTitle.getText().toString(), desc.getText().toString(), leaderEmail, task_members, deadlineView.getText().toString(), categoryView.getText().toString());
        });
    }

    private void handleTabClick() {
        LinearLayout tabContainer = findViewById(R.id.tabContainer);
        showResultTab();
        currentTab = new AtomicReference<>(findViewById(R.id.resultTab));
        for (int i = 0; i < tabContainer.getChildCount(); i++) {
            TextView child = (TextView) tabContainer.getChildAt(i);
            child.setOnClickListener(v -> {
                if (currentTab.get() == child) return;
                child.setTextColor(Color.parseColor("#FFFFFF"));
                child.setBackground(ContextCompat.getDrawable(this, R.drawable.rounded_corner_24_blue));
                currentTab.get().setTextColor(Color.parseColor("#007AFF"));
                currentTab.get().setBackground(ContextCompat.getDrawable(this, R.drawable.rounded_corner_24_bw));
                currentTab.set(child);
                if (child.getId() == R.id.resultTab) {
                    showResultTab();
                }
                if (child.getId() == R.id.informationTab) {
                    showInformationTab();
                }
                if (child.getId() == R.id.subtaskTab) {
                    showTaskList();
                }
            });
        }
    }

    private void loadUI() {
        InitUI();
        if (currentTab.get().getId() == R.id.resultTab) {
            showResultTab();
        }
        if (currentTab.get().getId() == R.id.informationTab) {
            showInformationTab();
        }
        if (currentTab.get().getId() == R.id.subtaskTab) {
            showTaskList();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void showTaskList() {
        create_btn.setVisibility(View.VISIBLE);
        loadingBar.setVisibility(View.VISIBLE);
        container.setVisibility(View.GONE);
        container.removeAllViews();

        ScrollView scrollView = new ScrollView(this);
        scrollView.setVerticalScrollBarEnabled(false);
        View task_list = LayoutInflater.from(this).inflate(R.layout.task_list, null);
        scrollView.addView(task_list);

        db = FirebaseFirestore.getInstance();

        LinearLayout leftSide = task_list.findViewById(R.id.leftSide);
        LinearLayout rightSide = task_list.findViewById(R.id.rightSide);

        leftSide.getLayoutParams().width = (int) (getResources().getDisplayMetrics().widthPixels * 0.5 - 68);
        rightSide.getLayoutParams().width = (int) (getResources().getDisplayMetrics().widthPixels * 0.5 - 68);
        leftSide.removeAllViews();
        rightSide.removeAllViews();

        create_btn.setOnClickListener(v -> {
            AtomicReference<Boolean> isDialogShowing = new AtomicReference<>(false);
            showPopupCreateTask(isDialogShowing);
        });

        AtomicInteger count = new AtomicInteger(0);
        db.collection("tasks").whereEqualTo("parentId", taskId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (int i = 0; i < task.getResult().getDocuments().size(); i++) {
                    count.getAndIncrement();
                    Map<String, Object> taskItem = task.getResult().getDocuments().get(i).getData();
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
                        Intent intent = new Intent(this, TaskDetail.class);
                        intent.putExtra("taskId", task.getResult().getDocuments().get(finalI).getId());
                        intent.putExtra("projectId", projectId);
                        intent.putExtra("parentId", taskId);
                        startActivity(intent);
                    });
                    Space space = new Space(this);
                    space.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 20));
                    taskView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    if (count.get() % 2 == 1) {
                        leftSide.addView(taskView);
                        leftSide.addView(space);
                    } else {
                        rightSide.addView(taskView);
                        rightSide.addView(space);
                    }
                }
                container.removeAllViews();
                container.addView(scrollView);
                container.setVisibility(View.VISIBLE);
                loadingBar.setVisibility(View.GONE);
            }
        });
    }

    private void showPopupCreateTask(AtomicReference<Boolean> isDialogShowing) {
        task_members = new ArrayList<>();
        task_names = new ArrayList<>();
        membersProject = new ArrayList<>();
        memberNames = new ArrayList<>();
        leaderEmail = null;
        leaderName = null;
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view1 = LayoutInflater.from(this).inflate(R.layout.modal_create_task, null);
        TextView title = view1.findViewById(R.id.titleDialog);
        title.setText("Create Sub Task");

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
        // handle deadline button
        Button deadlineBtn = view1.findViewById(R.id.deadlineBtn);
        TextView deadlineView = view1.findViewById(R.id.deadlineView);
        AtomicReference<Boolean> isDatePickerShowing = new AtomicReference<>(false);
        deadlineBtn.setOnClickListener(v1 -> {
            showDatePicker(isDatePickerShowing, deadlineView, Boolean.TRUE);
        });
        // handle choose category button
        Button addCategoryBtn = view1.findViewById(R.id.addCategoryBtn);
        TextView categoryView = view1.findViewById(R.id.categoryView);
        addCategoryBtn.setOnClickListener(v1 -> {
            showCategoryDialog(categoryView);
        });
        // handle add member button
        Button addMemberBtn = view1.findViewById(R.id.addMemberBtn);
        memberList = view1.findViewById(R.id.memberList2);
        addMemberBtn.setOnClickListener(v1 -> {
            showAddMemberActivity();
        });
        // handle choose leader button
        Button addLeaderBtn = view1.findViewById(R.id.addLeaderBtn);
        leader = view1.findViewById(R.id.leaderTV);
        addLeaderBtn.setOnClickListener(v1 -> {
            showChooseLeaderActivity();
        });
        // handle create task button
        Button createTaskBtn = view1.findViewById(R.id.createTaskBtn);
        createTaskBtn.setOnClickListener(v1 -> {
            EditText taskTitle = view1.findViewById(R.id.taskTitleET);
            EditText desc = view1.findViewById(R.id.descET);
            if (taskTitle.getText().toString().isEmpty())
                taskTitle.setError("Task title is required");
            if (desc.getText().toString().isEmpty()) desc.setError("Description is required");
            if (leader.getVisibility() == View.GONE)
                Toast.makeText(this, "Leader is required", Toast.LENGTH_SHORT).show();
            else if (task_members.size() == 0)
                Toast.makeText(this, "Members are required", Toast.LENGTH_SHORT).show();
            else if (categoryView.getVisibility() == View.GONE)
                Toast.makeText(this, "Category is required", Toast.LENGTH_SHORT).show();
            else if (deadlineView.getVisibility() == View.GONE)
                Toast.makeText(this, "Deadline is required", Toast.LENGTH_SHORT).show();
            if (taskTitle.getText().toString().isEmpty() || desc.getText().toString().isEmpty() || leader.getVisibility() == View.GONE || task_members.size() == 0 || deadlineView.getVisibility() == View.GONE || categoryView.getVisibility() == View.GONE)
                return;
            bottomSheetDialog.dismiss();
            handleCreateTaskBtn(taskTitle.getText().toString(), desc.getText().toString(), leaderEmail, task_members, deadlineView.getText().toString(), categoryView.getText().toString());
        });
    }

    private void showDatePicker(AtomicReference<Boolean> isDatePickerShowing, TextView deadlineView, Boolean isShowTimePicker) {
        BottomSheetDialog bottomSheetDialog1 = new BottomSheetDialog(this);
        View view2 = LayoutInflater.from(this).inflate(R.layout.modal_date_picker, null);
        bottomSheetDialog1.setContentView(view2);
        if (isDatePickerShowing.get()) {
            return;
        }
        isDatePickerShowing.set(true);
        bottomSheetDialog1.show();
        bottomSheetDialog1.setOnDismissListener(dialog -> {
            isDatePickerShowing.set(false);
        });
        bottomSheetDialog1.setOnCancelListener(dialog -> {
            isDatePickerShowing.set(false);
        });
        TextView timeBtn = view2.findViewById(R.id.timeBtn);
        ConstraintLayout timeContainer = view2.findViewById(R.id.timeContainer);
        if (!isShowTimePicker) {
            timeContainer.setVisibility(View.GONE);
        }
        AtomicReference<Boolean> isTimePickerShow = new AtomicReference<>(false);
        CalendarView datePicker = view2.findViewById(R.id.datePicker);
        if (deadlineView.getVisibility() == View.VISIBLE) {
            String deadlineDate = deadlineView.getText().toString().split(" - ")[0];
            String deadlineTime = "";
            if (isShowTimePicker)
                deadlineTime = deadlineView.getText().toString().split(" - ")[1].replace(".", ":");
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy", Locale.ENGLISH);
            try {
                Date date = formatter.parse(deadlineDate);
                datePicker.setDate(date.getTime());
            } catch (Exception e) {
                e.printStackTrace();
            }
            timeBtn.setText(deadlineTime);
        }
        timeBtn.setOnClickListener(v2 -> {
            if (isTimePickerShow.get()) {
                return;
            }
            isTimePickerShow.set(true);
            BottomSheetDialog bottomSheetDialog2 = new BottomSheetDialog(this);
            View view3 = LayoutInflater.from(this).inflate(R.layout.modal_time_picker, null);
            bottomSheetDialog2.setContentView(view3);
            NumberPicker hourPicker = view3.findViewById(R.id.hourPicker);
            NumberPicker minutePicker = view3.findViewById(R.id.monthPicker);
            NumberPicker amPmPicker = view3.findViewById(R.id.yearPicker);
            hourPicker.setMinValue(1);
            hourPicker.setMaxValue(12);
            hourPicker.setFormatter(value -> String.format(new Locale("en"), "%02d", value));
            minutePicker.setMinValue(0);
            minutePicker.setMaxValue(59);
            minutePicker.setFormatter(value -> String.format(new Locale("en"), "%02d", value));
            ArrayList<String> amPm = new ArrayList<>();
            amPm.add("AM");
            amPm.add("PM");
            amPmPicker.setDisplayedValues(amPm.toArray(new String[0]));
            amPmPicker.setMinValue(0);
            amPmPicker.setMaxValue(1);
            amPmPicker.setValue(timeBtn.getText().toString().contains("AM") ? 0 : 1);
            hourPicker.setValue(timeBtn.getText().toString().contains("AM") ? Integer.parseInt(timeBtn.getText().toString().split(":")[0]) : Integer.parseInt(timeBtn.getText().toString().split(":")[0]) + 12);
            minutePicker.setValue(Integer.parseInt(timeBtn.getText().toString().split(":")[1].split(" ")[0]));
            bottomSheetDialog2.show();
            bottomSheetDialog2.setOnDismissListener(v3 -> {
                isTimePickerShow.set(false);
            });
            Button applyBtn1 = view3.findViewById(R.id.applyBtn);
            Button cancelBtn1 = view3.findViewById(R.id.cancelBtn);
            applyBtn1.setOnClickListener(v3 -> {
                timeBtn.setText(String.format(new Locale("en"), "%02d:%02d %s", hourPicker.getValue() > 12 ? hourPicker.getValue() - 12 : hourPicker.getValue() == 0 ? 12 : hourPicker.getValue(), minutePicker.getValue(), amPm.get(amPmPicker.getValue())));
                bottomSheetDialog2.dismiss();
            });
            cancelBtn1.setOnClickListener(v3 -> {
                bottomSheetDialog2.dismiss();
            });
        });
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
            String[] months = new String[12];
            for (int i = 0; i < 12; i++) {
                months[i] = new SimpleDateFormat("MMMM", Locale.ENGLISH).format(new Date(0, i, 1));
            }
            monthPickerNumber.setDisplayedValues(months);
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
        deadlineDate.observe(this, date -> {
            datePicker.setDate(date.getTime());
        });
        Button applyBtn = view2.findViewById(R.id.applyBtn);
        Button cancelBtn = view2.findViewById(R.id.cancelBtn);
        applyBtn.setOnClickListener(v2 -> {
            SimpleDateFormat formatter;
            String deadline;
            if (isShowTimePicker) {
                formatter = new SimpleDateFormat("dd/MM/yy - ", Locale.ENGLISH);
                deadline = formatter.format(deadlineDate.getValue());
                deadline += timeBtn.getText().toString().replace(":", ".");
            } else {
                formatter = new SimpleDateFormat("dd/MM/yy", Locale.ENGLISH);
                deadline = formatter.format(deadlineDate.getValue());
            }
            deadlineView.setText(deadline);
            deadlineView.setVisibility(View.VISIBLE);
            bottomSheetDialog1.dismiss();
        });
        cancelBtn.setOnClickListener(v2 -> {
            bottomSheetDialog1.dismiss();
        });
    }

    private void showCategoryDialog(TextView categoryView) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.modal_task_category, null);
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();
        LinearLayout meetingCategory = view.findViewById(R.id.meetingCategory);
        LinearLayout taskCategory = view.findViewById(R.id.taskCategory);
        CheckBox meetingCB = view.findViewById(R.id.meetingCB);
        CheckBox taskCB = view.findViewById(R.id.taskCB);
        if (categoryView.getText().toString().equals("Meeting")) {
            meetingCB.setChecked(true);
        } else if (categoryView.getText().toString().equals("Task")) {
            taskCB.setChecked(true);
        }
        meetingCategory.setOnClickListener(v -> {
            meetingCB.setChecked(true);
            taskCB.setChecked(false);
        });
        taskCategory.setOnClickListener(v -> {
            taskCB.setChecked(true);
            meetingCB.setChecked(false);
        });
        bottomSheetDialog.setOnDismissListener(dialog -> {
            if (meetingCB.isChecked()) {
                categoryView.setBackground(ContextCompat.getDrawable(this, R.drawable.rounded_corner_24_purple));
                categoryView.setTextColor(Color.parseColor("#5856D6"));
                categoryView.setText("Meeting");
                categoryView.setVisibility(View.VISIBLE);
            } else if (taskCB.isChecked()) {
                categoryView.setBackground(ContextCompat.getDrawable(this, R.drawable.rounded_corner_24_orange));
                categoryView.setTextColor(Color.parseColor("#FFAF45"));
                categoryView.setText("Task");
                categoryView.setVisibility(View.VISIBLE);
            }
        });
        bottomSheetDialog.setOnCancelListener(dialog -> {
            bottomSheetDialog.dismiss();
        });
    }

    private void showAddMemberActivity() {
        Dialog loading = createLoadingDialog();
        Intent intent = new Intent(this, AddMember.class);
        intent.putExtra("category", 3);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("projects").document(projectId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Map<String, Object> project = task.getResult().getData();
                ArrayList<Map<String, Object>> members = (ArrayList<Map<String, Object>>) project.get("members");
                ArrayList<String> memberEmails = new ArrayList<>();
                MutableLiveData<ArrayList<String>> memberNames = new MutableLiveData<>(new ArrayList<>());
                for (Map<String, Object> member : members) {
                    db.collection("users").whereEqualTo("email", member.get("email")).get().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            memberEmails.add((String) member.get("email"));
                            ArrayList<String> names = memberNames.getValue();
                            names.add(task1.getResult().getDocuments().get(0).get("name").toString());
                            memberNames.setValue(names);
                        }
                    });
                }
                ArrayList<String> names = memberNames.getValue();
                db.collection("users").whereEqualTo("email", project.get("user_created")).get().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        memberEmails.add((String) project.get("user_created"));
                        names.add(task1.getResult().getDocuments().get(0).get("name").toString());
                        memberNames.setValue(names);
                    }
                });
                memberNames.observe(this, strings -> {
                    if (strings.size() == members.size() + 1) {
                        intent.putStringArrayListExtra("members", memberEmails);
                        intent.putStringArrayListExtra("name", strings);
                        intent.putExtra("task_members", task_members);
                        intent.putExtra("task_names", task_names);
                        startActivityForResult(intent, 3);
                        loading.dismiss();
                    }
                });
            }
        });
    }

    private void showChooseLeaderActivity() {
        Dialog loading = createLoadingDialog();
        Intent intent = new Intent(this, AddMember.class);
        intent.putExtra("category", 2);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("projects").document(projectId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Map<String, Object> project = task.getResult().getData();
                ArrayList<Map<String, Object>> members = (ArrayList<Map<String, Object>>) project.get("members");
                ArrayList<String> memberEmails = new ArrayList<>();
                MutableLiveData<ArrayList<String>> memberNames = new MutableLiveData<>(new ArrayList<>());
                for (Map<String, Object> member : members) {
                    db.collection("users").whereEqualTo("email", member.get("email")).get().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            ArrayList<String> names = memberNames.getValue();
                            memberEmails.add((String) member.get("email"));
                            names.add(task1.getResult().getDocuments().get(0).get("name").toString());
                            memberNames.setValue(names);
                        }
                    });
                }
                ArrayList<String> names = memberNames.getValue();
                db.collection("users").whereEqualTo("email", project.get("user_created")).get().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        memberEmails.add((String) project.get("user_created"));
                        names.add(task1.getResult().getDocuments().get(0).get("name").toString());
                        memberNames.setValue(names);
                    }
                });
                memberNames.observe(this, strings -> {
                    if (strings.size() == members.size() + 1) {
                        intent.putStringArrayListExtra("members", memberEmails);
                        intent.putStringArrayListExtra("name", strings);
                        intent.putExtra("leader_email", leaderEmail);
                        intent.putExtra("leader_name", leaderName);
                        startActivityForResult(intent, 2);
                        loading.dismiss();
                    }
                });
            }
        });
    }

    private void handleEditTaskBtn(String taskTitle, String desc, String leader, ArrayList<String> members, String deadline, String category) {
        Dialog loading = createLoadingDialog();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("tasks").document(taskId).update("title", taskTitle);
        db.collection("tasks").document(taskId).update("description", desc);
        db.collection("tasks").document(taskId).update("deadline", deadline);
        db.collection("tasks").document(taskId).update("category", category);

        ArrayList<Map<String, Object>> membersList = new ArrayList<>();
        for (String member : members) {
            Map<String, Object> memberMap = new HashMap<>();
            memberMap.put("email", member);
            membersList.add(memberMap);
            if (member.equals(leader)) {
                memberMap.put("isLeader", true);
            } else {
                memberMap.put("isLeader", false);
            }
        }
        if (!members.contains(leader)) {
            Map<String, Object> leaderMap = new HashMap<>();
            leaderMap.put("email", leader);
            leaderMap.put("isLeader", true);
        }
        db.collection("tasks").document(taskId).update("members", membersList).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                loading.dismiss();
                Toast.makeText(this, "Edit task successfully", Toast.LENGTH_SHORT).show();
                loadUI();
            }
        });
    }

    private void handleCreateTaskBtn(String taskTitle, String desc, String leader, ArrayList<String> members, String deadline, String category) {
        Dialog loading = createLoadingDialog();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> newTask = new HashMap<>();
        newTask.put("title", taskTitle);
        newTask.put("description", desc);
        newTask.put("deadline", deadline);
        newTask.put("category", category);
        newTask.put("projectId", projectId);
        newTask.put("parentId", taskId);

        ArrayList<Map<String, Object>> membersList = new ArrayList<>();
        for (String member : members) {
            Map<String, Object> memberMap = new HashMap<>();
            memberMap.put("email", member);
            membersList.add(memberMap);
            if (member.equals(leader)) {
                memberMap.put("isLeader", true);
            } else {
                memberMap.put("isLeader", false);
            }
        }
        if (!members.contains(leader)) {
            Map<String, Object> leaderMap = new HashMap<>();
            leaderMap.put("email", leader);
            leaderMap.put("isLeader", true);
        }
        newTask.put("status", 0);
        newTask.put("members", membersList);
        db.collection("tasks").add(newTask).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                loading.dismiss();
                Toast.makeText(this, "Create task successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, TaskDetail.class);
                intent.putExtra("taskId", task.getResult().getId());
                intent.putExtra("projectId", projectId);
                intent.putExtra("parentId", taskId);
                for (String member : members) {
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy", Locale.ENGLISH);
                    db.collection("users").whereEqualTo("email", member).get().addOnCompleteListener(task2 -> {
                        if (task2.isSuccessful()) {
                            db.collection("users").document(task2.getResult().getDocuments().get(0).getId()).collection("notification_logs").add(new HashMap<String, Object>() {{
                                put("type", 3);
                                put("message", "You have been assigned to task " + taskTitle);
                                put("date", formatter.format(new Date()));
                                put("sender", email);
                                put("taskId", task.getResult().getId());
                                put("projectId", projectId);
                                put("isRead", false);
                            }});
                        }
                    });
                }
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uploadFile(data.getData());
        }
        if (requestCode == 3 && resultCode == RESULT_OK) {
            task_members = data.getStringArrayListExtra("task_members");
            task_names = data.getStringArrayListExtra("task_names");
            memberList.removeAllViews();
            for (int i = 0; i < Math.min(2, task_members.size()); i++) {
                TextView member = createItemMember(task_names.get(i));
                memberList.addView(member);
            }
            if (task_members.size() > 2) {
                TextView more = createItemMember((task_members.size() - 2) + " more...");
                memberList.addView(more);
            }
        } else if (requestCode == 2 && resultCode == RESULT_OK) {
            leaderEmail = data.getStringExtra("leader_email");
            leaderName = data.getStringExtra("leader_name");
            leader.setText(leaderName);
            leader.setVisibility(View.VISIBLE);
            if (task_members.contains(leaderEmail)) {
                return;
            }
            task_members.add(leaderEmail);
            task_names.add(leaderName);
            memberList.removeAllViews();
            for (int i = 0; i < Math.min(2, task_members.size()); i++) {
                TextView member = createItemMember(task_names.get(i));
                memberList.addView(member);
            }
            if (task_members.size() > 2) {
                TextView more = createItemMember((task_members.size() - 2) + " more...");
                memberList.addView(more);
            }
        } else if (requestCode == 1234 && resultCode == RESULT_OK) {
            int image = data.getIntExtra("image", 0);
            ImageView cover = findViewById(R.id.cover);
            cover.setImageResource(ImageArray.getCoverProjectImage().get(image));
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("projects").document(projectId).update("cover", image);
        } else if (requestCode == 5678 && resultCode == RESULT_OK) {
            membersProject = data.getStringArrayListExtra("members");
            memberNames = data.getStringArrayListExtra("names");
            memberList.removeAllViews();
            for (int i = 0; i < Math.min(2, memberNames.size()); i++) {
                TextView member = createItemMember(memberNames.get(i));
                memberList.addView(member);
            }
            if (memberNames.size() > 2) {
                TextView more = createItemMember((memberNames.size() - 2) + " more...");
                memberList.addView(more);
            }
        }
    }

    private void showResultTab() {
        container.setVisibility(View.GONE);
        create_btn.setVisibility(View.GONE);
        container.removeAllViews();

        View resultPane = LayoutInflater.from(this).inflate(R.layout.task_detail_result, null);
        Button btnAtach = resultPane.findViewById(R.id.attachBtn);
        Button btnLink = resultPane.findViewById(R.id.linkBtn);
        TextView moreAllBtn = resultPane.findViewById(R.id.moreAllBtn);

        listComments = resultPane.findViewById(R.id.list_comments);
        listItem = resultPane.findViewById(R.id.list_item);
        listItemAttached = resultPane.findViewById(R.id.list_link);
        //show list item attachment
        showLiskItem(listItem);
        showLiskLink(listItemAttached);

        moreAllBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, Comment.class);
            intent.putExtra("taskId", taskId);
            intent.putExtra("projectId", projectId);
            startActivity(intent);
        });
        btnAtach.setOnClickListener(v -> showAttachDialog());
        btnLink.setOnClickListener(v -> {
            showLinkDialog();
        });

        showComments();

        container.addView(resultPane);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        resultPane.setLayoutParams(params);
        loadingBar.setVisibility(View.GONE);
        container.setVisibility(View.VISIBLE);
    }

    private void showLiskItem(LinearLayout listItem) {
        listItem.removeAllViews();
        float dip10 = 10f;
        Resources r = getResources();
        float px10 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip10, r.getDisplayMetrics());
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("tasks").document(taskId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ArrayList<String> files = (ArrayList<String>) task.getResult().get("attachments");
                if (files != null) {
                    for (String file : files.subList(0, Math.min(files.size(), 2))) {
                        View fileTextView = LayoutInflater.from(this).inflate(R.layout.item_attach2, null);
                        TextView item_name = fileTextView.findViewById(R.id.item_name);
                        item_name.setText(getFileName(Uri.parse(file)));
                        listItem.addView(fileTextView);
                        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) fileTextView.getLayoutParams();
                        layoutParams.setMargins(0, 0, 0, (int) px10);
                        item_name.setOnClickListener(v -> {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(file));
                            startActivity(intent);
                        });
                    }
                    if (files.size() > 2) {
                        TextView textView = new TextView(this);
                        textView.setText("More");
                        textView.setBackgroundResource(R.drawable.rounded_corner_24_bw);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        // add padding
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
                        listItem.addView(textView);
                    }
                }
            }
        });
    }

    private void showLiskLink(LinearLayout listLink) {
        listLink.removeAllViews();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        float dip10 = 10f;
        Resources r = getResources();
        float px10 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip10, r.getDisplayMetrics());
        db.collection("tasks").document(taskId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ArrayList<String> links = (ArrayList<String>) task.getResult().get("links");
                if (links != null) {
                    for (String link : links.subList(0, Math.min(links.size(), 2))) {
                        View item_link = LayoutInflater.from(this).inflate(R.layout.item_link2, null);
                        TextView linkTV = item_link.findViewById(R.id.item_name);
                        linkTV.setText(link);
                        listItemAttached.addView(item_link);
                        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) item_link.getLayoutParams();
                        layoutParams.setMargins(0, 0, 0, (int) px10);
                    }
                    if (links.size() > 2) {
                        TextView textView = new TextView(this);
                        textView.setText("More");
                        textView.setBackgroundResource(R.drawable.rounded_corner_24_bw);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        // add padding
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
                        listLink.addView(textView);
                    }
                }
            }
        });
    }

    private void showLinkDialog() {
        Dialog dialog;
        dialog = new Dialog(TaskDetail.this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.popup_link);
        EditText search_field = dialog.findViewById(R.id.search_field);
        TextView add_btn = dialog.findViewById(R.id.add_btn);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        float dip10 = 10f;
        Resources r = getResources();
        float px10 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip10, r.getDisplayMetrics());
        db.collection("tasks").document(taskId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ArrayList<String> links = (ArrayList<String>) task.getResult().get("links");
                if (links != null) {
                    LinearLayout listItemAttached = dialog.findViewById(R.id.list_item_atached);
                    for (String link : links) {
                        View item_link = LayoutInflater.from(this).inflate(R.layout.item_link, null);
                        TextView linkTV = item_link.findViewById(R.id.item_name);
                        linkTV.setText(link);
                        ImageView deleteBtn = item_link.findViewById(R.id.deleteBtn);
                        listItemAttached.addView(item_link);
                        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) item_link.getLayoutParams();
                        layoutParams.setMargins(0, 0, 0, (int) px10);
                        deleteBtn.setOnClickListener(v2 -> {
                            listItemAttached.removeView(item_link);
                            links.remove(link);
                            db.collection("tasks").document(taskId).update("links", links);
                        });
                    }
                }
            }
        });
        add_btn.setOnClickListener(v1 -> {
            String link = search_field.getText().toString();
            if (link.isEmpty()) {
                search_field.setError("Link is required");
                return;
            }
            if (!link.contains("http://") && !link.contains("https://")) {
                search_field.setError("Link must not contain http:// or https://");
                return;
            }
            View item_link = LayoutInflater.from(this).inflate(R.layout.item_link, null);
            TextView linkTV = item_link.findViewById(R.id.item_name);
            linkTV.setText(link);
            ImageView deleteBtn = item_link.findViewById(R.id.deleteBtn);

            linkTV.setOnClickListener(v2 -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                startActivity(intent);
            });
            ((LinearLayout) dialog.findViewById(R.id.list_item_atached)).addView(item_link);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) item_link.getLayoutParams();
            layoutParams.setMargins(0, 0, 0, (int) px10);
            db.collection("tasks").document(taskId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    ArrayList<String> links = (ArrayList<String>) task.getResult().get("links");
                    if (links == null) {
                        links = new ArrayList<>();
                    }
                    links.add(link);
                    db.collection("tasks").document(taskId).update("links", links);
                    ArrayList<String> finalLinks = links;
                    deleteBtn.setOnClickListener(v2 -> {
                        ((LinearLayout) dialog.findViewById(R.id.list_item_atached)).removeView(item_link);
                        finalLinks.remove(link);
                        db.collection("tasks").document(taskId).update("links", finalLinks);
                    });
                }
            });
        });
        Button btnCancel = dialog.findViewById(R.id.btn_cancle);
        btnCancel.setOnClickListener(v1 -> {
            dialog.dismiss();
            showLiskLink(listItemAttached);
        });
        dialog.show();
    }

    private void showAttachDialog() {
        Dialog dialog = new Dialog(TaskDetail.this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.popup_attachment);

        Button btnCancel = dialog.findViewById(R.id.btn_cancle);
        btnCancel.setOnClickListener(v -> {
                    dialog.dismiss();
                    showLiskItem(listItem);
                }
        );
        Button btnUpload = dialog.findViewById(R.id.btn_upload);
        btnUpload.setOnClickListener(v -> {
            currentDialog = dialog;
            chooseFile();
        });

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("tasks").document(taskId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ArrayList<String> files = (ArrayList<String>) task.getResult().get("attachments");
                if (files != null) {
                    LinearLayout listItemAttached = dialog.findViewById(R.id.list_item_atached);
                    for (String file : files) {
                        View fileTextView = LayoutInflater.from(this).inflate(R.layout.item_attach, null);
                        TextView item_name = fileTextView.findViewById(R.id.item_name);
                        item_name.setText(getFileName(Uri.parse(file)));
                        ImageView closeIcon = fileTextView.findViewById(R.id.deleteBtn);
                        ProgressBar progressBar = fileTextView.findViewById(R.id.progressBar2);
                        progressBar.setVisibility(View.GONE);
                        listItemAttached.addView(fileTextView);
                        float dip10 = 10f;
                        Resources r = getResources();
                        float px10 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip10, r.getDisplayMetrics());
                        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) fileTextView.getLayoutParams();
                        layoutParams.setMargins(0, 0, 0, (int) px10);
                        item_name.setOnClickListener(v -> {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(file));
                            startActivity(intent);
                        });
                        closeIcon.setOnClickListener(v -> {
                            listItemAttached.removeView(fileTextView);
                            files.remove(file);
                            FirebaseStorage storage = FirebaseStorage.getInstance();
                            StorageReference storageRef = storage.getReferenceFromUrl(file);
                            storageRef.delete();
                            db.collection("tasks").document(taskId).update("attachments", files);
                        });
                    }
                }
            }
        });

        dialog.show();
    }

    private void chooseFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    private void uploadFile(Uri dataUpload) {
        if (currentDialog != null && dataUpload != null) {
            LinearLayout listItemAttached = currentDialog.findViewById(R.id.list_item_atached);
            View fileTextView = LayoutInflater.from(this).inflate(R.layout.item_attach, null);
            TextView item_name = fileTextView.findViewById(R.id.item_name);
            item_name.setText(getFileName(dataUpload));
            ImageView closeIcon = fileTextView.findViewById(R.id.deleteBtn);
            listItemAttached.addView(fileTextView);
            float dip10 = 10f;
            Resources r = getResources();
            float px10 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip10, r.getDisplayMetrics());
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) fileTextView.getLayoutParams();
            layoutParams.setMargins(0, 0, 0, (int) px10);
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            StorageReference fileRef = storageRef.child("uploads/" + getFileName(dataUpload) + "_" + System.currentTimeMillis());
            UploadTask uploadTask = fileRef.putFile(dataUpload);
            ProgressBar progressBar = fileTextView.findViewById(R.id.progressBar2);
            uploadTask.addOnProgressListener(taskSnapshot -> {
                closeIcon.setOnClickListener(v -> {
                    uploadTask.cancel();
                    listItemAttached.removeView(fileTextView);
                });
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                progressBar.setProgress((int) progress);
            }).addOnSuccessListener(taskSnapshot -> {
                progressBar.setProgress(100);
                progressBar.setVisibility(View.GONE);
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("tasks").document(taskId).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<String> files = (ArrayList<String>) task.getResult().get("attachments");
                        if (files == null) {
                            files = new ArrayList<>();
                        }
                        ArrayList<String> finalFiles = files;
                        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            finalFiles.add(uri.toString());
                            db.collection("tasks").document(taskId).update("attachments", finalFiles);
                            item_name.setOnClickListener(v -> {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri.toString()));
                                startActivity(intent);
                            });
                            closeIcon.setOnClickListener(v -> {
                                listItemAttached.removeView(fileTextView);
                                finalFiles.remove(uri.toString());
                                FirebaseStorage storage1 = FirebaseStorage.getInstance();
                                StorageReference storageRef1 = storage1.getReferenceFromUrl(uri.toString());
                                storageRef1.delete();
                                db.collection("tasks").document(taskId).update("attachments", finalFiles);
                            });
                        });
                        Toast.makeText(this, "Upload file successfully", Toast.LENGTH_SHORT).show();
                    }
                });
            }).addOnFailureListener(e -> {
                listItemAttached.removeView(fileTextView);
                Toast.makeText(this, "Upload file failed", Toast.LENGTH_SHORT).show();
            });
        }
    }


    @SuppressLint("Range")
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }


    private void showInformationTab() {
        loadingBar.setVisibility(View.VISIBLE);
        container.setVisibility(View.GONE);
        create_btn.setVisibility(View.GONE);
        container.removeAllViews();
        View informationPane = LayoutInflater.from(this).inflate(R.layout.task_detail_information, null);
        TextView leader = informationPane.findViewById(R.id.leader_tv);
        LinearLayout memberList = informationPane.findViewById(R.id.memberList2);
        TextView category = informationPane.findViewById(R.id.category);
        TextView deadline = informationPane.findViewById(R.id.deadline_tv);
        //edit status
        Button btnEditStatus = informationPane.findViewById(R.id.editStatus);
        TextView status = informationPane.findViewById(R.id.status);
        //handle edit status task
        btnEditStatus.setOnClickListener(v -> {
            showPopupStatus(status);
        });
        //show status task
        db = FirebaseFirestore.getInstance();
        db.collection("tasks").document(taskId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Map<String, Object> _task = task.getResult().getData();
                if (task.getResult().getLong("status") == 0) {
                    status.setVisibility(View.GONE);
                } else if (task.getResult().getLong("status") == 1) {
                    status.setVisibility(View.VISIBLE);
                    status.setText("On going");
                } else {
                    status.setBackgroundResource(R.drawable.rounded_corner_24_blue);
                    status.setVisibility(View.VISIBLE);
                    status.setText("Done");
                    status.setTextColor(getResources().getColor(R.color.white));
                }
                category.setText(task.getResult().getString("category"));
                deadline.setText(task.getResult().getString("deadline"));
                ArrayList<Map<String, Object>> members = (ArrayList<Map<String, Object>>) task.getResult().get("members");
                for (Map<String, Object> member : members) {
                    String _email = (String) member.get("email");
                    db.collection("users").whereEqualTo("email", _email).get().addOnCompleteListener(task2 -> {
                        if (task2.isSuccessful()) {
                            Map<String, Object> user = task2.getResult().getDocuments().get(0).getData();
                            if ((boolean) member.get("isLeader"))
                                leader.setText((String) user.get("name"));
                            else
                                memberList.addView(createItemMember((String) user.get("name")));
                        }
                    });
                }
                Button reviewbtn = informationPane.findViewById(R.id.btn_review);
                View viewReview = informationPane.findViewById(R.id.view_review);
                TextView beforeDlTV = informationPane.findViewById(R.id.before_dl_tv);

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy - HH.mm a", Locale.US);
                Boolean isLeader = false;
                for (Map<String, Object> member : members) {
                    if ((boolean) member.get("isLeader") && member.get("email").equals(email)) {
                        isLeader = true;
                        break;
                    }
                }
                if (_task.get("review") != null) {
                    Map<String, Object> review = (Map<String, Object>) _task.get("review");
                    reviewbtn.setVisibility(View.GONE);
                    beforeDlTV.setVisibility(View.GONE);
                    viewReview.setVisibility(View.VISIBLE);
                    TextView reviewTitle = viewReview.findViewById(R.id.etDescProject);
                    reviewTitle.setText(review.get("feedback").toString());
                    ImageView star1 = viewReview.findViewById(R.id.img_start1);
                    ImageView star2 = viewReview.findViewById(R.id.img_start2);
                    ImageView star3 = viewReview.findViewById(R.id.img_start3);
                    ImageView star4 = viewReview.findViewById(R.id.img_start4);
                    ImageView star5 = viewReview.findViewById(R.id.img_start5);
                    MutableLiveData<Integer> stars = new MutableLiveData<>(0);
                    stars.observe(this, integer -> {
                        if (integer >= 1) {
                            star1.setImageResource(R.drawable.ic_star2);
                        } else {
                            star1.setImageResource(R.drawable.ic_star3);
                        }
                        if (integer >= 2) {
                            star2.setImageResource(R.drawable.ic_star2);
                        } else {
                            star2.setImageResource(R.drawable.ic_star3);
                        }
                        if (integer >= 3) {
                            star3.setImageResource(R.drawable.ic_star2);
                        } else {
                            star3.setImageResource(R.drawable.ic_star3);
                        }
                        if (integer >= 4) {
                            star4.setImageResource(R.drawable.ic_star2);
                        } else {
                            star4.setImageResource(R.drawable.ic_star3);
                        }
                        if (integer >= 5) {
                            star5.setImageResource(R.drawable.ic_star2);
                        } else {
                            star5.setImageResource(R.drawable.ic_star3);
                        }
                    });
                    stars.setValue(Math.toIntExact((Long) review.get("stars")));
                }
                if (isLeader) {
                    try {
                        Date deadline2 = sdf.parse(_task.get("deadline").toString());
                        if (deadline2.before(new Date()) || deadline2.equals(new Date())) {
                            reviewbtn.setVisibility(View.VISIBLE);
                            beforeDlTV.setVisibility(View.GONE);
                            viewReview.setVisibility(View.GONE);
                        } else {
                            reviewbtn.setVisibility(View.GONE);
                            beforeDlTV.setVisibility(View.VISIBLE);
                            viewReview.setVisibility(View.GONE);
                        }
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                    reviewbtn.setOnClickListener(v -> {
                        AtomicReference<Boolean> isDialogShowing = new AtomicReference<>(false);
                        showPopupReview(isDialogShowing);
                    });
                }
            }
        });
        memberList.setOnClickListener(v -> {
            Intent intent = new Intent(TaskDetail.this, ViewMembers.class);
            intent.putExtra("taskId", taskId);
            startActivity(intent);
        });
        container.addView(informationPane);
        loadingBar.setVisibility(View.GONE);
        container.setVisibility(View.VISIBLE);
    }

    private void showPopupStatus(TextView statusView) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view1 = LayoutInflater.from(this).inflate(R.layout.modal_update_task_status, null);
        bottomSheetDialog.setContentView(view1);
        bottomSheetDialog.show();
        LinearLayout onGoingStatus = view1.findViewById(R.id.statusOnGoing);
        LinearLayout doneStatus = view1.findViewById(R.id.statusDone);
        CheckBox onGoing = view1.findViewById(R.id.cbOnGoing);
        CheckBox done = view1.findViewById(R.id.cbDone);
        if (statusView.getVisibility() == View.VISIBLE) {
            if (statusView.getText().toString().equals("On Going")) {
                onGoing.setChecked(true);
            } else if (statusView.getText().toString().equals("Done")) {
                done.setChecked(true);
            }
        }
        onGoingStatus.setOnClickListener(v -> {
            onGoing.setChecked(true);
            done.setChecked(false);
            db.collection("tasks").document(taskId).update("status", 1).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Update status successfully", Toast.LENGTH_SHORT).show();
                    bottomSheetDialog.dismiss();
                }
            });
        });
        doneStatus.setOnClickListener(v -> {
            done.setChecked(true);
            onGoing.setChecked(false);
            db.collection("tasks").document(taskId).update("status", 2).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Update status successfully", Toast.LENGTH_SHORT).show();
                    bottomSheetDialog.dismiss();
                }
            });
        });
        bottomSheetDialog.setOnDismissListener(dialog -> {
            if (onGoing.isChecked()) {
                statusView.setBackground(ContextCompat.getDrawable(this, R.drawable.rounded_corner_24_on_going));
                statusView.setTextColor(Color.parseColor("#34C759"));
                statusView.setText("On Going");
                statusView.setVisibility(View.VISIBLE);
            } else if (done.isChecked()) {
                statusView.setBackground(ContextCompat.getDrawable(this, R.drawable.rounded_corner_24_blue));
                statusView.setTextColor(Color.parseColor("#FFFFFF"));
                statusView.setText("Done");
                statusView.setVisibility(View.VISIBLE);
            }
        });
        bottomSheetDialog.setOnCancelListener(dialog -> {
            bottomSheetDialog.dismiss();
        });
    }

    private void showPopupReview(AtomicReference<Boolean> isDialogShowing) {
        Dialog loading = createLoadingDialog();
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view1 = LayoutInflater.from(this).inflate(R.layout.modal_review_project, null);
        bottomSheetDialog.setContentView(view1);
        if (isDialogShowing.get()) {
            return;
        }
        isDialogShowing.set(true);
        bottomSheetDialog.setOnDismissListener(dialog -> {
            isDialogShowing.set(false);
        });
        bottomSheetDialog.setOnCancelListener(dialog -> {
            isDialogShowing.set(false);
        });
        bottomSheetDialog.show();
        loading.dismiss();
        EditText reviewET = view1.findViewById(R.id.etReviewProject);
        Button submitBtn = view1.findViewById(R.id.btn_submit);
        ImageView star1 = view1.findViewById(R.id.img_start1);
        ImageView star2 = view1.findViewById(R.id.img_start2);
        ImageView star3 = view1.findViewById(R.id.img_start3);
        ImageView star4 = view1.findViewById(R.id.img_start4);
        ImageView star5 = view1.findViewById(R.id.img_start5);
        MutableLiveData<Integer> stars = new MutableLiveData<>(0);
        stars.observe(this, integer -> {
            if (integer >= 1) {
                star1.setImageResource(R.drawable.ic_star2);
            } else {
                star1.setImageResource(R.drawable.ic_star3);
            }
            if (integer >= 2) {
                star2.setImageResource(R.drawable.ic_star2);
            } else {
                star2.setImageResource(R.drawable.ic_star3);
            }
            if (integer >= 3) {
                star3.setImageResource(R.drawable.ic_star2);
            } else {
                star3.setImageResource(R.drawable.ic_star3);
            }
            if (integer >= 4) {
                star4.setImageResource(R.drawable.ic_star2);
            } else {
                star4.setImageResource(R.drawable.ic_star3);
            }
            if (integer >= 5) {
                star5.setImageResource(R.drawable.ic_star2);
            } else {
                star5.setImageResource(R.drawable.ic_star3);
            }
        });
        star1.setOnClickListener(v -> stars.setValue(1));
        star2.setOnClickListener(v -> stars.setValue(2));
        star3.setOnClickListener(v -> stars.setValue(3));
        star4.setOnClickListener(v -> stars.setValue(4));
        star5.setOnClickListener(v -> stars.setValue(5));

        submitBtn.setOnClickListener(v1 -> {
            if (stars.getValue() == 0) {
                Toast.makeText(this, "Please rate the project", Toast.LENGTH_SHORT).show();
                return;
            }
            if (reviewET.getText().toString().isEmpty()) {
                Toast.makeText(this, "Feedback is required", Toast.LENGTH_SHORT).show();
                return;
            }
            String review = reviewET.getText().toString();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Map<String, Object> newReview = new HashMap<>();
            newReview.put("feedback", review);
            newReview.put("stars", stars.getValue());
            db.collection("tasks").document(taskId).update("review", newReview);
            bottomSheetDialog.dismiss();
            loadUI();
        });

    }

    private TextView createItemMember(String name) {
        TextView textView = new TextView(this);
        textView.setText(name);
        textView.setBackgroundResource(R.drawable.rounded_corner_24_bw);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        // add padding
        float dip10 = 10f;
        Resources r = getResources();
        float px10 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip10, r.getDisplayMetrics());
        float dip12 = 12f;
        float px12 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip12, r.getDisplayMetrics());
        float dip4 = 4f;
        float px4 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip4, r.getDisplayMetrics());
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

                        // Update UI after getting comments
                        listComments.removeAllViews();
                        for (Map<String, Object> comment : commentList) {
                            View item_comment = getLayoutInflater().inflate(R.layout.item_comment, null);
                            TextView name = item_comment.findViewById(R.id.nameCmt);
                            TextView content = item_comment.findViewById(R.id.contentCmt);
                            TextView date = item_comment.findViewById(R.id.dateCmt);
                            ImageView avatar = item_comment.findViewById(R.id.imageView123);

                            String email = (String) comment.get("email");
                            name.setText((String) comment.get("name"));
                            content.setText((String) comment.get("message"));

                            // Format date
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

                            listComments.addView(item_comment);
                        }
                    } else {
                        Log.d("Firestore", "Error getting comments: ", task.getException());
                    }
                });
    }

    public String formatDateTime(long timestamp) {
        Date date = new Date(timestamp);

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy");
        return sdf.format(date);
    }

    public Dialog createLoadingDialog() {
        Dialog loading = new Dialog(this);
        loading.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        loading.setContentView(R.layout.loading);
        loading.show();
        return loading;
    }

}
