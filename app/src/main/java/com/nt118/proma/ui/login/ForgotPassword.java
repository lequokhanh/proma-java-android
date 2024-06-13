package com.nt118.proma.ui.login;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nt118.proma.R;

public class ForgotPassword extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        EditText etEmail = findViewById(R.id.etEmail);
        Button btn_send_request = findViewById(R.id.btn_send_request);
        btn_send_request.setOnClickListener(v -> {
            btn_send_request.setEnabled(false);
            String email = etEmail.getText().toString();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").whereEqualTo("email", email).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (task.getResult().getDocuments().size() > 0) {
                        FirebaseAuth auth = FirebaseAuth.getInstance();
                        auth.sendPasswordResetEmail(email).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                btn_send_request.setEnabled(true);
                                Dialog dialog = new Dialog(this);
                                dialog.setContentView(R.layout.popup_success);
                                Button btnOk = dialog.findViewById(R.id.back_to_sign_in);
                                btnOk.setOnClickListener(v1 -> {
                                    dialog.dismiss();
                                    finish();
                                });
                                dialog.show();
                            }
                        });
                    } else {
                        etEmail.setError("Email not found");
                        btn_send_request.setEnabled(true);
                    }
                } else {
                    etEmail.setError("Email not found");
                    btn_send_request.setEnabled(true);
                }
            });
        });
        TextView tv_back= findViewById(R.id.back);
        tv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}