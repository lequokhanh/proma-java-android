package com.nt118.proma.ui.task;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.nt118.proma.R;

import java.util.concurrent.atomic.AtomicReference;

public class TaskDetail extends AppCompatActivity {
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_detail);
        ImageView imageView = findViewById(R.id.img_Back);
        imageView.setOnClickListener(v -> {
            finish();
        });
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        AtomicReference<TextView> currentTab = new AtomicReference<>(findViewById(R.id.resultTab));
        LinearLayout tabContainer = findViewById(R.id.tabContainer);
        // get children elements
        for (int i = 0; i < tabContainer.getChildCount(); i++) {
            TextView child = (TextView) tabContainer.getChildAt(i);
            child.setOnClickListener(v -> {
                if (currentTab.get() == child) return;
                child.setTextColor(Color.parseColor("#FFFFFF"));
                child.setBackground(ContextCompat.getDrawable(this, R.drawable.rounded_corner_24_blue));
                currentTab.get().setTextColor(Color.parseColor("#007AFF"));
                currentTab.get().setBackground(ContextCompat.getDrawable(this,R.drawable.rounded_corner_24_bw));
                currentTab.set(child);
                if (child.getId() == R.id.resultTab) {
                    findViewById(R.id.informationContainer).setVisibility(View.GONE);
                    findViewById(R.id.resultContainer).setVisibility(View.VISIBLE);
                }
                if (child.getId() == R.id.informationTab) {
                    findViewById(R.id.resultContainer).setVisibility(View.GONE);
                    findViewById(R.id.informationContainer).setVisibility(View.VISIBLE);
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    public static void setWindowFlag(Activity activity, final int bits, boolean on) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }
}
