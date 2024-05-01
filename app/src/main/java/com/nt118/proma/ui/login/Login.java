package com.nt118.proma.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.nt118.proma.MainActivity;
import com.nt118.proma.R;

public class Login extends AppCompatActivity {
    GoogleSignInClient googleSignInClient;
    FirebaseAuth mAuth;
    CallbackManager mCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        mAuth = FirebaseAuth.getInstance();
        Button login_button = findViewById(R.id.login_button);
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
        login_button.setOnClickListener(v -> {
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
            mAuth.signInWithEmailAndPassword(email_text, password_text).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Intent intent = new Intent(Login.this, MainActivity.class);
                    startActivity(intent);
                    add_fcm_token();
                } else {
                    email.setError("Invalid email or password");
                    password.setError("Invalid email or password");
                    Toast.makeText(Login.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                }
            });
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
                mAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Intent intent = new Intent(Login.this, MainActivity.class);
                        startActivity(intent);
                        add_fcm_token();
                        Toast.makeText(Login.this, "Firebase authentication successful", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Login.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                    }
                });
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
                        mAuth.signInWithCredential(authCredential).addOnCompleteListener(this, task -> {
                            // Check condition
                            if (task.isSuccessful()) {
                                Intent intent = new Intent(Login.this, MainActivity.class);
                                startActivity(intent);
                                add_fcm_token();
                                displayToast("Firebase authentication successful");
                            } else {
                                // When task is unsuccessful display Toast
                                displayToast("Authentication Failed :" + task.getException().getMessage());
                            }
                        });
                    }
                } catch (ApiException e) {
                    e.printStackTrace();
                }
            }
        } else {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
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
