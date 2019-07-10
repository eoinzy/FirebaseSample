package com.example.firebasesample;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LoginActivity extends AppCompatActivity {
    private final static String TAG = LoginActivity.class.getSimpleName();

    private EditText mUsernameView;
    private EditText mPasswordView;

    private static final int PASSWORD_MIN_LENGTH = 5;
    private static final String TIME_FORMAT = "ddMMyyHHmmss";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        //Enable offline caching of Firebase data
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        mUsernameView = findViewById(R.id.login_username);
        mPasswordView = findViewById(R.id.login_password);

        Button signInButton = findViewById(R.id.btn_login);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin(mUsernameView.getText().toString(), mPasswordView.getText().toString());
            }
        });

        Button registerButton = findViewById(R.id.btn_register);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText registerFirstName = findViewById(R.id.register_first_name);
                EditText registerLastName = findViewById(R.id.register_last_name);
                EditText registerEmail = findViewById(R.id.register_email);
                EditText registerPassword = findViewById(R.id.register_password);
                attemptRegistration(registerFirstName.getText().toString(),
                        registerLastName.getText().toString(),
                        registerEmail.getText().toString(),
                        registerPassword.getText().toString());
            }
        });
    }

    private void attemptLogin(final String username, final String password) {
        clearLoginErrors();
        showLoginProgress(true);

        if (isUsernameValid(username) && isPasswordValid(password)) {
            final FirebaseAuth mAuth = FirebaseAuth.getInstance();
            mAuth.signInWithEmailAndPassword(username, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            showLoginProgress(false);
                            if (task.isSuccessful()) {
                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                DatabaseReference myRef = database.getReference();
                                FirebaseUser currentUser = mAuth.getCurrentUser();
                                myRef.child("Users").child(currentUser.getUid()).child("profile").child("last_login").setValue(getCurrentTimeStamp());
                                Intent i = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(i);
                            } else {
                                Log.e(TAG, "Error logging in", task.getException());
                                //TODO: Show error
                                Snackbar.make(mPasswordView, task.getException().getLocalizedMessage(), Snackbar.LENGTH_LONG).show();
                            }
                        }
                    });
        } else {
            //TODO: Handle user/pass error
        }
    }

    private void attemptRegistration(final String firstName, final String lastName, final String email, final String password) {
        if (isUsernameValid(email) && isPasswordValid(password)) {
            final FirebaseAuth mAuth = FirebaseAuth.getInstance();
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()) {
                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                DatabaseReference myRef = database.getReference();
                                FirebaseUser currentUser = mAuth.getCurrentUser();
                                Log.d(TAG, "CurrentUser: " + currentUser.getUid() + "::" + currentUser.getEmail());
                                myRef.child("Users").child(currentUser.getUid()).child("profile").child("email").setValue(currentUser.getEmail());
                                myRef.child("Users").child(currentUser.getUid()).child("profile").child("first_name").setValue(firstName);
                                myRef.child("Users").child(currentUser.getUid()).child("profile").child("last_name").setValue(lastName);
                                myRef.child("Users").child(currentUser.getUid()).child("profile").child("last_login").setValue(getCurrentTimeStamp());

                                Intent i = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(i);
                            } else {
                                //TODO: Handle registration error
                            }
                        }
                    });
        }
    }

    private String getCurrentTimeStamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_FORMAT, Locale.ENGLISH);
        return dateFormat.format(new Date());
    }

    private boolean isUsernameValid(String username) {
        //TODO: Replace this with proper logic
        return null != username && !username.isEmpty();
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with proper logic
        return password.length() >= PASSWORD_MIN_LENGTH;
    }

    private void clearLoginErrors() {
        mUsernameView.setError(null);
        mPasswordView.setError(null);
    }

    /**
     * Shows the progress UI for the Login screen
     */
    private void showLoginProgress(final boolean show) {
        findViewById(R.id.login_progress).setVisibility(show ? View.VISIBLE : View.GONE);
    }

    /**
     * Shows the progress UI for the Register screen
     */
    private void showRegisterProgress(final boolean show) {
        findViewById(R.id.register_progress).setVisibility(show ? View.VISIBLE : View.GONE);
    }

    /**
     * Switch between showing the login screen and the register screen.
     *
     * @param show True to show the login screen, false to show register screen.
     */
    private void showLogin(boolean show) {
        findViewById(R.id.sign_in_form).setVisibility(show ? View.VISIBLE : View.GONE);
        findViewById(R.id.register_form).setVisibility(show ? View.GONE : View.VISIBLE);
    }

    public void switchView(View view) {
        switch (view.getId()) {
            case R.id.login_not_signed_up:
                showLogin(false);
                break;
            case R.id.register_already_registered:
                showLogin(true);
                break;
        }
    }
}
