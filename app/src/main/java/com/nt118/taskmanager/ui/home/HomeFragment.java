package com.nt118.taskmanager.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.nt118.taskmanager.R;
import com.nt118.taskmanager.databinding.FragmentHomeBinding;
import com.nt118.taskmanager.ui.ProjectDetail.ProjectDetail;

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
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}