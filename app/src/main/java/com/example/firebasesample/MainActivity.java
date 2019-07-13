package com.example.firebasesample;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.firebasesample.data.FirebaseHandler;
import com.example.firebasesample.util.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getSimpleName();

    private ImageView mUserImage;

    private static final int READ_IMAGE_CODE = 234;
    private static final int PICK_IMAGE_CODE = 342;

    private static final String TIME_FORMAT = "ddMMyyHHmmss";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        TextView txtUserEmail = findViewById(R.id.user_email);
        txtUserEmail.setText(currentUser.getEmail());

        mUserImage = findViewById(R.id.user_image);
        mUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkImagePermission();
            }
        });

        setupForm();
    }

    public void saveData(View view) {
        storeDataInFirebase();
    }

    private void storeDataInFirebase() {
        //Get checkbox data
        RadioGroup firstOption = findViewById(R.id.form_option_one);
        int firstId = firstOption.getCheckedRadioButtonId();
        RadioButton firstAnswer = findViewById(firstId);
        firstAnswer.getText();
        EditText firstReason = findViewById(R.id.form_reason_one);
        String firstReasonText = firstReason.getText().toString();

        RadioGroup secondOption = findViewById(R.id.form_option_two);
        int secondId = secondOption.getCheckedRadioButtonId();
        RadioButton secondAnswer = findViewById(secondId);
        secondAnswer.getText();
        EditText secondReason = findViewById(R.id.form_reason_two);
        String secondReasonText = secondReason.getText().toString();

        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        ///subjects/reports/{user_uid}/report/{report_name}/{report_timestamp}/{object_data}

        FirebaseHandler fbHandler = new FirebaseHandler();
        fbHandler.getSubjectReport(currentUser.getUid()).child("question_one").child("question").setValue(getString(R.string.question_one));
        fbHandler.getSubjectReport(currentUser.getUid()).child("question_one").child("answer").setValue(firstAnswer.getText());
        fbHandler.getSubjectReport(currentUser.getUid()).child("question_one").child("reason").setValue(firstReasonText);
        fbHandler.getSubjectReport(currentUser.getUid()).child("question_two").child("question").setValue(getString(R.string.question_two));
        fbHandler.getSubjectReport(currentUser.getUid()).child("question_two").child("answer").setValue(secondAnswer.getText());
        fbHandler.getSubjectReport(currentUser.getUid()).child("question_two").child("reason").setValue(secondReasonText);

        storeImageInFirebase(currentUser);
    }

    private void storeImageInFirebase(final FirebaseUser currentUser) {
        String email = currentUser.getEmail();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl("gs://prowork-e1b6f.appspot.com");

        SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_FORMAT, Locale.ENGLISH);
        Date date = new Date();
        String imagePath = splitEmail(email) + "." + dateFormat.format(date) + ".jpg";
        final StorageReference imageRef = storageRef.child("images/" + imagePath);
        mUserImage.isDrawingCacheEnabled();
        mUserImage.buildDrawingCache();

        BitmapDrawable bmpDrawable = (BitmapDrawable) mUserImage.getDrawable();
        Bitmap imgBitmap = bmpDrawable.getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imgBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        UploadTask uploadTask = imageRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Image upload failed", e);
                Snackbar.make(mUserImage, "Image upload failed: " + e.getLocalizedMessage(), Snackbar.LENGTH_LONG).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "UploadTask::SuccessListener::onSuccess");

                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        FirebaseHandler fbh = new FirebaseHandler();
                        fbh.getSubjectReport(currentUser.getUid()).child("email").setValue(currentUser.getEmail());
                        fbh.getSubjectReport(currentUser.getUid()).child("profile_image").setValue(uri.toString());
                        fbh.getSubjectReport(currentUser.getUid()).child("time_stamp").setValue(getCurrentTimeStamp());

                        Toast.makeText(MainActivity.this, "Data successfully uploaded", Toast.LENGTH_LONG).show();
                        if(!isFinishing() && !isDestroyed()) {
                            launchProfileScreen();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "UploadTask::SuccessListener::onFailure", e);
                    }
                });
            }
        }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                Log.d(TAG, "In OnCompleteListener");
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "In OnProgressListener");
            }
        });

        if(!Utils.hasNetworkConnection(this)) {
            launchProfileScreen();
        }
    }

    private void launchProfileScreen() {
        //Launch activity to view data
        Intent i = new Intent(MainActivity.this, ProfileActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    private String getCurrentTimeStamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_FORMAT, Locale.ENGLISH);
        return dateFormat.format(new Date());
    }

    private void checkImagePermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Requesting permission for READ_EXTERNAL_STORAGE");
                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, READ_IMAGE_CODE);
            }
        }

        loadImage();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == READ_IMAGE_CODE) {
            Log.d(TAG, "Permission: " + grantResults[0]);
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadImage();
            } else {
                Log.e(TAG, "Failed to access local image library");
                Toast.makeText(this, "Cannot access your image gallery", Toast.LENGTH_LONG).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void loadImage() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, PICK_IMAGE_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_CODE && resultCode == Activity.RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            String[] filePath = new String[]{MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(imageUri, filePath, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePath[0]);
            String imagePath = cursor.getString(columnIndex);
            cursor.close();
            mUserImage.setImageBitmap(BitmapFactory.decodeFile(imagePath));
        }
    }

    /**
     * Splits the email into the username (before the "@") and everything after.
     *
     * @param email The email to split.
     * @return A string value of the email address, before the "@".
     */
    private String splitEmail(String email) {
        if (email.isEmpty()) {
            return email;
        }
        String[] split = email.split("@");
        return split[0];
    }

    private void setupForm() {
        RadioGroup firstGroup = findViewById(R.id.form_option_one);
        firstGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                switch (checkedId) {
                    case R.id.form_option_one_yes:
                        findViewById(R.id.form_reason_one).setVisibility(View.GONE);
                        break;
                    case R.id.form_option_one_no:
                        findViewById(R.id.form_reason_one).setVisibility(View.VISIBLE);
                        break;
                }
            }
        });

        RadioGroup catGroup = findViewById(R.id.form_option_two);
        catGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                switch (checkedId) {
                    case R.id.form_option_two_yes:
                        findViewById(R.id.form_reason_two).setVisibility(View.GONE);
                        break;
                    case R.id.form_option_two_no:
                        findViewById(R.id.form_reason_two).setVisibility(View.VISIBLE);
                        break;
                }
            }
        });
    }

    public void logOut(View view) {
        FirebaseAuth.getInstance().signOut();
        Intent i = new Intent(this, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }
}
