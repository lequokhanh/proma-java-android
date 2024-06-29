package com.nt118.proma.ui.profile;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.nt118.proma.R;

public class ChangePassword extends AppCompatActivity {
    private ImageView imgBack;
    private Button btn_Reset;
    private EditText et_cr_pw, et_new_pw, et_cf_pw;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_password);

        InitUi();

        onClickBack();
        btn_Reset.setAlpha(0.5f);
        btn_Reset.setEnabled(false);

        et_cr_pw.addTextChangedListener(textWatcher);
        et_new_pw.addTextChangedListener(textWatcher);
        et_cf_pw.addTextChangedListener(textWatcher);

        btn_Reset.setOnClickListener(v -> changePassword());
    }

    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // Do nothing
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String currentPassword = et_cr_pw.getText().toString().trim();
            String newPassword = et_new_pw.getText().toString().trim();
            String confirmPassword = et_cf_pw.getText().toString().trim();

            if (!currentPassword.isEmpty() && !newPassword.isEmpty() && !confirmPassword.isEmpty()) {
                btn_Reset.setAlpha(1f);
                btn_Reset.setEnabled(true);
            } else {
                btn_Reset.setAlpha(0.5f);
                btn_Reset.setEnabled(false);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            // Do nothing
        }
    };

    public boolean validatePasswordChange(String currentPassword, String newPassword, String confirmPassword) throws Exception {
        if (newPassword.isEmpty() || confirmPassword.isEmpty() || currentPassword.isEmpty()) {
            throw new Exception("Fields cannot be empty.");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new Exception("New password and confirm password do not match.");
        }

        if (newPassword.equals(currentPassword)) {
            throw new Exception("New password should not be the same as the current password.");
        }

        return true;
    }

    private void changePassword() {
        String currentPassword = et_cr_pw.getText().toString().trim();
        String newPassword = et_new_pw.getText().toString().trim();
        String confirmPassword = et_cf_pw.getText().toString().trim();

        try {
            if (validatePasswordChange(currentPassword, newPassword, confirmPassword)) {
                user = FirebaseAuth.getInstance().getCurrentUser();
                String email = user.getEmail();

                AuthCredential credential = EmailAuthProvider.getCredential(email, currentPassword);
                user.reauthenticate(credential).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        user.updatePassword(newPassword).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                Toast.makeText(ChangePassword.this, "Update password successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ChangePassword.this, "Update password failed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(ChangePassword.this, "Not match current password", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (Exception e) {
            Toast.makeText(ChangePassword.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void onClickBack() {
        imgBack.setOnClickListener(v -> finish());
    }

    private void InitUi(){
        imgBack=findViewById(R.id.img_Back);
        btn_Reset=findViewById(R.id.btn_reset_pw);
        et_cr_pw=findViewById(R.id.et_cr_pw);
        et_new_pw=findViewById(R.id.et_new_pw);
        et_cf_pw=findViewById(R.id.et_cf_pw);
    }

}