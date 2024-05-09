package com.nt118.proma.ui.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nt118.proma.MainActivity;
import com.nt118.proma.R;
import com.nt118.proma.model.ImageArray;
import com.nt118.proma.ui.image.SetImage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import de.hdodenhof.circleimageview.CircleImageView;

public class CompleteProfile extends AppCompatActivity {


    private EditText etFullname, etPhoneNumber;
    private TextView etDOB;
    private Button btnSignUp;
    private final MutableLiveData<Integer> selectedImage = new MutableLiveData<>();
    private ImageView setImageBtn;
    private CircleImageView avatarImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_complete_profile);

        InitUi();
        btnSignUp.setOnClickListener(v -> setUserInformation());
        TextView signOutBtn = findViewById(R.id.signOutBtn);
        signOutBtn.setOnClickListener(v -> {
            remove_fcm_token();
            FirebaseAuth.getInstance().signOut();
            // logout from facebook
            FacebookSdk.sdkInitialize(this);
            LoginManager.getInstance().logOut();
            // logout from google
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
            mGoogleSignInClient.signOut();
            SharedPreferences.Editor editor = getSharedPreferences("user", MODE_PRIVATE).edit();
            editor.clear().apply();
            Intent intent = new Intent(CompleteProfile.this, Login.class);
            startActivity(intent);
        });
    }



    private void InitUi() {
        etFullname=findViewById(R.id.etFullName);
        etPhoneNumber=findViewById(R.id.etPhoneNumber);
        etDOB=findViewById(R.id.etDate);
        btnSignUp=findViewById(R.id.btn_Sign_Up);
        SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
        etFullname.setText(sharedPreferences.getString("name", ""));
        etPhoneNumber.setText(sharedPreferences.getString("phone_number", ""));
        etDOB.setText(sharedPreferences.getString("dob", ""));
        if (!etDOB.getText().toString().isEmpty()) {
            etDOB.setTextColor(getResources().getColor(R.color.black));
        }
        setImageBtn = findViewById(R.id.setImageBtn);
        avatarImage = findViewById(R.id.avatar2);
        selectedImage.setValue(sharedPreferences.getInt("avatar", -1));
        selectedImage.observe(this, integer -> {
            if (integer == -1) {
                avatarImage.setImageResource(R.color.grey);
                return;
            }
            avatarImage.setImageResource(new ImageArray().getAvatarImage().get(integer));
        });
        setImageBtn.setOnClickListener(v -> {
            Intent intent = new Intent(CompleteProfile.this, SetImage.class);
            intent.putExtra("type", "avatar");
            intent.putExtra("selectedImage", selectedImage.getValue());
            intent.putIntegerArrayListExtra("images", new ImageArray().getAvatarImage());
            startActivityForResult(intent, 1);
        });
        AtomicReference<Boolean> isDatePickerShowing = new AtomicReference<>(false);
        etDOB.setOnClickListener(v1 -> {
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
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                selectedImage.setValue(data.getIntExtra("image", -1));
            }
        }
    }

    private void setUserInformation() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String email = user.getProviderData().get(1).getEmail();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (selectedImage.getValue() == -1) {
            Toast.makeText(this, "Please select an avatar", Toast.LENGTH_SHORT).show();
            return;
        }
        if (etFullname.getText().toString().isEmpty()) {
            etFullname.setError("Fullname is required");
            return;
        }
        if (etPhoneNumber.getText().toString().isEmpty()) {
            etPhoneNumber.setError("Phone number is required");
            return;
        }
        if (etDOB.getText().toString().isEmpty()) {
            etDOB.setError("Date of birth is required");
            return;
        }
        db.collection("users").whereEqualTo("email", email).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().getDocuments().size() == 0) {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("email", email);
                    userMap.put("avatar", selectedImage.getValue());
                    userMap.put("name", etFullname.getText().toString());
                    userMap.put("phone_number", etPhoneNumber.getText().toString());
                    userMap.put("dob", etDOB.getText().toString());
                    db.collection("users").add(userMap);
                    Intent intent = new Intent(CompleteProfile.this, MainActivity.class);
                    startActivity(intent);
                    return;
                }
                task.getResult().getDocuments().get(0).getReference().update("avatar", selectedImage.getValue());
                task.getResult().getDocuments().get(0).getReference().update("name", etFullname.getText().toString());
                task.getResult().getDocuments().get(0).getReference().update("phone_number", etPhoneNumber.getText().toString());
                task.getResult().getDocuments().get(0).getReference().update("dob", etDOB.getText().toString());
                SharedPreferences.Editor editor = getSharedPreferences("user", MODE_PRIVATE).edit();
                editor.putInt("avatar", selectedImage.getValue());
                editor.putString("name", etFullname.getText().toString());
                editor.putString("phone_number", etPhoneNumber.getText().toString());
                editor.putString("dob", etDOB.getText().toString());
                editor.putBoolean("isCompletedProfile", true);
                editor.apply();
                Intent intent = new Intent(CompleteProfile.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    public void remove_fcm_token() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").whereEqualTo("email", user.getProviderData().get(1).getEmail()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                task.getResult().getDocuments().get(0).getReference().update("fcm_token", "");
            }
        });
    }

    @Override
    public void onBackPressed() {
        // do nothing
        super.onBackPressed();
        moveTaskToBack(true);
    }
}