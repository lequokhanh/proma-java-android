package com.nt118.proma.ui.profile;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

import com.nt118.proma.R;

public class NotificationSetting extends AppCompatActivity {
    @Override
    @SuppressLint({"UseSwitchCompatOrMaterialCode", "MissingInflatedId"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_notification);
        Switch muteAll = findViewById(R.id.muteAll);
        Switch assign = findViewById(R.id.assign);
        Switch comment = findViewById(R.id.comment);
        Switch review = findViewById(R.id.reviewSwitch);
        muteAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                assign.setChecked(false);
                comment.setChecked(false);
                review.setChecked(false);
            }
        });
        assign.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                muteAll.setChecked(false);
            }
        });
        comment.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                muteAll.setChecked(false);
            }
        });
        review.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                muteAll.setChecked(false);
            }
        });
    }
}
