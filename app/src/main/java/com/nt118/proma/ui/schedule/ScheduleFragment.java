package com.nt118.proma.ui.schedule;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.nt118.proma.R;
import com.nt118.proma.databinding.FragmentScheduleBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

public class ScheduleFragment extends Fragment {

    private FragmentScheduleBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ScheduleViewModel scheduleViewModel =
                new ViewModelProvider(this).get(ScheduleViewModel.class);

        binding = FragmentScheduleBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        Button month = binding.month;
        MutableLiveData<Date> currentDate = new MutableLiveData<>();
        currentDate.setValue(new Date());
        LinearLayout dayOfWeek = binding.dayOfWeek;
        TextView rangeDay = binding.rangeDay;
        HorizontalScrollView scrollView = binding.horizontalScrollView3;
        ImageView prev = binding.prev;
        ImageView next = binding.next;
        prev.setOnClickListener(v -> {
            currentDate.setValue(getPreviousMonday(currentDate.getValue()));
        });
        next.setOnClickListener(v -> {
            currentDate.setValue(getNextMonday(currentDate.getValue()));
        });
        for (int i = 0; i < dayOfWeek.getChildCount(); i++) {
            ConstraintLayout child = (ConstraintLayout) dayOfWeek.getChildAt(i);
            int finalI = i;
            child.setOnClickListener(v -> {
                currentDate.setValue(getDates(currentDate.getValue())[finalI]);
            });
        }
        rangeDay.setOnClickListener(v -> {
            currentDate.setValue(new Date());
        });
        AtomicBoolean isMonthPicker = new AtomicBoolean(false);
        month.setOnClickListener(v -> {
            if (isMonthPicker.get()) {
                return;
            }
            isMonthPicker.set(true);
            BottomSheetDialog monthPicker = new BottomSheetDialog(getContext());
            View monthPickerView = getLayoutInflater().inflate(R.layout.modal_month_picker, null);
            monthPicker.setContentView(monthPickerView);
            NumberPicker monthPickerNumber = monthPickerView.findViewById(R.id.monthPicker);
            NumberPicker yearPickerNumber = monthPickerView.findViewById(R.id.yearPicker);
            monthPickerNumber.setMinValue(1);
            monthPickerNumber.setMaxValue(12);
            String[] monthList = new String[12];
            for (int i = 0; i < 12; i++) {
                monthList[i] = new SimpleDateFormat("LLLL", Locale.US).format(new Date(0, i, 1));
            }
            monthPickerNumber.setDisplayedValues(monthList);
            monthPickerNumber.setValue(currentDate.getValue().getMonth() + 1);
            yearPickerNumber.setMinValue(1970);
            yearPickerNumber.setMaxValue(2100);
            yearPickerNumber.setValue(currentDate.getValue().getYear() + 1900);
            yearPickerNumber.setWrapSelectorWheel(false);
            Button applyBtn = monthPickerView.findViewById(R.id.welcomeText);
            applyBtn.setOnClickListener(v1 -> {
                currentDate.setValue(new Date(yearPickerNumber.getValue() - 1900, monthPickerNumber.getValue() - 1, 1));
                monthPicker.dismiss();
            });
            monthPicker.show();
            Button cancelBtn = monthPickerView.findViewById(R.id.cancelBtn);
            cancelBtn.setOnClickListener(v1 -> monthPicker.dismiss());
            monthPicker.setOnDismissListener(dialog -> isMonthPicker.set(false));
        });
        currentDate.observe(getViewLifecycleOwner(), date -> {
            setupCalendar(dayOfWeek, date, rangeDay, month);
            scrollToCurrentDay(scrollView, dayOfWeek, date.getDay() == 0 ? 6 : date.getDay() - 1);
        });
        return root;
    }
    @SuppressLint({"UseCompatLoadingForDrawables", "SetTextI18n"})
    private void setupCalendar(LinearLayout dayOfWeek, Date currentDate, TextView rangeDay, Button month) {
        Date[] dates = getDates(currentDate);
        month.setText(new SimpleDateFormat("LLLL yyyy", Locale.US).format(currentDate));
        SimpleDateFormat formatter = new SimpleDateFormat("dd", Locale.US);
        rangeDay.setText(formatter.format(dates[0]) + " - " + formatter.format(dates[6]));
        for (int i = 0; i < dayOfWeek.getChildCount(); i++) {
            ConstraintLayout child = (ConstraintLayout) dayOfWeek.getChildAt(i);
            TextView day = (TextView) child.getChildAt(0);
            TextView date = (TextView) child.getChildAt(1);
            SimpleDateFormat dayFormatter = new SimpleDateFormat("EEE", Locale.US);
            date.setText(String.valueOf(dates[i].getDate()));
            if (day.getText().toString().equals(dayFormatter.format(currentDate))) {
                child.setBackground(getResources().getDrawable(R.drawable.rounded_corner_8_blue));
                day.setTextColor(getResources().getColor(R.color.white));
                date.setTextColor(getResources().getColor(R.color.white));
            } else {
                child.setBackground(getResources().getDrawable(R.drawable.rounded_corner_8_white));
                day.setTextColor(getResources().getColor(R.color.black));
                date.setTextColor(getResources().getColor(R.color.black));
            }
        }
    }
    private void scrollToCurrentDay(HorizontalScrollView srollView, LinearLayout dayOfWeek, int position) {
        View child = dayOfWeek.getChildAt(position);
        srollView.post(() -> srollView.scrollTo(child.getLeft(), 0));
    }
    private Date[] getDates(Date currentDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(getMonday(currentDate));
        Date[] dates = new Date[7];
        for (int i = 0; i < 7; i++) {
            dates[i] = calendar.getTime();
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            System.out.println(dates[i]);
        }
        return dates;
    }

    private Date getMonday(Date currentDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int dayToMonday = (dayOfWeek - 2 + 7) % 7;
        calendar.add(Calendar.DAY_OF_MONTH, -dayToMonday);
        return calendar.getTime();
    }

    private Date getPreviousMonday(Date currentDate) {
        Date monday = getMonday(currentDate);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(monday);
        calendar.add(Calendar.DAY_OF_MONTH, -7);
        return calendar.getTime();
    }

    private Date getNextMonday(Date currentDate) {
        Date monday = getMonday(currentDate);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(monday);
        calendar.add(Calendar.DAY_OF_MONTH, 7);
        return calendar.getTime();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}