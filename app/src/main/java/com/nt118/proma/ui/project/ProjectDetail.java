package com.nt118.proma.ui.project;

import static com.nt118.proma.ui.task.TaskDetail.setWindowFlag;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nt118.proma.R;
import com.nt118.proma.model.ImageArray;
import com.nt118.proma.ui.image.SetImage;
import com.nt118.proma.ui.member.AddMember;
import com.nt118.proma.ui.member.ViewMembers;
import com.nt118.proma.ui.task.TaskDetail;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class ProjectDetail extends AppCompatActivity {
    private PopupMenu popup;
    private String projectId;
    private ArrayList<String> task_members = new ArrayList<>();
    private ArrayList<String> task_names = new ArrayList<>();
    private ArrayList<String> membersProject = new ArrayList<>();
    private ArrayList<String> memberNames = new ArrayList<>();
    private LinearLayout memberList;
    private String leaderEmail;
    private String leaderName;
    private TextView leader;
    private AtomicReference<TextView> currentTab;
    private ProgressBar loadingProjectDetail;
    private ScrollView container;
    private FloatingActionButton create_btn;
    private String email;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.project_detail);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        Intent intent = getIntent();
        SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
        projectId = intent.getStringExtra("projectId");
        email = sharedPreferences.getString("email", "");

        ImageView back = findViewById(R.id.img_Back);
        back.setOnClickListener(v -> {
            finish();
        });
        create_btn = findViewById(R.id.create_task_btn);
        AtomicReference<Boolean> isDialogShowing = new AtomicReference<>(false);
        create_btn.setOnClickListener(v -> {
            showPopupCreateTask(isDialogShowing);
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
                        Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                        Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                        setForceIcons.invoke(menuPopupHelper, true);
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_change_cover) {
                    Intent intent2 = new Intent(this, SetImage.class);
                    intent2.putExtra("type", "cover");
                    intent2.putIntegerArrayListExtra("images", ImageArray.getCoverProjectImage());
                    startActivityForResult(intent2, 1234);
                } else if (item.getItemId() == R.id.action_delete) {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("projects").document(projectId).delete();
                    finish();
                } else if (item.getItemId() == R.id.action_edit) {
                    showPopupEditProject(isDialogShowing);
                }
                return false;
            });
            popup.show();
        });
        container = findViewById(R.id.projectDetailContainer);
        LinearLayout tabContainer = findViewById(R.id.tabContainer);
        loadingProjectDetail = findViewById(R.id.loadingProjectDetail);
        InitUI();
        handleTabClick(tabContainer);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUI();
    }

    private void InitUI() {
        Dialog loading = createLoadingDialog();
        ImageView cover = findViewById(R.id.cover);
        TextView name = findViewById(R.id.nameProjectView);
        TextView description = findViewById(R.id.descProjectView);
        TextView deadline = findViewById(R.id.deadlineView);
        TextView progressProjectView = findViewById(R.id.progressProjectView);
        LinearProgressIndicator progressBar = findViewById(R.id.progressBar);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("projects").document(projectId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Map<String, Object> project = task.getResult().getData();
                if (task.getResult().get("cover") != null) {
                    cover.setImageResource(ImageArray.getCoverProjectImage().get(Math.toIntExact((Long) project.get("cover"))));
                }
                name.setText(project.get("name").toString());
                description.setText(project.get("description").toString());
                deadline.setText(project.get("deadline").toString());
                Map<String, Object> memberTask = new HashMap<>();
                memberTask.put("email", email);
                memberTask.put("isLeader", false);
                Map<String, Object> memberLeader = new HashMap<>();
                memberLeader.put("email", email);
                memberLeader.put("isLeader", true);
                db.collection("tasks")
                        .whereEqualTo("projectId", projectId)
                        .get()
                        .addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                int total = task1.getResult().getDocuments().size();
                                int completed = 0;
                                for (int i = 0; i < total; i++) {
                                    if (task1.getResult().getDocuments().get(i).getLong("status") == 2) {
                                        completed++;
                                    }
                                }
                                if (total == 0) {
                                    progressProjectView.setText("0/0");
                                    progressBar.setProgress(0);
                                    return;
                                }
                                progressProjectView.setText(completed + "/" + total);
                                progressBar.setProgress((int) ((float) completed / total));
                            }
                        });
                loading.dismiss();
            }
        });
    }

    private void handleTabClick(LinearLayout tabContainer) {
        currentTab = new AtomicReference<>(findViewById(R.id.taskTab));
        showTaskList(0, loadingProjectDetail);
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
                    showTaskList(0, loadingProjectDetail);
                } else if (child.getId() == R.id.processTaskTab) {
                    showTaskList(1, loadingProjectDetail);
                } else if (child.getId() == R.id.completedTaskTab) {
                    showTaskList(2, loadingProjectDetail);
                } else if (child.getId() == R.id.informationTab) {
                    showInformationTab(loadingProjectDetail);
                }
            });
        }
        SwipeRefreshLayout swipeRefresh = findViewById(R.id.swipeRefresh);
        swipeRefresh.setOnRefreshListener(() -> {
            loadUI();
            swipeRefresh.setRefreshing(false);
        });
    }

    private void loadUI() {
        InitUI();
        if (currentTab.get().getId() == R.id.taskTab) {
            showTaskList(0, loadingProjectDetail);
        } else if (currentTab.get().getId() == R.id.processTaskTab) {
            showTaskList(1, loadingProjectDetail);
        } else if (currentTab.get().getId() == R.id.completedTaskTab) {
            showTaskList(2, loadingProjectDetail);
        } else if (currentTab.get().getId() == R.id.informationTab) {
            showInformationTab(loadingProjectDetail);
        }
    }

    private void showTaskList(int status, ProgressBar loading) {
        create_btn.setVisibility(View.VISIBLE);
        loading.setVisibility(View.VISIBLE);
        container.removeAllViews();
        View task_list = LayoutInflater.from(this).inflate(R.layout.task_list, null);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        LinearLayout leftSide = task_list.findViewById(R.id.leftSide);
        LinearLayout rightSide = task_list.findViewById(R.id.rightSide);
        leftSide.getLayoutParams().width = (int) (getResources().getDisplayMetrics().widthPixels * 0.5 - 68);
        rightSide.getLayoutParams().width = (int) (getResources().getDisplayMetrics().widthPixels * 0.5 - 68);
        leftSide.removeAllViews();
        rightSide.removeAllViews();
        Map<String, Object> memberTask = new HashMap<>();
        memberTask.put("email", email);
        memberTask.put("isLeader", false);
        Map<String, Object> memberLeader = new HashMap<>();
        memberLeader.put("email", email);
        memberLeader.put("isLeader", true);
        db.collection("tasks")
                .where(Filter.and(Filter.equalTo("status", status),
                        Filter.and(Filter.equalTo("projectId", projectId),
                                Filter.or(Filter.arrayContains("members", memberLeader),
                                        Filter.arrayContains("members", memberTask)))))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        System.out.println("99999999999999" + task.getResult().getDocuments().size());
                        if (task.getResult().getDocuments().size() == 0) {
                            loading.setVisibility(View.GONE);
                            return;
                        }
                        for (int i = 0; i < task.getResult().getDocuments().size(); i++) {

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
                                startActivity(intent);
                            });
                            Space space = new Space(this);
                            space.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 20));
                            taskView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                            if (i % 2 == 1) {
                                leftSide.addView(taskView);
                                leftSide.addView(space);
                            } else {
                                rightSide.addView(taskView);
                                rightSide.addView(space);
                            }
                        }
                        container.removeAllViews();
                        container.addView(task_list);
                        loading.setVisibility(View.GONE);
                    }
                });
    }

    private void showInformationTab(ProgressBar loading) {
        create_btn.setVisibility(View.GONE);
        loading.setVisibility(View.VISIBLE);
        container.removeAllViews();
        View information = LayoutInflater.from(this).inflate(R.layout.project_detail_information, null);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("projects").document(projectId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Map<String, Object> project = task.getResult().getData();
                // display information of project
                TextView leader_tv = information.findViewById(R.id.leader_tv);
                db.collection("users")
                        .whereEqualTo("email", project.get("user_created"))
                        .get()
                        .addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                leader_tv.setText(task1.getResult().getDocuments().get(0).get("name").toString());
                                LinearLayout member_list = information.findViewById(R.id.memberList2);
                                TextView deadline_tv = information.findViewById(R.id.deadline_tv);
                                Button reviewbtn = information.findViewById(R.id.btn_review);
                                View viewReview = information.findViewById(R.id.view_review);
                                TextView beforeDlTV = information.findViewById(R.id.before_dl_tv);
                                deadline_tv.setText(project.get("deadline").toString());
                                ArrayList<Map<String, Object>> members = (ArrayList<Map<String, Object>>) project.get("members");
                                MutableLiveData<Integer> count = new MutableLiveData<>(0);
                                for (int i = 0; i < Math.min(2, members.size()); i++) {
                                    int finalI = i;
                                    db.collection("users").whereEqualTo("email", members.get(i).get("email")).get().addOnCompleteListener(task2 -> {
                                        if (task2.isSuccessful()) {
                                            TextView member = createItemMember(task2.getResult().getDocuments().get(0).get("name").toString());
                                            member_list.addView(member);
                                            if (finalI == Math.min(2, members.size()) - 1) {
                                                count.setValue(2);
                                            }
                                        }
                                    });
                                }
                                member_list.setOnClickListener(v -> {
                                    Intent intent = new Intent(ProjectDetail.this, ViewMembers.class);
                                    intent.putExtra("projectId", projectId);
                                    startActivity(intent);
                                });
                                count.observe(this, integer -> {
                                    if (integer == 2) {
                                        if (members.size() > 2) {
                                            TextView more = createItemMember((members.size() - 2) + " more...");
                                            member_list.addView(more);
                                        }
                                        loading.setVisibility(View.GONE);
                                        container.addView(information);
                                    }
                                });
                                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                if (project.get("review") != null) {
                                    Map<String, Object> review = (Map<String, Object>) project.get("review");
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
                                } else if (project.get("user_created").toString().equals(email)) {
                                    try {
                                        if (sdf.parse(project.get("deadline").toString()).before(new Date()) || sdf.parse(project.get("deadline").toString()).equals(new Date())) {
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
            }
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
            db.collection("projects").document(projectId).update("review", newReview);
            loadUI();
            bottomSheetDialog.dismiss();
        });

    }

    private void showPopupEditProject(AtomicReference<Boolean> isDialogShowing) {
        Dialog loading = createLoadingDialog();
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view1 = LayoutInflater.from(this).inflate(R.layout.modal_create_project, null);
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
        TextView title2TV = view1.findViewById(R.id.title2TV);
        title2TV.setVisibility(View.GONE);
        TextView titleTV = view1.findViewById(R.id.titleTV);
        titleTV.setText("Edit project");
        TextView nameProjectET = view1.findViewById(R.id.etNameProject);
        TextView descProjectET = view1.findViewById(R.id.etDescProject);
        TextView deadlineView = view1.findViewById(R.id.deadlineView);
        Button deadlineBtn = view1.findViewById(R.id.deadlineBtn);
        deadlineBtn.setOnClickListener(v1 -> {
            showDatePicker(new AtomicReference<>(false), deadlineView, Boolean.FALSE);
        });
        Button addMemberBtn = view1.findViewById(R.id.addMemberBtn);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Button createProjectBtn = view1.findViewById(R.id.createProjectBtn);
        createProjectBtn.setText("Save");
        membersProject = new ArrayList<>();
        memberNames = new ArrayList<>();
        memberList = view1.findViewById(R.id.memberList2);
        AtomicReference<ArrayList<Map<String, Object>>> members = new AtomicReference<>();
        db.collection("projects").document(projectId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Map<String, Object> project = task.getResult().getData();
                nameProjectET.setText(project.get("name").toString());
                descProjectET.setText(project.get("description").toString());
                deadlineView.setText(project.get("deadline").toString());
                deadlineView.setVisibility(View.VISIBLE);
                members.set((ArrayList<Map<String, Object>>) project.get("members"));
                MutableLiveData<Boolean> isCompleted = new MutableLiveData<>(false);
                for (int i = 0; i < Math.min(2, members.get().size()); i++) {
                    int finalI = i;
                    db.collection("users").whereEqualTo("email", members.get().get(i).get("email")).get().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            TextView member = createItemMember(task1.getResult().getDocuments().get(0).get("name").toString());
                            memberList.addView(member);
                            if (finalI == Math.min(2, members.get().size()) - 1) {
                                isCompleted.setValue(true);
                            }
                        }
                    });
                }
                isCompleted.observe(this, aBoolean -> {
                    if (aBoolean) {
                        if (members.get().size() > 2) {
                            TextView more = createItemMember((members.get().size() - 2) + " more...");
                            memberList.addView(more);
                        }
                    }
                });
                for (Map<String, Object> member : members.get()) {
                    db.collection("users").whereEqualTo("email", member.get("email")).get().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            membersProject.add((String) member.get("email"));
                            memberNames.add(task1.getResult().getDocuments().get(0).get("name").toString());
                        }
                        if (members.get().indexOf(member) == members.get().size() - 1) {
                            loading.dismiss();
                            bottomSheetDialog.show();
                        }
                    });
                }
            }
        });
        addMemberBtn.setOnClickListener(v1 -> {
            Intent intent = new Intent(this, AddMember.class);
            intent.putExtra("category", 1);
            intent.putStringArrayListExtra("members", membersProject);
            intent.putStringArrayListExtra("name", memberNames);
            startActivityForResult(intent, 5678);
        });
        createProjectBtn.setOnClickListener(v1 -> {
            if (nameProjectET.getText().toString().isEmpty()) {
                nameProjectET.setError("Name is required");
                return;
            }
            if (descProjectET.getText().toString().isEmpty()) {
                descProjectET.setError("Description is required");
                return;
            }
            if (deadlineView.getVisibility() == View.GONE) {
                deadlineView.setError("Deadline is required");
                return;
            }
            if (membersProject.size() == 0) {
                Toast.makeText(this, "Members are required", Toast.LENGTH_SHORT).show();
                return;
            }
            bottomSheetDialog.dismiss();
            Map<String, Object> newProject = new HashMap<>();
            newProject.put("name", nameProjectET.getText().toString());
            newProject.put("description", descProjectET.getText().toString());
            newProject.put("deadline", deadlineView.getText().toString());
            ArrayList<Map<String, Object>> membersList = new ArrayList<>();
            for (Map<String, Object> member : members.get()) {
                if (membersProject.contains(member.get("email"))) {
                    membersList.add(member);
                    membersProject.remove(member.get("email"));
                }
            }
            for (String member : membersProject) {
                Map<String, Object> memberMap = new HashMap<>();
                memberMap.put("email", member);
                memberMap.put("isAccepted", false);
                membersList.add(memberMap);
            }
            newProject.put("members", membersList);
            db.collection("projects").document(projectId).update(newProject);
            memberList = null;
            membersProject.clear();
            memberNames.clear();
            loadUI();
            bottomSheetDialog.dismiss();
        });
    }

    private void showPopupCreateTask(AtomicReference<Boolean> isDialogShowing) {
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

    private void handleCreateTaskBtn(String taskTitle, String desc, String leader, ArrayList<String> members, String deadline, String category) {
        Dialog loading = createLoadingDialog();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> newTask = new HashMap<>();
        newTask.put("title", taskTitle);
        newTask.put("description", desc);
        newTask.put("deadline", deadline);
        newTask.put("category", category);
        newTask.put("projectId", projectId);

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
                startActivity(intent);
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
                memberEmails.add((String) project.get("user_created"));
                ArrayList<String> names = memberNames.getValue();
                db.collection("users").whereEqualTo("email", project.get("user_created")).get().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
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
                    memberEmails.add((String) member.get("email"));
                    db.collection("users").whereEqualTo("email", member.get("email")).get().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            ArrayList<String> names = memberNames.getValue();
                            names.add(task1.getResult().getDocuments().get(0).get("name").toString());
                            memberNames.setValue(names);
                        }
                    });
                }
                memberEmails.add((String) project.get("user_created"));
                ArrayList<String> names = memberNames.getValue();
                db.collection("users").whereEqualTo("email", project.get("user_created")).get().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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

    public Dialog createLoadingDialog() {
        Dialog loading = new Dialog(this);
        loading.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        loading.setContentView(R.layout.loading);
        loading.show();
        return loading;
    }
}
