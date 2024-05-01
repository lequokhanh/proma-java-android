package com.nt118.proma.ui.login;

import static android.app.PendingIntent.getActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nt118.proma.MainActivity;
import com.nt118.proma.R;

import java.util.HashMap;
import java.util.Map;

public class CompleteProfile extends AppCompatActivity {


    private EditText etFullname, etPhoneNumber, etDOB;
    private Button btnSignUp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_complete_profile);

        InitUi();
        btnSignUp.setOnClickListener(v -> setUserInformation());
        TextView signOutBtn = findViewById(R.id.signOutBtn);
        signOutBtn.setOnClickListener(v -> {
            remove_fcm_token();
            FirebaseAuth.getInstance().signOut();
            // logout from facebook
            FacebookSdk.sdkInitialize(this);
            LoginManager.getInstance().logOut();
            // logout from google
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
            mGoogleSignInClient.signOut();

            Intent intent = new Intent(CompleteProfile.this, Login.class);
            startActivity(intent);
        });
    }



    private void InitUi() {
        etFullname=findViewById(R.id.etFullName);
        etPhoneNumber=findViewById(R.id.etPhoneNumber);
        etDOB=findViewById(R.id.etDate);
        btnSignUp=findViewById(R.id.btn_Sign_Up);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String email = user.getProviderData().get(1).getEmail();

        db.collection("users").whereEqualTo("email", email).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().getDocuments().size() == 0) {
                    return;
                }
                etFullname.setText(task.getResult().getDocuments().get(0).getString("name"));
                etPhoneNumber.setText(task.getResult().getDocuments().get(0).getString("phone_number"));
                etDOB.setText(task.getResult().getDocuments().get(0).getString("dob"));
            }
        });
    }

    private void setUserInformation() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String email = user.getProviderData().get(1).getEmail();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (etFullname.getText().toString().isEmpty()) {
            etFullname.setError("Fullname is required");
            return;
        }
        if (etPhoneNumber.getText().toString().isEmpty()) {
            etPhoneNumber.setError("Phone number is required");
            return;
        }
        if (etDOB.getText().toString().isEmpty()) {
            etDOB.setError("Date of birth is required");
            return;
        }
        db.collection("users").whereEqualTo("email", email).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().getDocuments().size() == 0) {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("email", email);
                    userMap.put("name", etFullname.getText().toString());
                    userMap.put("phone_number", etPhoneNumber.getText().toString());
                    userMap.put("dob", etDOB.getText().toString());
                    db.collection("users").add(userMap);
                    Intent intent = new Intent(CompleteProfile.this, MainActivity.class);
                    startActivity(intent);
                    return;
                }
                task.getResult().getDocuments().get(0).getReference().update("name", etFullname.getText().toString());
                task.getResult().getDocuments().get(0).getReference().update("phone_number", etPhoneNumber.getText().toString());
                task.getResult().getDocuments().get(0).getReference().update("dob", etDOB.getText().toString());
                Intent intent = new Intent(CompleteProfile.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    public void remove_fcm_token() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").whereEqualTo("email", user.getProviderData().get(1).getEmail()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                task.getResult().getDocuments().get(0).getReference().update("fcm_token", "");
            }
        });
    }

    @Override
    public void onBackPressed() {
        // do nothing
        super.onBackPressed();
        moveTaskToBack(true);
    }
}