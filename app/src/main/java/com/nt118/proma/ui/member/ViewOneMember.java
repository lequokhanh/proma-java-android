package com.nt118.proma.ui.member;

import static com.nt118.proma.ui.task.TaskDetail.setWindowFlag;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

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
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);


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
                    imgAvatar.setImageResource(new ImageArray().getAvatarImage().get(Math.toIntExact((Long) task.getResult().getDocuments().get(0).get("avatar"))));
                }
            }
        });
    }
}