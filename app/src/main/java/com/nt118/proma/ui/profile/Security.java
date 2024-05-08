package com.nt118.proma.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.nt118.proma.R;

public class Security extends AppCompatActivity {

    TextView tvChangePw;
    ImageView imgBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_security);

        //anh xa id
        InitUi();

        //thay doi mat khau
        onClickChangePw();
        //back ve
        onClickBack();
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
        tvChangePw=findViewById(R.id.tv_chang_pw);
        imgBack=findViewById(R.id.img_Back);
    }

    private void onClickChangePw() {
        tvChangePw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Security.this, ChangePassword.class);
                startActivity(intent);
            }
        });
    }
}