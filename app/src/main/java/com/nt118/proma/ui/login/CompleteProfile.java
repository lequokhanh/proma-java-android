package com.nt118.proma.ui.login;

import static android.app.PendingIntent.getActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.nt118.proma.MainActivity;
import com.nt118.proma.R;

public class CompleteProfile extends AppCompatActivity {


    private EditText etFullname, etPhoneNumber, etDOB;
    private Button btnSignUp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_complete_profile);

        InitUi();
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUserInformation();
            }
        });

    }



    private void InitUi() {
        etFullname=findViewById(R.id.etFullName);
        etPhoneNumber=findViewById(R.id.etPhoneNumber);
        etDOB=findViewById(R.id.etDate);
        btnSignUp=findViewById(R.id.btn_Sign_Up);
    }

    private void setUserInformation() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String strFullName= etFullname.getText().toString().trim();

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(strFullName)
                .setPhotoUri(null)
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(CompleteProfile.this,"Sign up complete",Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(CompleteProfile.this, MainActivity.class);
                            startActivity(intent);
                            finishAffinity();
                        }
                    }
                });
    }
}