package com.nt118.proma.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.nt118.proma.MainActivity;
import com.nt118.proma.R;

public class Login extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        Button login_button = findViewById(R.id.login_button);
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(Login.this, MainActivity.class);
            startActivity(intent);
        }
        EditText email = findViewById(R.id.etEmail);
        EditText password = findViewById(R.id.etPassword);
        login_button.setOnClickListener(v -> {
            // validate email and password
            String email_text = email.getText().toString();
            String password_text = password.getText().toString();
            if(email_text.isEmpty()){
                email.setError("Email is required");
                return;
            }
            if(password_text.isEmpty()){
                password.setError("Password is required");
                return;
            }
            mAuth.signInWithEmailAndPassword(email_text, password_text).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    Intent intent = new Intent(Login.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    email.setError("Invalid email or password");
                    password.setError("Invalid email or password");
                    Toast.makeText(Login.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                }
            });
        });
        // Google Sign In


    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        moveTaskToBack(true);
    }
}
