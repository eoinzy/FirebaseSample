package com.example.firebasesample.data;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseHandler {

    private static boolean isPersistenceEnabled = false;

    private DatabaseReference databaseReference;

    private static final String USERS_REF = "Users";
    private static final String SUBJECTS_REF = "subjects";

    public FirebaseHandler() {
        if (!isPersistenceEnabled) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            isPersistenceEnabled = true;
        }
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    public DatabaseReference getUserProfile(String uid) {
        return databaseReference.child(USERS_REF).child(uid).child("profile");
    }

    public DatabaseReference getLastLogin(final String uid) {
        return getUserProfile(uid).child("last_login");
    }

    public DatabaseReference getProfileEmail(final String uid) {
        return getUserProfile(uid).child("email");
    }

    public DatabaseReference getFirstName(final String uid) {
        return getUserProfile(uid).child("first_name");
    }

    public DatabaseReference getLastName(final String uid) {
        return getUserProfile(uid).child("last_name");
    }

    public DatabaseReference getSubjectReport(String uid) {
        return databaseReference.child(SUBJECTS_REF).child("reports").child(uid).child("report");
    }

    /**
     * Checks to see if there is a current active user.
     *
     * @return True if logged in, false otherwise.
     */
    public boolean isUserLoggedIn() {
        return null != FirebaseAuth.getInstance().getCurrentUser();
    }
}