package com.nt118.proma.ui.member;

import static com.nt118.proma.ui.task.TaskDetail.setWindowFlag;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nt118.proma.R;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddMember extends AppCompatActivity {
    private static final String EMAIL_REGEX =
        "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";

    private static final Pattern pattern = Pattern.compile(EMAIL_REGEX);

    public static boolean isValidEmail(String email) {
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_member);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        Intent intent = getIntent();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        TextView add_btn = findViewById(R.id.add_btn);
        MutableLiveData<ArrayList<String>> members = new MutableLiveData<>();
        members.setValue(intent.getStringArrayListExtra("members"));
        MutableLiveData<ArrayList<String>> names = new MutableLiveData<>();
        names.setValue(intent.getStringArrayListExtra("name"));
        LinearLayout member_list = findViewById(R.id.memberList);
        add_btn.setOnClickListener(v -> {
            // validate email format
            EditText invite_field = findViewById(R.id.invite_field);
            String emailInvite = invite_field.getText().toString();
            if (!isValidEmail(emailInvite)) {
                invite_field.setError("Invalid email format");
                return;
            }
            if (user.getProviderData().get(1).getEmail().equals(emailInvite)) {
                invite_field.setError("You can't invite yourself");
                return;
            }
            Log.d("AddMember", "onCreate: " + emailInvite);
            db.collection("users").whereEqualTo("email", emailInvite).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (task.getResult().isEmpty()) {
                        handleNoResult();
                    } else {
                        if (members.getValue() == null) {
                            members.setValue(new ArrayList<>());
                            names.setValue(new ArrayList<>());
                        }
                        if (members.getValue().contains(emailInvite)) {
                            invite_field.setError("Email already exists");
                            return;
                        }
                        ArrayList<String> tempName = names.getValue();
                        tempName.add(task.getResult().getDocuments().get(0).getString("name"));
                        names.setValue(tempName);
                        ArrayList<String> temp = members.getValue();
                        temp.add(emailInvite);
                        members.setValue(temp);
                    }
                }
            });
        });
        members.observe(this, strings -> {
            TextView memberCount = findViewById(R.id.memberCount);
            memberCount.setText("Project members (" + strings.size() + ")");
            member_list.removeAllViews();
            for (String email : strings) {
                View item_member = getLayoutInflater().inflate(R.layout.item_member, null);
                TextView email_member = item_member.findViewById(R.id.email);
                TextView name_member = item_member.findViewById(R.id.name);
                ImageView removeBtn = item_member.findViewById(R.id.removeBtn);
                email_member.setText(email);
                name_member.setText(names.getValue().get(strings.indexOf(email)));
                removeBtn.setOnClickListener(v -> {
                    ArrayList<String> temp = members.getValue();
                    ArrayList<String> tempName = names.getValue();
                    tempName.remove(names.getValue().get(strings.indexOf(email)));
                    temp.remove(email);
                    names.setValue(tempName);
                    members.setValue(temp);
                });
                // margin top 13dp
                float dip = 13f;
                Resources r = getResources();
                float px = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        dip,
                        r.getDisplayMetrics()
                );
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, (int) px, 0, 0);
                item_member.setLayoutParams(params);
                member_list.addView(item_member);
            }
        });
        Button save_btn = findViewById(R.id.saveBtn);
        save_btn.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putStringArrayListExtra("members", members.getValue());
            resultIntent.putStringArrayListExtra("names", names.getValue());
            setResult(RESULT_OK, resultIntent);
            finish();
        });
        Button cancel_btn = findViewById(R.id.cancelBtn);
        cancel_btn.setOnClickListener(v -> {
            finish();
        });
    }

    public void handleNoResult() {
        Dialog dialog = new Dialog(this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.no_match_result);
        dialog.show();
    }
}
