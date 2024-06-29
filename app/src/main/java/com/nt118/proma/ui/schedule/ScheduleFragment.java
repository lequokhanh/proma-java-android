package com.nt118.proma.ui.schedule;

import static com.google.firebase.firestore.Filter.arrayContains;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.TypedValue;
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
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nt118.proma.R;
import com.nt118.proma.databinding.FragmentScheduleBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class ScheduleFragment extends Fragment {

    private FragmentScheduleBinding binding;
    private String email;

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
        LinearLayout listTaskSchedule = binding.listTaskSchedule;
        requireContext();
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("user", Context.MODE_PRIVATE);
        email = sharedPreferences.getString("email", "");
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
            listTaskSchedule.removeAllViews();
            showListTask(listTaskSchedule, date);
        });
        return root;
    }

    private void showListTask(LinearLayout taskContainer, Date date) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> memberTask = new HashMap<>();
        memberTask.put("email", email);
        memberTask.put("isLeader", false);
        Map<String, Object> memberLeader = new HashMap<>();
        memberLeader.put("email", email);
        memberLeader.put("isLeader", true);
        db.collection("tasks")
                .where(Filter.or(arrayContains("members", memberTask), arrayContains("members", memberLeader)))
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy - hh.mm a", Locale.US);
                        for (int i = 0; i < task.getResult().getDocuments().size(); i++) {
                            // compare date of "deadline" field in task (no compare time) with date
                            try {
                                Date deadline = formatter.parse((String) task.getResult().getDocuments().get(i).get("deadline"));
                                if (deadline.getDate() == date.getDate() && deadline.getMonth() == date.getMonth() && deadline.getYear() == date.getYear()) {
                                    View item_schedule = getLayoutInflater().inflate(R.layout.item_schedule, null);
                                    TextView taskName = item_schedule.findViewById(R.id.taskName);
                                    TextView timeTV = item_schedule.findViewById(R.id.timeTV);
                                    ImageView alarmBtn = item_schedule.findViewById(R.id.alarmBtn);
                                    taskName.setText((String) task.getResult().getDocuments().get(i).get("title"));
                                    SimpleDateFormat timeFormatter = new SimpleDateFormat("hh.mm a", Locale.US);
                                    timeTV.setText(timeFormatter.format(deadline));
                                    float dip10 = 10f;
                                    Resources r = getResources();
                                    float px10 = TypedValue.applyDimension(
                                            TypedValue.COMPLEX_UNIT_DIP,
                                            dip10,
                                            r.getDisplayMetrics()
                                    );
                                    taskContainer.addView(item_schedule);
                                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) item_schedule.getLayoutParams();
                                    params.setMargins(0, (int) px10, 0, 0);
                                    int finalI = i;
                                    alarmBtn.setOnClickListener(v -> {
                                        Calendar calendar = Calendar.getInstance();
                                        Intent intent = new Intent(Intent.ACTION_EDIT);
                                        intent.setType("vnd.android.cursor.item/event");
                                        intent.putExtra("beginTime", deadline.getTime());
                                        intent.putExtra("allDay", false);
                                        intent.putExtra("rrule", "FREQ=MINUTELY;INTERVAL=60;COUNT=1");
                                        intent.putExtra("endTime", deadline.getTime() + 60 * 60 * 1000);
                                        intent.putExtra("title", (String) task.getResult().getDocuments().get(finalI).get("title"));
                                        startActivity(intent);
                                    });
                                }
                            } catch (ParseException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                });
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