package com.nt118.proma.ui.project;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nt118.proma.R;
import com.nt118.proma.databinding.FragmentProjectBinding;
import com.nt118.proma.model.ImageArray;
import com.nt118.proma.ui.search.SearchView;
import com.nt118.proma.ui.task.TaskDetail;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ProjectFragment extends Fragment {

    private FragmentProjectBinding binding;
    private String email;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ProjectViewModel projectViewModel =
                new ViewModelProvider(this).get(ProjectViewModel.class);
        binding = FragmentProjectBinding.inflate(inflater, container, false);
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("user", MODE_PRIVATE);
        email = sharedPreferences.getString("email", "");
        View root = binding.getRoot();
        TextView searchField = binding.searchField;
        searchField.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SearchView.class);
            startActivity(intent);
        });
        LinearLayout tabContainer = binding.tabContainer;
        AtomicReference<TextView> currentTab = new AtomicReference<>(binding.ProjectBtn);
        ProgressBar loading = binding.loadingProject;
        ScrollView scrollview = binding.scrollview;
        showProjectList(scrollview, loading);
        for (int i = 0; i < tabContainer.getChildCount(); i++) {
            TextView child = (TextView) tabContainer.getChildAt(i);
            child.setOnClickListener(v -> {
                if (currentTab.get() == child) return;
                child.setTextColor(Color.parseColor("#FFFFFF"));
                child.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.rounded_corner_24_blue));
                currentTab.get().setTextColor(Color.parseColor("#007AFF"));
                currentTab.get().setBackground(ContextCompat.getDrawable(requireContext(),R.drawable.rounded_corner_24_bw));
                currentTab.set(child);
                if (child.getId() == R.id.TaskBtn) {
                    showTaskList(scrollview, 0, loading);
                } else if (child.getId() == R.id.OnGoingBtn) {
                    showTaskList(scrollview, 1, loading);
                } else if (child.getId() == R.id.CompletedBtn) {
                    showTaskList(scrollview, 2, loading);
                } else {
                    showProjectList(scrollview, loading);
                }
            });
        }
        SwipeRefreshLayout swipeRefresh = binding.swipeRefresh;
        swipeRefresh.setOnRefreshListener(() -> {
            if (currentTab.get().getId() == R.id.TaskBtn) {
                showTaskList(scrollview, 0, loading);
            } else if (currentTab.get().getId() == R.id.OnGoingBtn) {
                showTaskList(scrollview, 1, loading);
            } else if (currentTab.get().getId() == R.id.CompletedBtn) {
                showTaskList(scrollview, 2, loading);
            } else {
                showProjectList(scrollview, loading);
            }
            swipeRefresh.setRefreshing(false);
        });
        return root;
    }

    private void showProjectList(ScrollView container, ProgressBar loading) {
        loading.setVisibility(View.VISIBLE);
        container.removeAllViews();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        LinearLayout containerLayout = new LinearLayout(getContext());
        containerLayout.setOrientation(LinearLayout.VERTICAL);
        MutableLiveData<Boolean> isLoaded = new MutableLiveData<>(false);

        Map<String, Object> member = new HashMap<>();
        member.put("email", email);
        member.put("isAccepted", true);

        db.collection("projects")
                .where(Filter.or(Filter.equalTo("user_created", email), Filter.arrayContains("members", member)))
                .get()
                .addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().getDocuments().size() == 0) {
                    loading.setVisibility(View.GONE);
                    return;
                }
                for (int i = 0; i < task.getResult().getDocuments().size(); i++) {
                    Map<String, Object> projectItem = task.getResult().getDocuments().get(i).getData();
                    View projectView = LayoutInflater.from(getContext()).inflate(R.layout.project_card, null);
                    TextView projectName = projectView.findViewById(R.id.projectName);
                    TextView projectDescription = projectView.findViewById(R.id.projectDescription);
                    TextView tvDeadline = projectView.findViewById(R.id.deadline);
                    TextView progressProject = projectView.findViewById(R.id.progressProject);
                    ImageView cover_project = projectView.findViewById(R.id.cover_project);
                    projectName.setText(projectItem.get("name").toString());
                    projectDescription.setText(projectItem.get("description").toString());
                    tvDeadline.setText(projectItem.get("deadline").toString());
                    if (projectItem.get("cover") != null) {
                        cover_project.setImageResource(ImageArray.getCoverProjectImage().get(Math.toIntExact((Long) projectItem.get("cover"))));
                    }
                    Map<String, Object> memberTask = new HashMap<>();
                    memberTask.put("email", email);
                    memberTask.put("isLeader", false);
                    Map<String, Object> memberLeader = new HashMap<>();
                    memberLeader.put("email", email);
                    memberLeader.put("isLeader", true);
                    db.collection("tasks")
                            .whereEqualTo("projectId", task.getResult().getDocuments().get(i).getId())
                            .get()
                            .addOnCompleteListener(task2 -> {
                        if (task2.isSuccessful()) {
                            int totalTask = task2.getResult().getDocuments().size();
                            int doneTask = 0;
                            for (int j = 0; j < totalTask; j++) {
                                if (task2.getResult().getDocuments().get(j).getLong("status") == 3) {
                                    doneTask++;
                                }
                            }
                            progressProject.setText(doneTask + "/" + totalTask);
                        }
                    });
                    int finalI = i;
                    projectView.setOnClickListener(v -> {
                        Intent intent = new Intent(getContext(), ProjectDetail.class);
                        intent.putExtra("projectId", task.getResult().getDocuments().get(finalI).getId());
                        startActivity(intent);
                    });
                    projectView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    Space space = new Space(getContext());
                    float dip20 = 20f;
                    Resources r = getResources();
                    float px20 = TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            dip20,
                            r.getDisplayMetrics()
                    );
                    space.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) px20));
                    containerLayout.addView(projectView);
                    containerLayout.addView(space);
                    if (i == task.getResult().getDocuments().size() - 1) {
                        isLoaded.setValue(true);
                    }
                }
            }
            isLoaded.observe(getViewLifecycleOwner(), aBoolean -> {
                if (aBoolean) {
                    container.addView(containerLayout);
                    loading.setVisibility(View.GONE);
                }
            });
        });
    }

    private void showTaskList(ScrollView container, int status, ProgressBar loading) {
        loading.setVisibility(View.VISIBLE);
        container.removeAllViews();
        View task_list = LayoutInflater.from(getContext()).inflate(R.layout.task_list, null);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        LinearLayout leftSide = task_list.findViewById(R.id.leftSide);
        LinearLayout rightSide = task_list.findViewById(R.id.rightSide);
        leftSide.getLayoutParams().width = (int) (getResources().getDisplayMetrics().widthPixels * 0.5 - 68);
        rightSide.getLayoutParams().width = (int) (getResources().getDisplayMetrics().widthPixels * 0.5 - 68);
        leftSide.removeAllViews();
        rightSide.removeAllViews();
        AtomicInteger count = new AtomicInteger(0);

        Map<String, Object> member = new HashMap<>();
        member.put("email", email);
        member.put("isAccepted", true);

        db.collection("projects")
                .where(Filter.or(Filter.equalTo("user_created", email), Filter.arrayContains("members", member)))
                .get()
                .addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()) {
                if (task1.getResult().getDocuments().size() == 0) {
                    loading.setVisibility(View.GONE);
                    return;
                }
                for (int j = 0; j < task1.getResult().getDocuments().size(); j++) {
                    Map<String, Object> projectItem = task1.getResult().getDocuments().get(j).getData();
                    String projectId = task1.getResult().getDocuments().get(j).getId();
                    Map<String, Object> memberTask = new HashMap<>();
                    memberTask.put("email", email);
                    memberTask.put("isLeader", false);
                    Map<String, Object> memberLeader = new HashMap<>();
                    memberLeader.put("email", email);
                    memberLeader.put("isLeader", true);
                    db.collection("tasks")
                            .whereEqualTo("projectId", projectId)
                            .where(Filter.or(Filter.arrayContains("members", memberLeader), Filter.arrayContains("members", memberTask)))
                            .get()
                            .addOnCompleteListener(task -> {
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
                                View taskView = LayoutInflater.from(getContext()).inflate(R.layout.task_card, null);
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
                                    Intent intent = new Intent(getContext(), TaskDetail.class);
                                    intent.putExtra("taskId", task.getResult().getDocuments().get(finalI).getId());
                                    intent.putExtra("projectId", projectId);
                                    startActivity(intent);
                                });
                                Space space = new Space(getContext());
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
                        }
                        container.removeAllViews();
                        container.addView(task_list);
                        loading.setVisibility(View.GONE);
                    });
                }
            }
        });
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}