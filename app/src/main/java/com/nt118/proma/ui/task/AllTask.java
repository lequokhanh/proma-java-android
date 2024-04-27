package com.nt118.proma.ui.task;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import androidx.appcompat.app.AppCompatActivity;

import com.nt118.proma.R;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class AllTask extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_task);
        ImageView back = findViewById(R.id.back_button);
        back.setOnClickListener(v -> {
            finish();
        });
        ImageView menu_button = findViewById(R.id.menu_button);
        menu_button.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(AllTask.this, v, 5);
            popup.getMenuInflater().inflate(R.menu.all_task_menu, popup.getMenu());
            popup.getMenu().setGroupDividerEnabled(true);
            try {
                Field[] fields = popup.getClass().getDeclaredFields();
                for (Field field : fields) {
                    if ("mPopup".equals(field.getName())) {
                        field.setAccessible(true);
                        Object menuPopupHelper = field.get(popup);
                        Class<?> classPopupHelper = Class.forName(menuPopupHelper
                                .getClass().getName());
                        Method setForceIcons = classPopupHelper.getMethod(
                                "setForceShowIcon", boolean.class);
                        setForceIcons.invoke(menuPopupHelper, true);
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            popup.show();
        });
        LinearLayout leftSide = findViewById(R.id.leftSide);
        LinearLayout rightSide = findViewById(R.id.linearLayout5);
        LinearLayout taskLayout = findViewById(R.id.horizontalLayout1);
        // set width of left and right side to 50% of screen width - 14dp * 2(padding) - 10dp
        leftSide.getLayoutParams().width = (int) (getResources().getDisplayMetrics().widthPixels * 0.5 - 68);
        rightSide.getLayoutParams().width = (int) (getResources().getDisplayMetrics().widthPixels * 0.5 - 68);
    }

}
