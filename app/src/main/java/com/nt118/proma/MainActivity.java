package com.nt118.proma;

import static android.Manifest.permission.POST_NOTIFICATIONS;
import static androidx.navigation.ui.NavigationUI.setupWithNavController;

import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nt118.proma.databinding.ActivityMainBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(R.id.navigation_home, R.id.navigation_project, R.id.navigation_schedule, R.id.navigation_profile).build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        setupWithNavController(navView, navController);
        navView.setItemIconTintList(null);
        new Thread(() -> {
            FloatingActionButton create_btn = findViewById(R.id.create_button);
            AtomicReference<Boolean> isDialogShowing = new AtomicReference<>(false);
            create_btn.setOnClickListener(v -> {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MainActivity.this);
                View view1 = LayoutInflater.from(MainActivity.this).inflate(R.layout.modal_create_project, null);
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
                Button addMemberBtn = view1.findViewById(R.id.addMemberBtn);
                addMemberBtn.setOnClickListener(v1 -> {
                });
                Button deadlineBtn = view1.findViewById(R.id.deadlineBtn);
                TextView deadlineView = view1.findViewById(R.id.deadlineView);
                AtomicReference<Boolean> isDatePickerShowing = new AtomicReference<>(false);
                deadlineBtn.setOnClickListener(v1 -> {
                    BottomSheetDialog bottomSheetDialog1 = new BottomSheetDialog(v.getContext());
                    View view2 = LayoutInflater.from(MainActivity.this).inflate(R.layout.modal_date_picker, null);
                    bottomSheetDialog1.setContentView(view2);
                    if (isDatePickerShowing.get()) {
                        return;
                    }
                    CalendarView datePicker = view2.findViewById(R.id.datePicker);
                    if (deadlineView.getVisibility() == View.VISIBLE) {
                        if (deadlineView.getText().toString().equals("Today")) {
                            datePicker.setDate(new Date().getTime());
                        } else if (deadlineView.getText().toString().equals("Tomorrow")) {
                            datePicker.setDate(new Date().getTime() + 86400000);
                        } else {
                            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy", Locale.ENGLISH);
                            try {
                                Date date = formatter.parse(deadlineView.getText().toString());
                                datePicker.setDate(date.getTime());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    isDatePickerShowing.set(true);
                    bottomSheetDialog1.show();
                    View timeContainer = view2.findViewById(R.id.timeContainer);
                    timeContainer.setVisibility(View.GONE);
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
                        Button applyBtn = monthPickerView.findViewById(R.id.signoutBtn);
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
                    Button applyBtn = view2.findViewById(R.id.signoutBtn);
                    Button cancelBtn = view2.findViewById(R.id.cancelBtn);
                    deadlineDate.observe(this, date -> {
                        datePicker.setDate(date.getTime());
                    });
                    datePicker.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
                        deadlineDate.setValue(new Date(year - 1900, month, dayOfMonth));
                    });
                    applyBtn.setOnClickListener(v2 -> {
                        String deadline = Optional.of(deadlineDate.getValue()).map(date -> {
                            if (date.getDate() == new Date().getDate()){
                                return "Today";
                            } else if (date.getDate() == new Date().getDate() + 1) {
                                return "Tomorrow";
                            } else {
                                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy", Locale.ENGLISH);
                                return formatter.format(date);
                            }
                        }).orElse("");
                        deadlineView.setText(deadline);
                        deadlineView.setVisibility(View.VISIBLE);
                        bottomSheetDialog1.dismiss();
                        isDatePickerShowing.set(false);
                    });
                    cancelBtn.setOnClickListener(v2 -> {
                        bottomSheetDialog1.dismiss();
                        isDatePickerShowing.set(false);
                    });
                });
            });
        }).start();
        if (ContextCompat.checkSelfPermission(this, POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{POST_NOTIFICATIONS}, 1);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Locale locale = new Locale("en");
        Locale.setDefault(locale);
        Configuration config = getBaseContext().getResources().getConfiguration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        moveTaskToBack(true);
    }


}