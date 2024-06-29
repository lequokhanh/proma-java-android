package com.nt118.proma.ui.member;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.nt118.proma.R;
import com.nt118.proma.model.ImageArray;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewOneMember extends AppCompatActivity {
    String email;
    TextView tvEmail, tvPhone, name, tvDOB;
    LinearLayout phoneBtn, emailBtn, smsBtn;
    CircleImageView imgAvatar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_one_member);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent2 = getIntent();
        email = intent2.getStringExtra("email");

        initUi();
        tvPhone.setOnClickListener(v -> {
            String phoneNumber = tvPhone.getText().toString().trim();

            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(intent);
        });

        tvEmail.setOnClickListener(v -> {
            String email = tvEmail.getText().toString().trim();

            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:" + email));
            startActivity(intent);
        });

        smsBtn.setOnClickListener(v -> {
            String phoneNumber = tvPhone.getText().toString().trim();

            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("smsto:" + phoneNumber));
            startActivity(intent);
        });

        phoneBtn.setOnClickListener(v -> {
            String phoneNumber = tvPhone.getText().toString().trim();

            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(intent);
        });

        emailBtn.setOnClickListener(v -> {
            String email = tvEmail.getText().toString().trim();

            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:" + email));
            startActivity(intent);
        });
    }
    private void initUi(){
        name = findViewById(R.id.name);
        tvDOB = findViewById(R.id.tvDOB);
        tvEmail=findViewById(R.id.tvEmail);
        tvPhone=findViewById(R.id.tvPhoneNumber);
        phoneBtn = findViewById(R.id.phoneBtn);
        emailBtn = findViewById(R.id.emailBtn);
        smsBtn = findViewById(R.id.smsBtn);
        imgAvatar = findViewById(R.id.avatar2);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").whereEqualTo("email", email).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                name.setText(task.getResult().getDocuments().get(0).get("name").toString());
                tvDOB.setText(task.getResult().getDocuments().get(0).get("dob").toString());
                tvEmail.setText(email);
                tvPhone.setText(task.getResult().getDocuments().get(0).get("phone_number").toString());
                if (task.getResult().getDocuments().get(0).get("avatar") != null) {
                    imgAvatar.setBackgroundResource(new ImageArray().getAvatarImage().get(Math.toIntExact((Long) task.getResult().getDocuments().get(0).get("avatar"))));
                }
            }
        });
    }
}