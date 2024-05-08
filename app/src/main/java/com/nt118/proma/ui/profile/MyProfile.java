package com.nt118.proma.ui.profile;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nt118.proma.R;

public class MyProfile extends AppCompatActivity {

    ImageView imgBack;
    EditText etFullName, etDate, etEmail, etPhoneNumber;
    FirebaseFirestore db;
    DocumentReference docRef;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_profile);

        initUi();
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //lay email cua user dang dang nhap
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String email = user.getProviderData().get(1).getEmail();
        //Truy cap Firestore va lay du lieu
        db = FirebaseFirestore.getInstance();
        db.collection("users").whereEqualTo("email",email).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                //lay cac truong thong tin tu db
                DocumentSnapshot document = task.getResult().getDocuments().get(0);
                String name = document.getString("name");
                String dob = document.getString("dob");
                String phoneNumber = document.getString("phone_number");
                // Dien thong tin lay tu db vao ui
                etFullName.setText(name);
                etDate.setText(dob);
                etPhoneNumber.setText(phoneNumber);
                etEmail.setText(email);
            }
        });
    }

    private void initUi(){
        //anh xa ui
        imgBack=findViewById(R.id.img_Back);
        etFullName = findViewById(R.id.etFullName);
        etDate = findViewById(R.id.etDate);
        etEmail = findViewById(R.id.etEmail);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
    }
}
