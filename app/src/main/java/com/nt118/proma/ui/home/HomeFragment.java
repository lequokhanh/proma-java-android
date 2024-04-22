package com.nt118.proma.ui.home;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Space;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nt118.proma.R;
import com.nt118.proma.databinding.FragmentHomeBinding;
import com.nt118.proma.model.Task;
import com.nt118.proma.ui.project.ProjectDetail;
import com.nt118.proma.ui.search.SearchView;
import com.nt118.proma.ui.task.AllTask;
import com.nt118.proma.ui.task.TaskDetail;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        CardView cardView = root.findViewById(R.id.card_view);
        cardView.findViewById(R.id.card_view).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ProjectDetail.class);
            startActivity(intent);
        });
        TextView projectName = cardView.findViewById(R.id.projectName);
        projectName.setText("Project Name");
        TextView projectDescription = cardView.findViewById(R.id.projectDescription);
        projectDescription.setText("Project Description");

        TextView seeAllButton = root.findViewById(R.id.informationTab);
        seeAllButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AllTask.class);
            startActivity(intent);
        });
        LinearLayout leftSide = root.findViewById(R.id.linearLayout4);
        LinearLayout rightSide = root.findViewById(R.id.linearLayout5);
        LinearLayout taskLayout = root.findViewById(R.id.horizontalLayout1);
        // set width of left and right side to 50% of screen width - 14dp * 2(padding) - 10dp
        leftSide.getLayoutParams().width = (int) (getResources().getDisplayMetrics().widthPixels * 0.5 - 68);
        rightSide.getLayoutParams().width = (int) (getResources().getDisplayMetrics().widthPixels * 0.5 - 68);
        List<Task> tasks = new ArrayList<>();
        // Task(String name, Integer id, Date startDate, Date endDate, Integer status)
        tasks.add(new Task("Task 1", 1, Date.from(java.time.Instant.now()), 1));
        tasks.add(new Task("Task 2", 2, Date.from(Instant.parse("2024-04-20T10:00:00Z")), 2)); // "10 Oct 2021 - 10.00 AM"
        tasks.add(new Task("Task 3", 3, Date.from(java.time.Instant.now()), 3));
        tasks.add(new Task("Task 4", 4, Date.from(java.time.Instant.now()), 1));
        tasks.add(new Task("Task 5", 5, Date.from(java.time.Instant.now()), 2));
        tasks.add(new Task("Task 6", 6, Date.from(java.time.Instant.now()), 1));
        tasks.add(new Task("Task 7", 7, Date.from(Instant.parse("2021-10-10T10:00:00Z")), 3)); // "10 Oct 2021 - 10.00 AM"
        // clear all child view of left and right side
        leftSide.removeAllViews();
        rightSide.removeAllViews();
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            View taskView = inflater.inflate(R.layout.task_card, null);
            TextView taskName = taskView.findViewById(R.id.taskName);
            taskName.setText(task.getName());
            // convert Date to String with format "dd MMM yyyy - HH.MM PM" and if the duedate is today, show "Today - 13.00 PM", if the duedate is tomorrow, show "Tomorrow - 13.00 PM"
            String deadline = Optional.ofNullable(task.getDueDate()).map(date -> {
                if (date.getDate() == new Date().getDate()) {
                    return "Today - " + date.getHours() + "." + date.getMinutes() + " PM";
                } else if (date.getDate() == new Date().getDate() + 1) {
                    return "Tomorrow - " + date.getHours() + "." + date.getMinutes() + " PM";
                } else {
                    SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy - hh.mm a", Locale.ENGLISH);
                    return formatter.format(date);
                }
            }).orElse("");
            TextView taskDeadline = taskView.findViewById(R.id.deadline);
            taskDeadline.setText(deadline);
            TextView taskStatus = taskView.findViewById(R.id.status);
            if (task.getStatus() == 1) {
                taskStatus.setVisibility(View.GONE);
            } else if (task.getStatus() == 2) {
                taskStatus.setVisibility(View.VISIBLE);
                taskStatus.setText("On going");
            } else {
                taskStatus.setBackgroundResource(R.drawable.rounded_corner_24_blue);
                taskStatus.setVisibility(View.VISIBLE);
                taskStatus.setText("Done");
                taskStatus.setTextColor(getResources().getColor(R.color.white));
            }
            taskView.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), TaskDetail.class);
                startActivity(intent);
            });
            ImageView threeDot = taskView.findViewById(R.id.threeDot);
            threeDot.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(getContext(), v, 5);
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
        FloatingActionButton searchButton = root.findViewById(R.id.search_button);
        searchButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SearchView.class);
            startActivity(intent);
        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}