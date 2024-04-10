package com.nt118.taskmanager.ui.ProjectDetail;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.bottomsheet.BottomSheetDragHandleView;
import com.nt118.taskmanager.R;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ProjectDetail extends Activity {
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.project_detail);
        ImageView imageView = findViewById(R.id.imageView6);
        imageView.setOnClickListener(v -> {
            finish();
        });
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        CardView cardView = findViewById(R.id.cardView);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) cardView.getLayoutParams();
        BottomSheetDragHandleView dragHandle = findViewById(R.id.drag_handle);
        ConstraintLayout memberAvatars = findViewById(R.id.memberAvatars);
        ConstraintLayout informationLayout = findViewById(R.id.informationLayout);
        AtomicInteger yBegin = new AtomicInteger();
        AtomicBoolean flag = new AtomicBoolean(false);
        AtomicBoolean isExpanded = new AtomicBoolean(false);
        int marginBegin = params.topMargin;
        dragHandle.setOnTouchListener((v, event) -> {
            if (!flag.get()){
                yBegin.set((int) event.getRawY() -3);
                flag.set(true);
            }
            System.out.println(event.getRawY() + " "  + yBegin + " " + marginBegin);
            if (marginBegin + (int) event.getRawY() - yBegin.get() > marginBegin) {
                memberAvatars.setVisibility(View.VISIBLE);
                informationLayout.setVisibility(View.VISIBLE);
                return true;
            }
            if (event.getAction() == 1) {
                if ((int) event.getRawY() < yBegin.get()) {
                    params.topMargin = marginBegin - 530;
                    cardView.setLayoutParams(params);
                    return true;
                }
            }else {
                memberAvatars.setVisibility(View.GONE);
                informationLayout.setVisibility(View.GONE);
            }
            if (yBegin.get() - (int) event.getRawY() < 530) {
                params.topMargin = marginBegin + (int) event.getRawY() - yBegin.get();
                cardView.setLayoutParams(params);
            }
            return true;
        });
    }

    @Override
    public void onBackPressed() {
        finish();
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
