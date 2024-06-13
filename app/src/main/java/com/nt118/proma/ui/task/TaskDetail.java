package com.nt118.proma.ui.task;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nt118.proma.R;
import com.nt118.proma.model.ImageArray;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class TaskDetail extends AppCompatActivity {
    private LinearLayout container;
    private String taskId, projectId, parentId;
    private FloatingActionButton create_btn;
    private ProgressBar loadingBar;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private Uri dataUpload;
    private static final int PICK_FILE_REQUEST = 1;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_detail);
        Intent intent = getIntent();
        taskId = intent.getStringExtra("taskId");
        projectId = intent.getStringExtra("projectId");
        parentId = intent.getStringExtra("parentId") == null ? "" : intent.getStringExtra("parentId");
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        InitUI();
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
        imageView.setOnClickListener(v -> {
            finish();
        });
        handleTabClick();
        db.collection("tasks").document(taskId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                title.setText(task.getResult().getString("title"));
                desc.setText(task.getResult().getString("description"));
                deadline.setText(task.getResult().getString("deadline"));
                db.collection("projects").document(projectId).get().addOnCompleteListener(task2 -> {
                    if (task.isSuccessful()) {
                        if (task2.getResult().get("cover") != null) {
                            cover.setImageResource(ImageArray.getCoverProjectImage().get(Math.toIntExact((Long) task2.getResult().get("cover"))));
                            loading.dismiss();
                        }
                    }
                });
            }
        });
        storage = FirebaseStorage.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
    }

    private void handleTabClick() {
        LinearLayout tabContainer = findViewById(R.id.tabContainer);
        showResultTab();
        AtomicReference<TextView> currentTab = new AtomicReference<>(findViewById(R.id.resultTab));
        for (int i = 0; i < tabContainer.getChildCount(); i++) {
            TextView child = (TextView) tabContainer.getChildAt(i);
            child.setOnClickListener(v -> {
                if (currentTab.get() == child) return;
                child.setTextColor(Color.parseColor("#FFFFFF"));
                child.setBackground(ContextCompat.getDrawable(this, R.drawable.rounded_corner_24_blue));
                currentTab.get().setTextColor(Color.parseColor("#007AFF"));
                currentTab.get().setBackground(ContextCompat.getDrawable(this,R.drawable.rounded_corner_24_bw));
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

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
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

    private void showTaskList() {
        create_btn.setVisibility(View.VISIBLE);
        loadingBar.setVisibility(View.VISIBLE);
        container.setVisibility(View.GONE);
        container.removeAllViews();
        ScrollView scrollView = new ScrollView(this);
        scrollView.setVerticalScrollBarEnabled(false);
        View task_list = LayoutInflater.from(this).inflate(R.layout.task_list, null);
        scrollView.addView(task_list);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        LinearLayout leftSide = task_list.findViewById(R.id.leftSide);
        LinearLayout rightSide = task_list.findViewById(R.id.rightSide);
        leftSide.getLayoutParams().width = (int) (getResources().getDisplayMetrics().widthPixels * 0.5 - 68);
        rightSide.getLayoutParams().width = (int) (getResources().getDisplayMetrics().widthPixels * 0.5 - 68);
        leftSide.removeAllViews();
        rightSide.removeAllViews();
        AtomicInteger count = new AtomicInteger(0);
        db.collection("tasks").whereEqualTo("projectId", projectId).get().addOnCompleteListener(task -> {
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

    private void showResultTab() {
        loadingBar.setVisibility(View.VISIBLE);
        container.setVisibility(View.GONE);
        create_btn.setVisibility(View.GONE);
        container.removeAllViews();
        View resultPane = LayoutInflater.from(this).inflate(R.layout.task_detail_result, null);
        Button btnAtach =   resultPane.findViewById(R.id.attachBtn);
        Button btnLink =   resultPane.findViewById(R.id.linkBtn);
        TextView linkTextView = resultPane.findViewById(R.id.link_tv);
        TextView fileTextView = resultPane.findViewById(R.id.attach_tv);

        btnAtach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAttachDialog();
            }
        });
        btnLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog;
                dialog = new Dialog(TaskDetail.this);
                dialog.setContentView(R.layout.popup_link);

                Button btnCancel = dialog.findViewById(R.id.btn_cancle);
                btnCancel.setOnClickListener(v1 -> {
                    // Đóng Dialog và kết thúc Activity
                    dialog.dismiss();
                });

                dialog.show();
                uploadFile();
            }
        });

        if (storageRef != null) {
            storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    // Tại đây bạn có thể sử dụng URI để hiển thị file
                    String fileUrl = uri.toString();
                    String fileName = storageRef.getName();
                    // Hiển thị URL trên TextView
                    fileTextView.setText(fileName);
                    fileTextView.setVisibility(View.VISIBLE); // Đặt TextView là hiển thị
                    fileTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(fileUrl));
                            startActivity(intent);
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Xử lý lỗi
                }
            });
        }
        container.addView(resultPane);
        loadingBar.setVisibility(View.GONE);
        container.setVisibility(View.VISIBLE);
    }
    private void showAttachDialog() {
        Dialog dialog = new Dialog(TaskDetail.this);
        dialog.setContentView(R.layout.popup_attachment);

        Button btnCancel = dialog.findViewById(R.id.btn_cancle);
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        Button btnUpload = dialog.findViewById(R.id.btn_upload);
        btnUpload.setOnClickListener(v -> {
            // Lưu dialog để hiển thị sau khi chọn file
            currentDialog = dialog;
            chooseFile();
        });

        dialog.show();
    }

    private Dialog currentDialog; // Biến để lưu dialog hiện tại

    private void chooseFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            dataUpload = data.getData();
            // Hiển thị file trong popup_attachment
            displaySelectedFile(dataUpload);
        }
    }
    private void displaySelectedFile(Uri fileUri) {
        if (currentDialog  != null && fileUri != null) {
            // Lấy LinearLayout từ dialog
            LinearLayout listItemAttached = currentDialog.findViewById(R.id.list_item_atached);

            // Tạo một TextView mới để hiển thị file đã chọn
            TextView fileTextView = new TextView(this);
            fileTextView.setText(getFileName(fileUri));
            fileTextView.setPadding(16, 16, 16, 16);
            fileTextView.setBackgroundResource(R.drawable.rounded_corner_12_bw);
            fileTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_document_small, 0, R.drawable.ic_close_circle_2, 0);
            fileTextView.setCompoundDrawablePadding(6);

            // Thêm TextView vào LinearLayout
            listItemAttached.addView(fileTextView);

            // Xử lý sự kiện khi nhấn nút xóa
            fileTextView.setOnClickListener(v -> listItemAttached.removeView(fileTextView));

            // Hiển thị dialog trở lại
            currentDialog.show();
        }
    }
    private void uploadFile() {
        if (dataUpload != null) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            StorageReference fileRef = storageRef.child("uploads/" + System.currentTimeMillis() + "_" + getFileName(dataUpload));

            fileRef.putFile(dataUpload)
                    .addOnSuccessListener(taskSnapshot -> {
                        Toast.makeText(TaskDetail.this, "File uploaded successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(TaskDetail.this, "Failed to upload file", Toast.LENGTH_SHORT).show();
                    });
        }
    }
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
        TextView status = informationPane.findViewById(R.id.status);
        TextView category = informationPane.findViewById(R.id.category);
        TextView deadline = informationPane.findViewById(R.id.deadline_tv);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("tasks").document(taskId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
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
                    String email = (String) member.get("email");
                    db.collection("users").whereEqualTo("email", email).get().addOnCompleteListener(task2 -> {
                        if (task2.isSuccessful()) {
                            Map<String, Object> user = task2.getResult().getDocuments().get(0).getData();
                            if ((boolean) member.get("isLeader"))
                                leader.setText((String) user.get("name"));
                            memberList.addView(createItemMember((String) user.get("name")));
                        }
                    });
                }
            }
        });
        container.addView(informationPane);
        loadingBar.setVisibility(View.GONE);
        container.setVisibility(View.VISIBLE);
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
        loading.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        loading.setContentView(R.layout.loading);
        loading.show();
        return loading;
    }

}
