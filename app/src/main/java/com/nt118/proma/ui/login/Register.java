package com.nt118.proma.ui.login;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.nt118.proma.R;

public class Register extends AppCompatActivity {

    private EditText etEmail, etPassword, etRePassword;
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
        etRePassword = findViewById(R.id.etRePassword);
        btnSignUp = findViewById(R.id.sign_up_button);
    }
    private void initListener() {
        btnSignUp.setOnClickListener(v -> onClickSignUp());
        etRePassword.setOnFocusChangeListener((v, hasFocus) -> {
            if(!hasFocus){
                String password = etPassword.getText().toString();
                String rePassword = etRePassword.getText().toString();
                if(!password.equals(rePassword)){
                    etRePassword.setError("Password not match");
                }
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
        if(!password_text.equals(etRePassword.getText().toString())){
            etRePassword.setError("Password not match");
            return;
        }
        mAuth.createUserWithEmailAndPassword(email_text, password_text)
                .addOnCompleteListener(Register.this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(Register.this, "Register success",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(Register.this, "Register failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}
