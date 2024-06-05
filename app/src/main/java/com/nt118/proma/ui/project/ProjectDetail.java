package com.nt118.proma.ui.project;

import static com.google.firebase.firestore.Filter.equalTo;
import static com.nt118.proma.ui.task.TaskDetail.setWindowFlag;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nt118.proma.R;
import com.nt118.proma.model.ImageArray;
import com.nt118.proma.ui.image.SetImage;
import com.nt118.proma.ui.member.AddMember;
import com.nt118.proma.ui.task.TaskDetail;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ProjectDetail extends AppCompatActivity {
    private PopupMenu popup;
    private String projectId;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.project_detail);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        Intent intent = getIntent();
        projectId = intent.getStringExtra("projectId");
        ImageView back = findViewById(R.id.img_Back);
        back.setOnClickListener(v -> {
            finish();
        });
        FloatingActionButton create_btn = findViewById(R.id.create_task_btn);
        AtomicReference<Boolean> isDialogShowing = new AtomicReference<>(false);
        create_btn.setOnClickListener(v -> {
            showPopupMenu(isDialogShowing);
        });
        ImageView menu_btn = findViewById(R.id.menu_btn);
        menu_btn.setOnClickListener(v -> {
            popup = new PopupMenu(this, v, 5);
            popup.getMenuInflater().inflate(R.menu.project_detail_menu, popup.getMenu());
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
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_change_cover){
                    Intent intent2 = new Intent(this, SetImage.class);
                    intent2.putExtra("type", "cover");
                    intent2.putIntegerArrayListExtra("images", new ImageArray().getCoverProjectImage());
                    startActivity(intent2);
                }
                return false;
            });
            popup.show();
        });
        ScrollView container = findViewById(R.id.projectDetailContainer);
        LinearLayout tabContainer = findViewById(R.id.tabContainer);
        ProgressBar loadingProjectDetail = findViewById(R.id.loadingProjectDetail);
        handleTabClick(tabContainer, container, loadingProjectDetail);
    }

    private void handleTabClick(LinearLayout tabContainer, ScrollView container, ProgressBar loadingProjectDetail) {
        AtomicReference<TextView> currentTab = new AtomicReference<>(findViewById(R.id.taskTab));
        showTaskList(container, 0, loadingProjectDetail);
        for (int i = 0; i < tabContainer.getChildCount(); i++) {
            TextView child = (TextView) tabContainer.getChildAt(i);
            child.setOnClickListener(v -> {
                if (currentTab.get() == child) return;
                child.setTextColor(Color.parseColor("#FFFFFF"));
                child.setBackground(ContextCompat.getDrawable(this, R.drawable.rounded_corner_24_blue));
                currentTab.get().setTextColor(Color.parseColor("#007AFF"));
                currentTab.get().setBackground(ContextCompat.getDrawable(this, R.drawable.rounded_corner_24_bw));
                currentTab.set(child);
                if (child.getId() == R.id.taskTab) {
                    showTaskList(container, 0, loadingProjectDetail);
                } else if (child.getId() == R.id.processTaskTab) {
                    showTaskList(container, 1, loadingProjectDetail);
                } else if (child.getId() == R.id.completedTaskTab) {
                    showTaskList(container, 2, loadingProjectDetail);
                } else if (child.getId() == R.id.informationTab) {
                    showInformationTab(container, loadingProjectDetail);
                }
            });
        }
    }

    private void showTaskList(ScrollView container, int status, ProgressBar loading) {
        loading.setVisibility(View.VISIBLE);
        container.removeAllViews();
        View task_list = LayoutInflater.from(this).inflate(R.layout.task_list, null);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        LinearLayout leftSide = task_list.findViewById(R.id.leftSide);
        LinearLayout rightSide = task_list.findViewById(R.id.linearLayout5);
        leftSide.getLayoutParams().width = (int) (getResources().getDisplayMetrics().widthPixels * 0.5 - 68);
        rightSide.getLayoutParams().width = (int) (getResources().getDisplayMetrics().widthPixels * 0.5 - 68);
        leftSide.removeAllViews();
        rightSide.removeAllViews();
        AtomicInteger count = new AtomicInteger(0);
        db.collection("tasks").whereEqualTo("projectId", projectId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().getDocuments().size() == 0) {
                    loading.setVisibility(View.GONE);
                    return;
                }
                for (int i = 0; i < task.getResult().getDocuments().size(); i++) {
                    if (task.getResult().getDocuments().get(i).getLong("status") != status && status != 0) {
                        continue;
                    }
                    count.getAndIncrement();
                    Map<String, Object> taskItem = task.getResult().getDocuments().get(i).getData();
                    View taskView = LayoutInflater.from(this).inflate(R.layout.task_card, null);
                    TextView taskName = taskView.findViewById(R.id.taskName);
                    taskName.setText(taskItem.get("name").toString());
                    String deadline = (String) taskItem.get("deadline");
                    TextView taskDeadline = taskView.findViewById(R.id.deadline);
                    taskDeadline.setText(deadline);
                    TextView taskStatus = taskView.findViewById(R.id.status);
                    if ((int) taskItem.get("status") == 1) {
                        taskStatus.setVisibility(View.GONE);
                    } else if ((int) taskItem.get("status") == 2) {
                        taskStatus.setVisibility(View.VISIBLE);
                        taskStatus.setText("On going");
                    } else {
                        taskStatus.setBackgroundResource(R.drawable.rounded_corner_24_blue);
                        taskStatus.setVisibility(View.VISIBLE);
                        taskStatus.setText("Done");
                        taskStatus.setTextColor(getResources().getColor(R.color.white));
                    }
                    taskView.setOnClickListener(v -> {
                        Intent intent = new Intent(this, TaskDetail.class);
                        startActivity(intent);
                    });
                    ImageView threeDot = taskView.findViewById(R.id.threeDot);
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
                    });
                    Space space = new Space(this);
                    space.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 20));
                    taskView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    if (count.get() % 2 == 0) {
                        leftSide.addView(taskView);
                        leftSide.addView(space);
                    } else {
                        rightSide.addView(taskView);
                        rightSide.addView(space);
                    }
                }
                container.addView(task_list);
                loading.setVisibility(View.GONE);
            }
        });
    }

    private void showInformationTab(ScrollView container, ProgressBar loading) {
        loading.setVisibility(View.VISIBLE);
        container.removeAllViews();
        View information = LayoutInflater.from(this).inflate(R.layout.project_detail_information, null);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("projects").document(projectId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Map<String, Object> project = task.getResult().getData();
                // display information of project
                TextView leader_tv = information.findViewById(R.id.leader_tv);
                db.collection("users").whereEqualTo("email", project.get("user_created")).get().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        leader_tv.setText(task1.getResult().getDocuments().get(0).get("name").toString());
                        LinearLayout member_list = information.findViewById(R.id.memberList2);
                        ArrayList<Map<String, Object>> members = (ArrayList<Map<String, Object>>) project.get("members");
                        for (Map<String, Object> member : members) {
                            if (members.indexOf(member) > 2) {
                                TextView member_tv = createItemMember((members.size() - 2) + " more...");
                                member_list.addView(member_tv);
                                break;
                            }
                            db.collection("users").where(equalTo("email", member.get("email"))).get().addOnCompleteListener(task2 -> {
                                if (task2.isSuccessful()) {
                                    TextView member_tv = createItemMember(task2.getResult().getDocuments().get(0).get("name").toString());
                                    member_list.addView(member_tv);
                                }
                            });
                        }
                        container.addView(information);
                        loading.setVisibility(View.GONE);
                    }
                });
            }
        });
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

    private void showPopupMenu(AtomicReference<Boolean> isDialogShowing) {
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
        Button deadlineBtn = view1.findViewById(R.id.deadlineBtn);
        TextView deadlineView = view1.findViewById(R.id.deadlineView);
        AtomicReference<Boolean> isDatePickerShowing = new AtomicReference<>(false);
        deadlineBtn.setOnClickListener(v1 -> {
            showDatePicker(isDatePickerShowing, deadlineView);
        });
        Button addCategoryBtn = view1.findViewById(R.id.addCategoryBtn);
        TextView categoryView = view1.findViewById(R.id.categoryView);
        addCategoryBtn.setOnClickListener(v1 -> {
            showCategoryDialog(categoryView);
        });
        Button addMemberBtn = view1.findViewById(R.id.addMemberBtn);
//        TextView memberList = view1.findViewById(R.id.memberList2);
        addMemberBtn.setOnClickListener(v1 -> {
            Intent intent = new Intent(this, AddMember.class);
            intent.putExtra("category", 3);
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("projects").document(projectId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Map<String, Object> project = task.getResult().getData();
                    ArrayList<Map<String, Object>> members = (ArrayList<Map<String, Object>>) project.get("members");
                    ArrayList<String> memberEmails = new ArrayList<>();
                    ArrayList<String> memberNames = new ArrayList<>();
                    MutableLiveData<Boolean> isFinished = new MutableLiveData<>(false);
                    for (Map<String, Object> member : members) {
                        memberEmails.add((String) member.get("email"));
                        db.collection("users").whereEqualTo("email", member.get("email")).get().addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                memberNames.add(task1.getResult().getDocuments().get(0).get("name").toString());
                                if (members.indexOf(member) == members.size() - 1) {
                                    isFinished.setValue(true);
                                }
                            }
                        });
                    }
                    isFinished.observe(this, aBoolean -> {
                        if (!aBoolean) {
                            return;
                        }
                        intent.putStringArrayListExtra("members", memberEmails);
                        intent.putStringArrayListExtra("name", memberNames);
                        startActivity(intent);
                    });
                }
            });
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

    private void showDatePicker(AtomicReference<Boolean> isDatePickerShowing, TextView deadlineView) {
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
        AtomicReference<Boolean> isTimePickerShow = new AtomicReference<>(false);
        CalendarView datePicker = view2.findViewById(R.id.datePicker);
        if (deadlineView.getVisibility() == View.VISIBLE) {
            String deadlineDate = deadlineView.getText().toString().split(" - ")[0];
            String deadlineTime = deadlineView.getText().toString().split(" - ")[1].replace(".", ":");
            if (deadlineDate.equals("Today")) {
                datePicker.setDate(new Date().getTime());
            } else if (deadlineDate.equals("Tomorrow")) {
                datePicker.setDate(new Date().getTime() + 86400000);
            } else {
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy", Locale.ENGLISH);
                try {
                    Date date = formatter.parse(deadlineDate);
                    datePicker.setDate(date.getTime());
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy - ", Locale.ENGLISH);
            String deadline = formatter.format(deadlineDate.getValue());
            deadline += timeBtn.getText().toString().replace(":", ".");
            deadlineView.setText(deadline);
            deadlineView.setVisibility(View.VISIBLE);
            bottomSheetDialog1.dismiss();
        });
        cancelBtn.setOnClickListener(v2 -> {
            bottomSheetDialog1.dismiss();
        });
    }
}
