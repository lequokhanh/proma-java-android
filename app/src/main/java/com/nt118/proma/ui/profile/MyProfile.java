package com.nt118.proma.ui.profile;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nt118.proma.R;
import com.nt118.proma.model.ImageArray;
import com.nt118.proma.ui.image.SetImage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyProfile extends AppCompatActivity {

    private final MutableLiveData<Integer> selectedImage = new MutableLiveData<>();
    private final MutableLiveData<String> name = new MutableLiveData<>();
    private final MutableLiveData<String> dob = new MutableLiveData<>();
    private final MutableLiveData<String> phoneNumber = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoaded = new MutableLiveData<>(false);
    private String email;
    private EditText etFullName, etEmail, etPhoneNumber;
    private TextView etDOB;
    private FirebaseFirestore db;
    private DocumentReference docRef;
    private ImageView imgBack, setAvatar;
    private CircleImageView avatar;
    private Button update;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_profile);
        Dialog loading = createLoadingDialog();
        initUi();
        imgBack.setOnClickListener(v -> finish());
        handleClickSetAvatar();
        // get email from firebase
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        email = user.getProviderData().get(1).getEmail();
        etEmail.setText(email);
        loadDB();
        isLoaded.observe(this, aBoolean -> {
            if (aBoolean) {
                loading.dismiss();
            } else {
                loading.show();
            }
        });
        name.observe(this, s -> etFullName.setText(s));
        dob.observe(this, s -> etDOB.setText(s));
        phoneNumber.observe(this, s -> etPhoneNumber.setText(s));
        selectedImage.observe(this, integer -> avatar.setImageResource(new ImageArray().getAvatarImage().get(integer)));
        AtomicReference<Boolean> isDatePickerShowing = new AtomicReference<>(false);
        etDOB.setOnClickListener(v -> showPopupSetDOB(isDatePickerShowing));
        update.setOnClickListener(v -> saveToDB());
    }

    private void handleClickSetAvatar() {
        setAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(this, SetImage.class);
            intent.putExtra("type", "avatar");
            intent.putExtra("selectedImage", selectedImage.getValue());
            intent.putIntegerArrayListExtra("images", new ImageArray().getAvatarImage());
            startActivityForResult(intent, 1);
        });
    }

    private void saveToDB() {
        isLoaded.setValue(false);
        db.collection("users").whereEqualTo("email", email).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult().getDocuments().get(0);
                docRef = db.collection("users").document(document.getId());
                docRef.update("name", etFullName.getText().toString());
                docRef.update("dob", etDOB.getText().toString());
                docRef.update("phone_number", etPhoneNumber.getText().toString());
                docRef.update("avatar", selectedImage.getValue());
                SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("name", etFullName.getText().toString());
                editor.putString("dob", etDOB.getText().toString());
                editor.putString("phone_number", etPhoneNumber.getText().toString());
                editor.putInt("avatar", selectedImage.getValue());
                editor.apply();
                isLoaded.setValue(true);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) return;
        if (requestCode == 1) {
            selectedImage.setValue(data.getIntExtra("image", 0));
        }
    }

    private void loadDB() {
        db = FirebaseFirestore.getInstance();
        db.collection("users").whereEqualTo("email", email).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult().getDocuments().get(0);
                name.setValue(document.getString("name"));
                dob.setValue(document.getString("dob"));
                phoneNumber.setValue(document.getString("phone_number"));
                selectedImage.setValue(document.getLong("avatar").intValue());
                isLoaded.setValue(true);
            }
        });
    }

    private void initUi(){
        imgBack=findViewById(R.id.img_Back);
        etFullName = findViewById(R.id.etFullName);
        etDOB = findViewById(R.id.etDate);
        etEmail = findViewById(R.id.etEmail);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        setAvatar = findViewById(R.id.setAvatar);
        avatar = findViewById(R.id.avatar2);
        update = findViewById(R.id.updateBtn);
    }

    private void showPopupSetDOB(AtomicReference<Boolean> isDatePickerShowing) {
        BottomSheetDialog datePickerDialog = new BottomSheetDialog(this);
        View view2 = LayoutInflater.from(this).inflate(R.layout.modal_date_picker, null);
        datePickerDialog.setContentView(view2);
        if (isDatePickerShowing.get()) {
            return;
        }
        CalendarView datePicker = view2.findViewById(R.id.datePicker);
        if (!etDOB.getText().toString().isEmpty()) {
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy", Locale.ENGLISH);
            try {
                Date date = formatter.parse(etDOB.getText().toString());
                datePicker.setDate(date.getTime());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        isDatePickerShowing.set(true);
        datePickerDialog.show();
        View timeContainer = view2.findViewById(R.id.timeContainer);
        timeContainer.setVisibility(View.GONE);
        MutableLiveData<Date> dobDate = new MutableLiveData<>(new Date(datePicker.getDate()));
        datePicker.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            dobDate.setValue(new Date(year - 1900, month, dayOfMonth));
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
            monthPickerNumber.setValue(dobDate.getValue().getMonth() + 1);
            yearPicker.setMinValue(1970);
            yearPicker.setMaxValue(2100);
            yearPicker.setValue(new Date().getYear() + 1900);
            yearPicker.setWrapSelectorWheel(false);
            Button applyBtn = monthPickerView.findViewById(R.id.applyBtn);
            Button cancelBtn = view2.findViewById(R.id.cancelBtn);
            applyBtn.setOnClickListener(v3 -> {
                Date date = new Date(yearPicker.getValue() - 1900, monthPickerNumber.getValue() - 1, 1);
                dobDate.setValue(date);
                monthPicker.dismiss();
            });
            cancelBtn.setOnClickListener(v3 -> monthPicker.dismiss());
            monthPicker.setOnDismissListener(dialog -> isMonthPicker.set(false));
            monthPicker.show();
        });
        Button applyBtn = view2.findViewById(R.id.applyBtn);
        Button cancelBtn = view2.findViewById(R.id.cancelBtn);
        dobDate.observe(this, date -> {
            datePicker.setDate(date.getTime());
        });
        datePicker.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            dobDate.setValue(new Date(year - 1900, month, dayOfMonth));
        });
        applyBtn.setOnClickListener(v2 -> {
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy", Locale.ENGLISH);
            String dob = formatter.format(dobDate.getValue());
            etDOB.setText(dob);
            etDOB.setTextColor(getResources().getColor(R.color.black));
            datePickerDialog.dismiss();
            isDatePickerShowing.set(false);
        });
        cancelBtn.setOnClickListener(v2 -> {
            datePickerDialog.dismiss();
            isDatePickerShowing.set(false);
        });
        datePickerDialog.setOnDismissListener(dialog -> isDatePickerShowing.set(false));
    }

    private Dialog createLoadingDialog() {
        Dialog loading = new Dialog(this);
        loading.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        loading.setContentView(R.layout.loading);
        loading.show();
        return loading;
    }
}
