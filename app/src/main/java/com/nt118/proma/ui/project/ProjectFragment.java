package com.nt118.proma.ui.project;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.nt118.proma.R;
import com.nt118.proma.databinding.FragmentProjectBinding;
import com.nt118.proma.ui.search.SearchView;

import java.util.concurrent.atomic.AtomicReference;

public class ProjectFragment extends Fragment {

    private FragmentProjectBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ProjectViewModel projectViewModel =
                new ViewModelProvider(this).get(ProjectViewModel.class);

        binding = FragmentProjectBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        TextView searchField = binding.searchField;
        searchField.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SearchView.class);
            startActivity(intent);
        });
        LinearLayout tabContainer = binding.tabContainer;
        AtomicReference<TextView> currentTab = new AtomicReference<>(binding.ProjectBtn);
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
                    binding.AllProject.setVisibility(View.GONE);
                    binding.AllTask.setVisibility(View.VISIBLE);
                } else {
                    binding.AllTask.setVisibility(View.GONE);
                    binding.AllProject.setVisibility(View.VISIBLE);
                }
            });
        }
        LinearLayout leftSide = root.findViewById(R.id.linearLayout4);
        LinearLayout rightSide = root.findViewById(R.id.linearLayout5);
        LinearLayout taskLayout = root.findViewById(R.id.horizontalLayout1);
        // set width of left and right side to 50% of screen width - 14dp * 2(padding) - 10dp
        leftSide.getLayoutParams().width = (int) (getResources().getDisplayMetrics().widthPixels * 0.5 - 68);
        rightSide.getLayoutParams().width = (int) (getResources().getDisplayMetrics().widthPixels * 0.5 - 68);
        return root;
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