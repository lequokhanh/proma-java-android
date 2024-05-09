package com.nt118.proma.ui.project;

import static com.nt118.proma.ui.task.TaskDetail.setWindowFlag;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nt118.proma.R;
import com.nt118.proma.ui.image.SetImage;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class ProjectDetail extends AppCompatActivity {
    private PopupMenu popup;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.project_detail);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        ImageView back = findViewById(R.id.img_Back);
        back.setOnClickListener(v -> {
            finish();
        });
        FloatingActionButton create_btn = findViewById(R.id.create_task_btn);
        AtomicReference<Boolean> isDialogShowing = new AtomicReference<>(false);
        create_btn.setOnClickListener(v -> {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
            View view1 = LayoutInflater.from(this).inflate(R.layout.modal_create_task, null);
            bottomSheetDialog.setContentView(view1);
            if (isDialogShowing.get()) {
                return;
            }
            isDialogShowing.set(true);
            bottomSheetDialog.show();
            bottomSheetDialog.setOnDismissListener(dialog -> {
                isDialogShowing.set(false);
            });
            bottomSheetDialog.setOnCancelListener(dialog -> {
                isDialogShowing.set(false);
            });
            Button deadlineBtn = view1.findViewById(R.id.deadlineBtn);
            TextView deadlineView = view1.findViewById(R.id.deadlineView);
            AtomicReference<Boolean> isDatePickerShowing = new AtomicReference<>(false);
            deadlineBtn.setOnClickListener(v1 -> {
                BottomSheetDialog bottomSheetDialog1 = new BottomSheetDialog(this);
                View view2 = LayoutInflater.from(this).inflate(R.layout.modal_date_picker, null);
                bottomSheetDialog1.setContentView(view2);
                if (isDatePickerShowing.get()) {
                    return;
                }
                isDatePickerShowing.set(true);
                bottomSheetDialog1.show();
                bottomSheetDialog1.setOnDismissListener(dialog -> {
                    isDatePickerShowing.set(false);
                });
                bottomSheetDialog1.setOnCancelListener(dialog -> {
                    isDatePickerShowing.set(false);
                });
                TextView timeBtn = view2.findViewById(R.id.timeBtn);
                AtomicReference<Boolean> isTimePickerShow = new AtomicReference<>(false);
                CalendarView datePicker = view2.findViewById(R.id.datePicker);
                if (deadlineView.getVisibility() == View.VISIBLE) {
                    String deadlineDate = deadlineView.getText().toString().split(" - ")[0];
                    String deadlineTime = deadlineView.getText().toString().split(" - ")[1].replace(".", ":");
                    if (deadlineDate.equals("Today")) {
                        datePicker.setDate(new Date().getTime());
                    } else if (deadlineDate.equals("Tomorrow")) {
                        datePicker.setDate(new Date().getTime() + 86400000);
                    } else {
                        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy", Locale.ENGLISH);
                        try {
                            Date date = formatter.parse(deadlineDate);
                            datePicker.setDate(date.getTime());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    timeBtn.setText(deadlineTime);
                }
                timeBtn.setOnClickListener(v2 -> {
                    if (isTimePickerShow.get()) {
                        return;
                    }
                    isTimePickerShow.set(true);
                    BottomSheetDialog bottomSheetDialog2 = new BottomSheetDialog(this);
                    View view3 = LayoutInflater.from(this).inflate(R.layout.modal_time_picker, null);
                    bottomSheetDialog2.setContentView(view3);
                    NumberPicker hourPicker = view3.findViewById(R.id.hourPicker);
                    NumberPicker minutePicker = view3.findViewById(R.id.monthPicker);
                    NumberPicker amPmPicker = view3.findViewById(R.id.yearPicker);
                    hourPicker.setMinValue(1);
                    hourPicker.setMaxValue(12);
                    hourPicker.setFormatter(value -> String.format(new Locale("en"), "%02d", value));
                    minutePicker.setMinValue(0);
                    minutePicker.setMaxValue(59);
                    minutePicker.setFormatter(value -> String.format(new Locale("en"), "%02d", value));
                    ArrayList<String> amPm = new ArrayList<>();
                    amPm.add("AM");
                    amPm.add("PM");
                    amPmPicker.setDisplayedValues(amPm.toArray(new String[0]));
                    amPmPicker.setMinValue(0);
                    amPmPicker.setMaxValue(1);
                    amPmPicker.setValue(timeBtn.getText().toString().contains("AM") ? 0 : 1);
                    hourPicker.setValue(timeBtn.getText().toString().contains("AM") ? Integer.parseInt(timeBtn.getText().toString().split(":")[0]) : Integer.parseInt(timeBtn.getText().toString().split(":")[0]) + 12);
                    minutePicker.setValue(Integer.parseInt(timeBtn.getText().toString().split(":")[1].split(" ")[0]));
                    bottomSheetDialog2.show();
                    bottomSheetDialog2.setOnDismissListener(v3 -> {
                        isTimePickerShow.set(false);
                    });
                    Button applyBtn1 = view3.findViewById(R.id.applyBtn);
                    Button cancelBtn1 = view3.findViewById(R.id.cancelBtn);
                    applyBtn1.setOnClickListener(v3 -> {
                        timeBtn.setText(String.format(new Locale("en"), "%02d:%02d %s", hourPicker.getValue() > 12 ? hourPicker.getValue() - 12 : hourPicker.getValue() == 0 ? 12 : hourPicker.getValue(), minutePicker.getValue(), amPm.get(amPmPicker.getValue())));
                        bottomSheetDialog2.dismiss();
                    });
                    cancelBtn1.setOnClickListener(v3 -> {
                        bottomSheetDialog2.dismiss();
                    });
                });
                MutableLiveData<Date> deadlineDate = new MutableLiveData<>(new Date(datePicker.getDate()));
                datePicker.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
                    deadlineDate.setValue(new Date(year - 1900, month, dayOfMonth));
                });
                Button monthBtn = view2.findViewById(R.id.monthBtn);
                AtomicReference<Boolean> isMonthPicker = new AtomicReference<>(false);
                monthBtn.setOnClickListener(v2 -> {
                    if (isMonthPicker.get()) {
                        return;
                    }
                    isMonthPicker.set(true);
                    BottomSheetDialog monthPicker = new BottomSheetDialog(this);
                    View monthPickerView = getLayoutInflater().inflate(R.layout.modal_month_picker, null);
                    monthPicker.setContentView(monthPickerView);
                    NumberPicker monthPickerNumber = monthPickerView.findViewById(R.id.monthPicker);
                    NumberPicker yearPicker = monthPickerView.findViewById(R.id.yearPicker);
                    monthPickerNumber.setMinValue(1);
                    monthPickerNumber.setMaxValue(12);
                    monthPickerNumber.setDisplayedValues(new String[]{"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"});
                    monthPickerNumber.setValue(deadlineDate.getValue().getMonth() + 1);
                    yearPicker.setMinValue(1970);
                    yearPicker.setMaxValue(2100);
                    yearPicker.setValue(new Date().getYear() + 1900);
                    yearPicker.setWrapSelectorWheel(false);
                    Button applyBtn = monthPickerView.findViewById(R.id.applyBtn);
                    Button cancelBtn = view2.findViewById(R.id.cancelBtn);
                    applyBtn.setOnClickListener(v3 -> {
                        Date date = new Date(yearPicker.getValue() - 1900, monthPickerNumber.getValue() - 1, 1);
                        deadlineDate.setValue(date);
                        monthPicker.dismiss();
                    });
                    cancelBtn.setOnClickListener(v3 -> monthPicker.dismiss());
                    monthPicker.setOnDismissListener(dialog -> isMonthPicker.set(false));
                    monthPicker.show();
                });
                deadlineDate.observe(this, date -> {
                    datePicker.setDate(date.getTime());
                });
                Button applyBtn = view2.findViewById(R.id.applyBtn);
                Button cancelBtn = view2.findViewById(R.id.cancelBtn);
                applyBtn.setOnClickListener(v2 -> {
                    String deadline = Optional.of(deadlineDate.getValue()).map(date -> {
                        if (date.equals(new Date())) {
                            return "Today - ";
                        } else if (date.getDate() == new Date().getDate() + 1) {
                            return "Tomorrow - ";
                        } else {
                            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy - ", Locale.ENGLISH);
                            return formatter.format(date);
                        }
                    }).orElse("");
                    deadline += timeBtn.getText().toString().replace(":", ".");
                    deadlineView.setText(deadline);
                    deadlineView.setVisibility(View.VISIBLE);
                    bottomSheetDialog1.dismiss();
                });
                cancelBtn.setOnClickListener(v2 -> {
                    bottomSheetDialog1.dismiss();
                });
            });
        });
        ImageView menu_btn = findViewById(R.id.menu_btn);
        menu_btn.setOnClickListener(v -> {
            popup = new PopupMenu(this, v, 5);
            popup.getMenuInflater().inflate(R.menu.project_detail_menu, popup.getMenu());
            SpannableString s = new SpannableString(popup.getMenu().getItem(2).getTitle());
            s.setSpan(new ForegroundColorSpan(Color.parseColor("#FF3B30")), 0, s.length(), 0);
            popup.getMenu().getItem(2).setTitle(s);
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
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_change_cover){
                    Intent intent = new Intent(this, SetImage.class);
                    startActivity(intent);
                }
                return false;
            });
            popup.show();

        });
    }
    private void showPopupMenu(){

    }

}
