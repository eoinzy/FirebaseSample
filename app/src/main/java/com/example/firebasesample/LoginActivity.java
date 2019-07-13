package com.example.firebasesample;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.firebasesample.data.FirebaseHandler;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LoginActivity extends AppCompatActivity {
    private final static String TAG = LoginActivity.class.getSimpleName();

    private EditText mLoginUsernameView;
    private EditText mLoginPasswordView;

    private EditText mRegisterFirstNameView;
    private EditText mRegisterLastNameView;
    private EditText mRegisterUsernameView;
    private EditText mRegisterPasswordView;

    private static final int PASSWORD_MIN_LENGTH = 5;
    private static final String TIME_FORMAT = "ddMMyyHHmmss";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        //Do a quick check to see if the user is logged in.
        FirebaseHandler fbh = new FirebaseHandler();
        if (fbh.isUserLoggedIn()) {
            launchProfileScreen();
        }

        //Setup login views
        mLoginUsernameView = findViewById(R.id.login_username);
        mLoginPasswordView = findViewById(R.id.login_password);

        Button signInButton = findViewById(R.id.btn_login);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin(mLoginUsernameView.getText().toString(), mLoginPasswordView.getText().toString());
            }
        });

        //Setup register views
        mRegisterFirstNameView = findViewById(R.id.register_first_name);
        mRegisterLastNameView = findViewById(R.id.register_last_name);
        mRegisterUsernameView = findViewById(R.id.register_email);
        mRegisterPasswordView = findViewById(R.id.register_password);

        Button registerButton = findViewById(R.id.btn_register);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegistration(mRegisterFirstNameView.getText().toString(),
                        mRegisterLastNameView.getText().toString(),
                        mRegisterUsernameView.getText().toString(),
                        mRegisterPasswordView.getText().toString());
            }
        });
    }

    private void attemptLogin(final String username, final String password) {
        clearFormErrors();
        showLoginProgress(true);

        if (isUsernameValid(username) && isPasswordValid(password)) {
            final FirebaseAuth mAuth = FirebaseAuth.getInstance();
            mAuth.signInWithEmailAndPassword(username, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            showLoginProgress(false);
                            if (task.isSuccessful()) {
                                FirebaseUser currentUser = mAuth.getCurrentUser();
                                FirebaseHandler fbHandler = new FirebaseHandler();
                                fbHandler.getLastLogin(currentUser.getUid()).setValue(getCurrentTimeStamp());

                                launchProfileScreen();
                            } else {
                                Log.e(TAG, "Error logging in", task.getException());
                                Snackbar.make(mLoginPasswordView, task.getException().getLocalizedMessage(), Snackbar.LENGTH_LONG).show();
                            }
                        }
                    });
        } else {
            showLoginProgress(false);
            Log.e(TAG, "Error logging in with username or password");
            Snackbar.make(mLoginPasswordView, "Error logging in with username or password", Snackbar.LENGTH_LONG).show();
        }
    }

    private void launchProfileScreen() {
        Intent i = new Intent(LoginActivity.this, ProfileActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    private void attemptRegistration(final String firstName, final String lastName, final String email, final String password) {
        clearFormErrors();
        showRegisterProgress(true);

        if (isUsernameValid(email) && isPasswordValid(password)) {
            final FirebaseAuth mAuth = FirebaseAuth.getInstance();
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            showRegisterProgress(false);
                            if (task.isSuccessful()) {
                                FirebaseUser currentUser = mAuth.getCurrentUser();

                                FirebaseHandler firebaseHandler = new FirebaseHandler();
                                firebaseHandler.getProfileEmail(currentUser.getUid()).setValue(currentUser.getEmail());
                                firebaseHandler.getFirstName(currentUser.getUid()).setValue(firstName);
                                firebaseHandler.getLastName(currentUser.getUid()).setValue(lastName);
                                firebaseHandler.getLastLogin(currentUser.getUid()).setValue(getCurrentTimeStamp());

                                launchProfileScreen();
                            } else {
                                Log.e(TAG, "Error in user registration", task.getException());
                                Snackbar.make(mRegisterUsernameView, "Registration Error: " + task.getException().getLocalizedMessage(), Snackbar.LENGTH_LONG).show();
                            }
                        }
                    });
        } else {
            showRegisterProgress(false);
            Log.e(TAG, "Error registering with username or password");
            Snackbar.make(mRegisterUsernameView, "Error registering with username or password", Snackbar.LENGTH_LONG).show();
        }
    }

    private String getCurrentTimeStamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_FORMAT, Locale.ENGLISH);
        return dateFormat.format(new Date());
    }

    /**
     * Basic check that the username isn't empty. Should throw in a MIN_LENGTH as well as
     * checking to make sure its a proper email address.
     *
     * @param username The username to validate
     * @return True if valid, false otherwise.
     */
    private boolean isUsernameValid(String username) {
        return null != username && !username.isEmpty();
    }

    /**
     * Checks to see if the password is valid.
     * here, we just check that it is greater than an arbitrary length.
     * We can check if its got the required number of uppercase, special chars later...
     *
     * @param password The password to validate
     * @return True if valid, false otherwise.
     */
    private boolean isPasswordValid(String password) {
        return password.length() >= PASSWORD_MIN_LENGTH;
    }

    /**
     * When there is no error, we can clear all errors.
     * It doesn't matter if it's the login screen or register screen, so just reset everything.
     */
    private void clearFormErrors() {
        mLoginUsernameView.setError(null);
        mLoginPasswordView.setError(null);
        mRegisterFirstNameView.setError(null);
        mRegisterLastNameView.setError(null);
        mRegisterUsernameView.setError(null);
        mRegisterPasswordView.setError(null);
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
