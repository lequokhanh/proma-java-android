package com.nt118.proma.ui.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.nt118.proma.R;
import com.nt118.proma.databinding.FragmentHomeBinding;
import com.nt118.proma.model.ImageArray;
import com.nt118.proma.ui.login.Login;
import com.nt118.proma.ui.notification.NotificationView;
import com.nt118.proma.ui.project.ProjectDetail;
import com.nt118.proma.ui.search.SearchView;
import com.nt118.proma.ui.task.AllTask;
import com.nt118.proma.ui.task.TaskDetail;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeFragment extends Fragment {
    private PopupMenu popup;
    private FragmentHomeBinding binding;
    private ListenerRegistration projectListener;
    private ListenerRegistration taskListener;
    private String taskId;
    private View root;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        getActivity();
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user", Context.MODE_PRIVATE);
        FloatingActionButton searchButton = root.findViewById(R.id.search_button);
        searchButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SearchView.class);
            startActivity(intent);
        });
        TextView username = root.findViewById(R.id.username);
        CircleImageView avatar = root.findViewById(R.id.avatarView);
        username.setText(sharedPreferences.getString("name", ""));
        avatar.setImageResource(new ImageArray().getAvatarImage().get(sharedPreferences.getInt("avatar", -1)));
        loadUI();
        handleFiresrtoreChange();
        SwipeRefreshLayout swipeRefresh = binding.swipeRefresh;
        swipeRefresh.setOnRefreshListener(() -> {
            loadUI();
            swipeRefresh.setRefreshing(false);
        });
        return root;
    }

    @SuppressLint("ResourceType")
    public void loadUI() {
        LayoutInflater inflater = getLayoutInflater();
        root = binding.getRoot();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(getActivity(), Login.class);
            startActivity(intent);
        }

        //handle notification button
        onClickNotification();

        FloatingActionButton searchButton = root.findViewById(R.id.search_button);
        searchButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SearchView.class);
            startActivity(intent);
        });
        String email = user.getProviderData().get(1).getEmail();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        ProgressBar loadingHome = root.findViewById(R.id.loadingHome);
        ScrollView homeLayout = root.findViewById(R.id.homeLayout);
        homeLayout.removeAllViews();
        loadingHome.setVisibility(View.VISIBLE);
        //member : {email, isAccepted}
        Map<String, Object> member = new HashMap<>();
        member.put("email", email);
        member.put("isAccepted", true);

        db.collection("projects")
                .where(
                        Filter.or(
                                Filter.equalTo("user_created", email),
                                Filter.arrayContains("members", member)))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().getDocuments().isEmpty()) {
                            View homeContainer = inflater.inflate(R.layout.home_container, null);
                            CardView cardView = homeContainer.findViewById(R.id.card_view);

                            TextView projectName = cardView.findViewById(R.id.projectName);
                            TextView projectDescription = cardView.findViewById(R.id.projectDescription);
                            TextView tvDeadline = cardView.findViewById(R.id.deadline);
                            TextView progressProject = cardView.findViewById(R.id.progressProject);
                            ImageView cover_project = cardView.findViewById(R.id.cover_project);
                            // find a project in database owned by user or user is a member of that project and the deadline is the nearest today
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy", Locale.US);
                            Date today = new Date();
                            int index = 0;
                            for (int i = 1; i < task.getResult().getDocuments().size(); i++) {
                                try {
                                    Date deadline = sdf.parse(task.getResult().getDocuments().get(i).getString("deadline"));
                                    if (deadline.after(today) && deadline.before(sdf.parse(task.getResult().getDocuments().get(index).getString("deadline")))) {
                                        index = i;
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                            try {
                                if (index == 0 && sdf.parse(task.getResult().getDocuments().get(index).getString("deadline")).before(today)) {
                                    for (int i = 1; i < task.getResult().getDocuments().size(); i++) {
                                        try {
                                            Date deadline = sdf.parse(task.getResult().getDocuments().get(i).getString("deadline"));
                                            if (deadline.before(today) && deadline.after(sdf.parse(task.getResult().getDocuments().get(index).getString("deadline"))))
                                                index = i;
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            } catch (ParseException e) {
                                throw new RuntimeException(e);
                            }
                            String name = task.getResult().getDocuments().get(index).getString("name");
                            String description = task.getResult().getDocuments().get(index).getString("description");
                            String deadlineStr = task.getResult().getDocuments().get(index).getString("deadline");
                            projectName.setText(name);
                            projectDescription.setText(description);
                            tvDeadline.setText(deadlineStr);
                            if (task.getResult().getDocuments().get(index).get("cover") != null) {
                                cover_project.setImageResource(ImageArray.getCoverProjectImage().get(task.getResult().getDocuments().get(index).getLong("cover").intValue()));
                            }
                            AtomicReference<String> projectId = new AtomicReference<>();
                            projectId.set(task.getResult().getDocuments().get(index).getId());
                            cardView.findViewById(R.id.card_view).setOnClickListener(v -> {
                                Intent intent = new Intent(getActivity(), ProjectDetail.class);
                                intent.putExtra("projectId", projectId.get());

                                startActivity(intent);
                            });
                            Map<String, Object> memberTask = new HashMap<>();
                            memberTask.put("email", email);
                            memberTask.put("isLeader", false);
                            Map<String, Object> memberLeader = new HashMap<>();
                            memberLeader.put("email", email);
                            memberLeader.put("isLeader", true);
                            db.collection("tasks").whereEqualTo("projectId", projectId.get()).get().addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    int totalTask = task1.getResult().getDocuments().size();
                                    int doneTask = 0;
                                    for (int i = 0; i < task1.getResult().getDocuments().size(); i++) {
                                        if ((Long) task1.getResult().getDocuments().get(i).get("status") == 2) {
                                            doneTask++;
                                        }
                                    }
                                    progressProject.setText(doneTask + "/" + totalTask);
                                }
                            });
                            db.collection("tasks")
                                    .where(Filter.or(Filter.arrayContains("members", memberLeader),
                                            Filter.arrayContains("members", memberTask)))
                                    .get()
                                    .addOnCompleteListener(task2 -> {
                                        if (task2.isSuccessful()) {
                                            TextView seeAllButton = homeContainer.findViewById(R.id.informationTab);
                                            seeAllButton.setOnClickListener(v -> {
                                                Intent intent = new Intent(getActivity(), AllTask.class);
                                                intent.putExtra("email", email);
                                                intent.putExtra("projectId", projectId.get());
                                                startActivity(intent);
                                            });
                                            LinearLayout leftSide = homeContainer.findViewById(R.id.leftSide);
                                            LinearLayout rightSide = homeContainer.findViewById(R.id.rightSide);
                                            // set width of left and right side to 50% of screen width - 14dp * 2(padding) - 10dp
                                            leftSide.getLayoutParams().width = (int) (getResources().getDisplayMetrics().widthPixels * 0.5 - 68);
                                            rightSide.getLayoutParams().width = (int) (getResources().getDisplayMetrics().widthPixels * 0.5 - 68);
                                            List<Map<String, Object>> tasks = new ArrayList<>();
                                            for (int i = 0; i < task2.getResult().getDocuments().size(); i++) {
                                                Map<String, Object> taskItem = task2.getResult().getDocuments().get(i).getData();
                                                tasks.add(taskItem);
                                            }
                                            leftSide.removeAllViews();
                                            rightSide.removeAllViews();
                                            for (int i = 0; i < tasks.size(); i++) {
                                                Map<String, Object> taskItem = tasks.get(i);
                                                View taskView = inflater.inflate(R.layout.task_card, null);
                                                ImageView taskIcon = taskView.findViewById(R.id.icon);
                                                taskIcon.setImageResource(ImageArray.getIconTaskCard().get(taskItem.get("category").toString()));
                                                TextView taskName = taskView.findViewById(R.id.taskName);
                                                taskName.setText(taskItem.get("title").toString());
                                                String deadline = (String) taskItem.get("deadline");
                                                TextView taskDeadline = taskView.findViewById(R.id.deadline);
                                                taskDeadline.setText(deadline);
                                                TextView taskStatus = taskView.findViewById(R.id.status);
                                                // Check if the current user is a leader

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
                                                    Intent intent = new Intent(getActivity(), TaskDetail.class);
                                                    intent.putExtra("taskId", task2.getResult().getDocuments().get(finalI).getId());
                                                    intent.putExtra("projectId", projectId.get());
                                                    startActivity(intent);
                                                });
                                                Space space = new Space(getContext());
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
                                            homeLayout.removeAllViews();
                                            homeLayout.addView(homeContainer);
                                            loadingHome.setVisibility(View.GONE);
                                        }
                                    });
                        } else {
                            View noProject = inflater.inflate(R.layout.no_data, null);
                            noProject.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                            homeLayout.removeAllViews();
                            homeLayout.addView(noProject);
                            loadingHome.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void onClickNotification() {
        root = binding.getRoot();
        FloatingActionButton notificationButton = root.findViewById(R.id.notification_button);
        notificationButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), NotificationView.class);
            startActivity(intent);
        });
    }

    public void handleFiresrtoreChange() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(getActivity(), com.nt118.proma.ui.login.Login.class);
            startActivity(intent);
        }
        String email = user.getProviderData().get(1).getEmail();
        // listen to changes in projects collection
        projectListener = db.collection("projects")
                .where(Filter.or(Filter.equalTo("user_created", email), Filter.arrayContains("members", email)))
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return;
                    }
                    if (value != null) {
                        loadUI();
                    }
                });
        db.collection("projects")
                .where(Filter.or(Filter.equalTo("user_created", email), Filter.arrayContains("members", email))).get()
                .addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        if (!task1.getResult().getDocuments().isEmpty()) {
                            String projectId = task1.getResult().getDocuments().get(0).getId();
                            taskListener = db.collection("tasks").whereEqualTo("project_id", projectId).addSnapshotListener((value, error) -> {
                                if (error != null) {
                                    return;
                                }
                                if (value != null) {
                                    loadUI();
                                }
                            });
                        }
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUI();
    }

    // remove addSnapshotListener when fragment is destroyed
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        if (projectListener != null) {
            projectListener.remove();
            projectListener = null;
        }
        if (taskListener != null) {
            taskListener.remove();
            taskListener = null;
        }
    }

}