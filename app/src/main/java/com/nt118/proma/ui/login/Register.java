package com.nt118.proma.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.nt118.proma.MainActivity;
import com.nt118.proma.R;

public class Register extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnSignUp;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        initUi();
        initListener();

        mAuth = FirebaseAuth.getInstance();
    }

    private void initUi(){
        etEmail = findViewById(R.id.etEmail);
        etPassword =findViewById(R.id.etPassword);
        btnSignUp = findViewById(R.id.sign_up_button);
    }
    private void initListener() {
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickSignUp();
            }
        });
    }

    private void onClickSignUp() {
        // validate email and password
        String email_text = etEmail.getText().toString();
        String password_text = etPassword.getText().toString();
        if(email_text.isEmpty()){
            etEmail.setError("Email is required");
            return;
        }
        if(password_text.isEmpty()){
            etPassword.setError("Password is required");
            return;
        }
        mAuth.createUserWithEmailAndPassword(email_text, password_text)
                .addOnCompleteListener(Register.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Intent intent = new Intent(Register.this, CompleteProfile.class);
                            startActivity(intent);
                            finishAffinity();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(Register.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

}
