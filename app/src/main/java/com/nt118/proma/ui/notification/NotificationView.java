package com.nt118.proma.ui.notification;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.nt118.proma.R;

public class NotificationView extends AppCompatActivity {

    ImageView imgBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_notification);

        InitUi();

        onClickImageBack();

    }



    private void InitUi(){
        imgBack = findViewById(R.id.img_Back);
    }
    private void onClickImageBack() {
        imgBack.setOnClickListener(v -> finish());
    }
}