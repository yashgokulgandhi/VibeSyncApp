package com.example.vibesyncapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    EditText emailInput, passwordInput;
    Button loginBtn, googleLogin;
    TextView clickRegister, recoverPassword;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    GoogleSignInClient oneTapClient;
    int REQ_ONE_TAP = 20;

    ActionBar actionBar;

    @Override
    protected void onStart() {
        super.onStart();
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Close the login activity so the user can't navigate back
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailInput = findViewById(R.id.email);
        passwordInput = findViewById(R.id.password);
        loginBtn = findViewById(R.id.loginbtn);
        clickRegister = findViewById(R.id.clickRegister);
        progressBar = findViewById(R.id.progressbar);
        recoverPassword = findViewById(R.id.recoverpassword);
        googleLogin = findViewById(R.id.loginwithgoogle);

        actionBar=getSupportActionBar();
        actionBar.setIcon(R.mipmap.ic_launcher_foreground);
        actionBar.setTitle("VibeSync");
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id)) // Ensure correct web client ID
                .requestEmail()
                .build();

        oneTapClient = GoogleSignIn.getClient(this, gso);

        clickRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        recoverPassword.setOnClickListener(v -> recoverPasswordDialog());

        loginBtn.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            progressBar.setVisibility(View.VISIBLE);

            if (!isValidEmail(email)) {
                Toast.makeText(LoginActivity.this, "Please enter a valid email", Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);

                return;
            }

            if (password.isEmpty() || password.length() < 6) {
                Toast.makeText(LoginActivity.this, "Password must be at least 6 characters", Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                           if(task.getResult().getAdditionalUserInfo().isNewUser())
                           {
                               FirebaseUser user = mAuth.getCurrentUser();
                               String emailId = user.getEmail();
                               String uid = user.getUid();

                               HashMap<Object, String> hashMap = new HashMap<>();
                               hashMap.put("email", emailId);
                               hashMap.put("uid", uid);
                               hashMap.put("name", "");
                               hashMap.put("phone", "");
                               hashMap.put("image", "");

                               // Storing user info in Firebase Database
                               FirebaseDatabase database = FirebaseDatabase.getInstance();
                               DatabaseReference reference = database.getReference("Users");
                               reference.child(uid).setValue(hashMap);
                           }


                            Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "Authentication failed If you have not Registered then Register first", Toast.LENGTH_LONG).show();
                        }
                    });
        });

        googleLogin.setOnClickListener(v -> {
            Intent signInIntent = oneTapClient.getSignInIntent();

            startActivityForResult(signInIntent, REQ_ONE_TAP);
        });
    }

    private void recoverPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setTitle("Recover Password");

        LinearLayout linearLayout = new LinearLayout(LoginActivity.this);
        EditText emailText = new EditText(LoginActivity.this);
        emailText.setHint("Enter Email");
        emailText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailText.setEms(20);

        linearLayout.addView(emailText);
        linearLayout.setPadding(10, 10, 10, 10);

        builder.setView(linearLayout);
        builder.setPositiveButton("Recover", (dialog, which) -> {
            String email = emailText.getText().toString().trim();
            if (!email.isEmpty()) {
                mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Recovery email sent", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "Failed to send email", Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(e -> Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show());
            } else {
                Toast.makeText(LoginActivity.this, "Enter a valid email", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_ONE_TAP) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

                mAuth.signInWithCredential(credential).addOnCompleteListener(this, signInTask -> {
                    if (signInTask.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && signInTask.getResult().getAdditionalUserInfo().isNewUser()) {
                            String emailId = user.getEmail();
                            String uid = user.getUid();

                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put("email", emailId);
                            hashMap.put("uid", uid);
                            hashMap.put("name", "");
                            hashMap.put("phone", "");
                            hashMap.put("image", "");

                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference reference = database.getReference("Users");
                            reference.child(uid).setValue(hashMap)
                                    .addOnCompleteListener(saveTask -> {
                                        if (saveTask.isSuccessful()) {
                                            Toast.makeText(LoginActivity.this, "User data saved successfully", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(LoginActivity.this, "Failed to save user data", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }

                        Toast.makeText(LoginActivity.this, "Google Sign-In Successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
     } else {
                                        Toast.makeText(LoginActivity.this, "Google sign-in failed", Toast.LENGTH_SHORT).show();
                                    }
                                });


            } catch (ApiException e) {
                Toast.makeText(this, "Sign in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
