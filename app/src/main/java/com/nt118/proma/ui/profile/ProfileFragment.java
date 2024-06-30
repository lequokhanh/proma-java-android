package com.nt118.proma.ui.profile;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nt118.proma.R;
import com.nt118.proma.databinding.FragmentProfileBinding;
import com.nt118.proma.model.ImageArray;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private FirebaseUser user;
    private String email;
    private FirebaseFirestore db;
    private int numProject,numsTask,numsDoneTask;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        db=FirebaseFirestore.getInstance();
        ProfileViewModel profileViewModel =
                new ViewModelProvider(this).get(ProfileViewModel.class);
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        TextView logout = binding.logout;
        AtomicReference<Boolean> isDialogShowing = new AtomicReference<>(false);
        TextView username = binding.username;
        CircleImageView avatar = binding.avatar3;
        TextView numPro = binding.numProject;
        TextView numTask = binding.numTask;
        TextView numDoneTask = binding.numDoneTask;


        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("user", MODE_PRIVATE);
        String name = sharedPreferences.getString("name", "");
        email = sharedPreferences.getString("email", "");
        avatar.setImageResource(new ImageArray().getAvatarImage().get(sharedPreferences.getInt("avatar", 0)));
        username.setText(name);
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
            Button singoutBtn = view1.findViewById(R.id.signoutBtn);
            singoutBtn.setOnClickListener(v1 -> {
                remove_fcm_token();
                FirebaseAuth.getInstance().signOut();
                // logout from facebook
                FacebookSdk.sdkInitialize(requireContext());
                LoginManager.getInstance().logOut();
                // logout from google
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();
                GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso);
                mGoogleSignInClient.signOut();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear().apply();
                bottomSheetDialog.dismiss();
                Intent intent = new Intent(requireContext(), com.nt118.proma.ui.login.Login.class);
                startActivity(intent);
            });
            Button cancelBtn = view1.findViewById(R.id.cancelBtn);
            cancelBtn.setOnClickListener(v1 -> {
                bottomSheetDialog.dismiss();
            });
        });
        TextView myProfileBtn = binding.myProfileBtn;
        myProfileBtn.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), MyProfile.class);
            startActivity(intent);
        });
        TextView notification = binding.notification;
        notification.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("android.provider.extra.APP_PACKAGE", requireContext().getPackageName());
            startActivity(intent);
        });
        TextView sercurity = binding.security;
        sercurity.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), Security.class);
            startActivity(intent);
        });
        sercurity.setVisibility(View.GONE);
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            for (UserInfo profile : user.getProviderData()) {
                String providerId = profile.getProviderId();
                if (providerId.equals("password")) {
                    sercurity.setVisibility(View.VISIBLE);
                }
            }
        }
        numProject = 0;
        numsTask = 0;
        numsDoneTask = 0;
        //count project
        Map<String, Object> member = new HashMap<>();
        member.put("email", email);
        member.put("isAccepted", true);

        db.collection("projects")
                .where(Filter.or(Filter.equalTo("user_created", email), Filter.arrayContains("members", member)))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        numProject = task.getResult().getDocuments().size();
                        numPro.setText(String.valueOf(numProject));
                    }
                });
        //count task of this user
        Map<String, Object> memberTask = new HashMap<>();
        memberTask.put("email", email);
        memberTask.put("isLeader", false);
        Map<String, Object> memberLeader = new HashMap<>();
        memberLeader.put("email", email);
        memberLeader.put("isLeader", true);
        db.collection("tasks")
                .where(Filter.or(Filter.arrayContains("members", memberLeader),
                        Filter.arrayContains("members", memberTask)))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        numsTask = task.getResult().getDocuments().size();
                        numTask.setText(String.valueOf(numsTask));
                        for (int j = 0; j < numsTask; j++) {
                            if (task.getResult().getDocuments().get(j).getLong("status") == 2) {
                                numsDoneTask++;
                            }
                        }
                        numDoneTask.setText(String.valueOf(numsDoneTask));
                    }
                });
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        TextView username = binding.username;
        CircleImageView avatar = binding.avatar3;
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("user", MODE_PRIVATE);
        String name = sharedPreferences.getString("name", "");
        avatar.setImageResource(new ImageArray().getAvatarImage().get(sharedPreferences.getInt("avatar", 0)));
        username.setText(name);
    }


    public void remove_fcm_token() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String email = mAuth.getCurrentUser().getProviderData().get(1).getEmail();
        Log.d("rft", "remove_fcm_token: " + email);
        db.collection("users").whereEqualTo("email", email).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                task.getResult().getDocuments().get(0).getReference().update("fcm_token", null);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}