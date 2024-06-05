package com.nt118.proma.ui.member;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.nt118.proma.R;

public class ViewOneMember extends AppCompatActivity {

    TextView tvEmail, tvPhone;
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

    }
    private void initUi(){
        tvEmail=findViewById(R.id.tvEmail);
        tvPhone=findViewById(R.id.tvPhoneNumber);
    }
}