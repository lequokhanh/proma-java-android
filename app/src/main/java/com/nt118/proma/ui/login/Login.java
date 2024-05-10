package com.nt118.proma.ui.login;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.nt118.proma.MainActivity;
import com.nt118.proma.R;

import java.util.Date;

public class Login extends AppCompatActivity {
    GoogleSignInClient googleSignInClient;
    FirebaseAuth mAuth;
    CallbackManager mCallbackManager;
    LinearLayout loadingLogin;
    CheckBox rememberMe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        mAuth = FirebaseAuth.getInstance();
        Button login_button = findViewById(R.id.login_button);
        loadingLogin = findViewById(R.id.loadingLogin);
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(Login.this, MainActivity.class);
            startActivity(intent);
            add_fcm_token();
        }
        // set intent to register activity
        TextView registerBtn = findViewById(R.id.registerBtn);
        registerBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, Register.class);
            startActivity(intent);
        });
        // Login with email and password
        EditText email = findViewById(R.id.etEmail);
        EditText password = findViewById(R.id.etPassword);
        rememberMe = findViewById(R.id.rememberMe);
        login_button.setOnClickListener(v -> {
            //hide keyboard
            InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
            if (inputMethodManager.isAcceptingText()) {
                inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
            }
            // validate email and password
            String email_text = email.getText().toString();
            String password_text = password.getText().toString();
            if (email_text.isEmpty() || password_text.isEmpty()) {
                email.setError("Email is required");
                password.setError("Password is required");
                return;
            } else {
                email.setError(null);
                password.setError(null);
            }
            AuthCredential credential = EmailAuthProvider.getCredential(email_text, password_text);
            loginWithAuthCredential(credential);
        });
        // Google Sign In
        Button googleLoginBtn = findViewById(R.id.googleLoginBtn);
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);
        googleLoginBtn.setOnClickListener(v -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, 4000);
        });
        // Facebook Sign In
        Button facebookLoginBtn = findViewById(R.id.facebookLoginBtn);
        FacebookSdk.sdkInitialize(getApplicationContext());
        mCallbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = new LoginButton(this);
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Toast.makeText(Login.this, "Authentication successful", Toast.LENGTH_SHORT).show();
                AuthCredential credential = FacebookAuthProvider.getCredential(loginResult.getAccessToken().getToken());
                loginWithAuthCredential(credential);
            }

            @Override
            public void onCancel() {
                Toast.makeText(Login.this, "Authentication cancelled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(Login.this, "Authentication error", Toast.LENGTH_SHORT).show();
            }
        });
        facebookLoginBtn.setOnClickListener(v -> {
            loginButton.performClick();
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 4000) {
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            Task<GoogleSignInAccount> signInAccountTask = GoogleSignIn.getSignedInAccountFromIntent(data);
            if (signInAccountTask.isSuccessful()) {
                // When google sign in successful initialize string
                String s = "Google sign in successful";
                // Display Toast
                displayToast(s);
                // Initialize sign in account
                try {
                    // Initialize sign in account
                    GoogleSignInAccount googleSignInAccount = signInAccountTask.getResult(ApiException.class);
                    // Check condition
                    if (googleSignInAccount != null) {
                        // When sign in account is not equal to null initialize auth credential
                        AuthCredential authCredential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);
                        // Check credential
                        loginWithAuthCredential(authCredential);
                    }
                } catch (ApiException e) {
                    e.printStackTrace();
                }
            }
        } else {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void loginWithAuthCredential(AuthCredential credential) {
        loadingLogin.setVisibility(LinearLayout.VISIBLE);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        mAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                db.collection("users").whereEqualTo("email", mAuth.getCurrentUser().getProviderData().get(1).getEmail()).get().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        editor.putString("email", mAuth.getCurrentUser().getProviderData().get(1).getEmail());
                        if (!task1.getResult().isEmpty()) {
                            task1.getResult().getDocuments().get(0).getReference().update("last_login", new Date());
                            editor.putString("name", task1.getResult().getDocuments().get(0).getString("name"));
                            editor.putString("dob", task1.getResult().getDocuments().get(0).getString("dob"));
                            editor.putString("phone_number", task1.getResult().getDocuments().get(0).getString("phone_number"));
                            editor.putInt("avatar", task1.getResult().getDocuments().get(0).getLong("avatar") == null
                                    ? -1
                                    : task1.getResult().getDocuments().get(0).getLong("avatar").intValue());
                        }
                        editor.putBoolean("isCompletedProfile", !(task1.getResult().isEmpty()
                                || task1.getResult().getDocuments().get(0).getString("name") == null
                                || task1.getResult().getDocuments().get(0).getString("dob") == null
                                || task1.getResult().getDocuments().get(0).getString("phone_number") == null
                                || task1.getResult().getDocuments().get(0).getLong("avatar") == null));
                        editor.putBoolean("rememberMe", rememberMe.isChecked());
                        editor.apply();
                        Intent intent = new Intent(Login.this, MainActivity.class);
                        startActivity(intent);
                        add_fcm_token();
                        displayToast("Login successful");
                    }
                });
            } else {
                displayToast("Authentication Failed :" + task.getException().getMessage());
                loadingLogin.setVisibility(LinearLayout.GONE);
            }
        });
    }

    private void displayToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    private void add_fcm_token() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // get fcm token
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                return;
            }
            String token = task.getResult();
            Log.d("FCM", "add_fcm_token: " + token);
            db.collection("users").whereEqualTo("email", user.getProviderData().get(1).getEmail()).get().addOnCompleteListener(task1 -> {
                if (task1.isSuccessful()) {
                    if (task1.getResult().isEmpty()) {
                        return;
                    }
                    task1.getResult().getDocuments().get(0).getReference().update("fcm_token", token);
                }
            });
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        moveTaskToBack(true);
        finishAffinity();
    }
}
