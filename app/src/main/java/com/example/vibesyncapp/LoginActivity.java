package com.example.vibesyncapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
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

public class LoginActivity extends AppCompatActivity {

    EditText emailInput,passwordInput;
    Button Loginbtn,googlelogin;
    TextView clickRegister,recoverpassword;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    int REQ_ONE_TAP=20;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent=new Intent(LoginActivity.this,MainActivity.class);
            startActivity(intent);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailInput=findViewById(R.id.email);
        passwordInput=findViewById(R.id.password);
        Loginbtn=findViewById(R.id.loginbtn);
        clickRegister=findViewById(R.id.clickRegister);
        mAuth=FirebaseAuth.getInstance();
        progressBar=findViewById(R.id.progressbar);
        recoverpassword=findViewById(R.id.recoverpassword);
        googlelogin=findViewById(R.id.loginwithgoogle);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();

        GoogleSignInClient oneTapClient = GoogleSignIn.getClient(this, gso);



        clickRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
            }
        });

        recoverpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder=new AlertDialog.Builder(LoginActivity.this);
                builder.setTitle("Recover Password");
                LinearLayout linearLayout=new LinearLayout(LoginActivity.this);
                EditText emailtext=new EditText(LoginActivity.this);
                emailtext.setHint("Enter Email");
                emailtext.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                emailtext.setEms(20);

                linearLayout.addView(emailtext);
                linearLayout.setPadding(10,10,10,10);

                builder.setView(linearLayout);
                builder.setPositiveButton("Recover", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String email=emailtext.getText().toString().trim();

                        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if(task.isSuccessful())
                                {
                                    Toast.makeText(getApplicationContext(),"Email sent",Toast.LENGTH_LONG).show();
                                }else {
                                    Toast.makeText(getApplicationContext(),"Failed",Toast.LENGTH_LONG).show();
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                              Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                            }
                        });


                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builder.create().show();

            }
        });

        Loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=emailInput.getText().toString();
                String password=passwordInput.getText().toString();
                progressBar.setVisibility(View.VISIBLE);

                if(email.isEmpty())
                {
                    Toast.makeText(LoginActivity.this,"Please Enter Email First",Toast.LENGTH_LONG).show();
                    return;
                }

                if(password.isEmpty())
                {
                    Toast.makeText(LoginActivity.this,"Please Enter Password First",Toast.LENGTH_LONG).show();
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(LoginActivity.this, "Login Successful.",
                                            Toast.LENGTH_SHORT).show();
                                    Intent intent=new Intent(LoginActivity.this,MainActivity.class);
                                    startActivity(intent);

                                } else {

                                    Toast.makeText(LoginActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();

                                }
                            }
                        });
            }
        });

        googlelogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = oneTapClient.getSignInIntent();

                startActivityForResult(signInIntent, REQ_ONE_TAP);


            }
        });



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_ONE_TAP) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                String idToken = account.getIdToken();
                if (idToken != null) {
                    AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
                    mAuth.signInWithCredential(firebaseCredential)
                            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign-in success
                                        Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                    } else {
                                        // Sign-in failed
                                        Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            } catch (ApiException e) {
                Toast.makeText(this, "Sign in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

}