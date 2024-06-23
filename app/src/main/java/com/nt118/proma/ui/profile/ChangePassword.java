package com.nt118.proma.ui.profile;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.nt118.proma.R;

public class ChangePassword extends AppCompatActivity {
    ImageView imgBack;
    Button btn_Reset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_password);

        InitUi();

        onClickBack();

        btn_Reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getProviderId();
            }
        });
    }

    private void onClickBack() {
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void InitUi(){
        imgBack=findViewById(R.id.img_Back);
        btn_Reset=findViewById(R.id.btn_reset_pw);
    }

    private void getProviderId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            for (UserInfo profile : user.getProviderData()) {
                String providerId = profile.getProviderId();
                Log.d("ChangePassword", "Provider ID: " + providerId);

                if (providerId.equals("password")) {
                    Log.d("ChangePassword", "User signed in with email and password");
                } else if (providerId.equals("phone")) {
                    Log.d("ChangePassword", "User signed in with phone number");
                } else {
                    Log.d("ChangePassword", "User signed in with " + providerId);
                }
            }
        } else {
            Log.d("ChangePassword", "No user is signed in.");
        }
    }
}