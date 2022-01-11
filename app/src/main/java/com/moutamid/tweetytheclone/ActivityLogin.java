package com.moutamid.tweetytheclone;


import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ActivityLogin extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginBtn;
    private ProgressDialog mProgressDialog;
    private DatabaseReference mDatabaseUsers;
    private Boolean isOnline = false;
    //    private SharedPreferences sharedPreferences;
    private EditText userNameEditText_RSD;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            finish();
            Intent intent = new Intent(ActivityLogin.this, BottomNavigationActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return;
        }

        findViewById(R.id.signBtn_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ActivityLogin.this, ActivitySignUp.class));
            }
        });


        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("users");
        mDatabaseUsers.keepSynced(true);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage("Signing you in...");

        // Initializing Views
        initViews();


    }

    private View.OnClickListener loginBtnListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new AlertDialog.Builder(ActivityLogin.this)
                        .setMessage("Do you agree to accept the Terms and conditions & our Privacy Policy?")
                        .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss())
                        .setNeutralButton("Show T&Cs", (dialogInterface, i) -> {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse("https://earnreal.github.io/home/terms-conditions.html"));
                            startActivity(intent);
                        })
                        .setPositiveButton("Yes", (dialogInterface, i) -> {

                            String emailStr = emailEditText.getText().toString();
                            String passwordStr = passwordEditText.getText().toString();

                            if (!TextUtils.isEmpty(emailStr) && !TextUtils.isEmpty(passwordStr)) {

                                signInUserWithNameAndPassword(emailStr, passwordStr);

                            } else if (TextUtils.isEmpty(emailStr)) {

                                emailEditText.setError("Please enter emailStr");
                                emailEditText.requestFocus();

                            } else if (TextUtils.isEmpty(passwordStr)) {

                                passwordEditText.setError("Please enter password");
                                passwordEditText.requestFocus();

                            }
                        })
                        .show();

            }
        };
    }

    private void signInUserWithNameAndPassword(final String emailStr, final String passwordStr) {

        mProgressDialog.show();

        if (!Patterns.EMAIL_ADDRESS.matcher(emailStr).matches()) {
            //if Email Address is Invalid..

            mProgressDialog.dismiss();
            emailEditText.setError("Email is not valid. Make sure no spaces and special characters are included");
            emailEditText.requestFocus();
        } else {

            mAuth.signInWithEmailAndPassword(emailStr, passwordStr).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful()) {

                        getUserdetails();

                    } else {

                        mProgressDialog.dismiss();
                        Toast.makeText(ActivityLogin.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }

                }
            });

        }

    }

    private void getUserdetails() {
        mDatabaseUsers.child(mAuth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (!snapshot.exists()) {
                            return;
                        }

                        new Utils().storeString(ActivityLogin.this,
                                "usernameStr", snapshot.child("name").getValue(String.class));

                        new Utils().storeString(ActivityLogin.this,
                                "profileUrl", snapshot.child("profileUrl").getValue(String.class));

                        mProgressDialog.dismiss();

                        finish();
                        Intent intent = new Intent(ActivityLogin.this, BottomNavigationActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);

                        Toast.makeText(ActivityLogin.this, "You are logged in!", Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        mProgressDialog.dismiss();
                        Toast.makeText(ActivityLogin.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void initViews() {
        emailEditText = findViewById(R.id.email_edittext_activity_login);
        passwordEditText = findViewById(R.id.password_edittext_activity_login);
        loginBtn = findViewById(R.id.login_btn_activity_login);
        loginBtn.setOnClickListener(loginBtnListener());
    }

}