package com.nt118.proma.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.nt118.proma.R;
import com.nt118.proma.databinding.FragmentProfileBinding;

import java.util.concurrent.atomic.AtomicReference;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ProfileViewModel profileViewModel =
                new ViewModelProvider(this).get(ProfileViewModel.class);

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        TextView logout = binding.logout;
        AtomicReference<Boolean> isDialogShowing = new AtomicReference<>(false);
        logout.setOnClickListener(v -> {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
            View view1 = LayoutInflater.from(requireContext()).inflate(R.layout.modal_logout, null);
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
        });
        TextView myProfileBtn = binding.myProfileBtn;
        myProfileBtn.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), MyProfile.class);
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