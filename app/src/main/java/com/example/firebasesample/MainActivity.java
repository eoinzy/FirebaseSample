package com.example.firebasesample;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

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
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getSimpleName();

    private ImageView mUserImage;

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
        RadioGroup deadOption = findViewById(R.id.form_option_dead);
        int deadId = deadOption.getCheckedRadioButtonId();
        RadioButton deadAnswer = findViewById(deadId);
        deadAnswer.getText();
        EditText deadExplain = findViewById(R.id.form_explain_dead);
        String deadText = deadExplain.getText().toString();

        RadioGroup catOption = findViewById(R.id.form_option_cat);
        int catId = catOption.getCheckedRadioButtonId();
        RadioButton catAnswer = findViewById(catId);
        catAnswer.getText();
        EditText catExplain = findViewById(R.id.form_explain_cat);
        String catText = catExplain.getText().toString();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();
        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        ///subjects/reports/{user_uid}/reports/{report_name}/{report_timestamp}/{object_data}
        myRef.child("subjects").child("reports").child(currentUser.getUid()).child("reports").child("deadUser").child(getCurrentTimeStamp()).setValue(deadAnswer.getText());
        myRef.child("subjects").child("reports").child(currentUser.getUid()).child("reports").child("deadUser").child("reason").setValue(deadText);
        myRef.child("subjects").child("reports").child(currentUser.getUid()).child("reports").child("catUser").child(getCurrentTimeStamp()).setValue(catAnswer.getText());
        myRef.child("subjects").child("reports").child(currentUser.getUid()).child("reports").child("catUser").child("reason").setValue(catText);

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
                Toast.makeText(MainActivity.this, "Failed to upload image to Firebase", Toast.LENGTH_LONG).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "UploadTask::SuccessListener::onSuccess");

                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference myRef = database.getReference();
                        myRef.child("subjects").child("reports").child(currentUser.getUid()).child("reports").child("email").setValue(currentUser.getEmail());
                        myRef.child("subjects").child("reports").child(currentUser.getUid()).child("reports").child("profileImage").setValue(uri.toString());
                        myRef.child("subjects").child("reports").child(currentUser.getUid()).child("reports").child("timeStamp").setValue(getCurrentTimeStamp());

                        Toast.makeText(MainActivity.this, "Data successfully uploaded", Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "\"UploadTask::SuccessListener::onFailure");
                    }
                });
            }
        });
    }

    private String getCurrentTimeStamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_FORMAT, Locale.ENGLISH);
        return dateFormat.format(new Date());
    }

    private static final int READ_IMAGE = 234;

    private void checkImagePermission() {
        if (Build.VERSION.SDK_INT > 23) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Requesting permission for READ_EXTERNAL_STORAGE");
                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, READ_IMAGE);
            }
        }

        loadImage();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == READ_IMAGE) {
            Log.d(TAG, "Permission: " + grantResults[0]);
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadImage();
            } else {
                Toast.makeText(this, "Cannot access your image gallery", Toast.LENGTH_LONG).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private static final int PICK_IMAGE = 342;

    private void loadImage() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
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
        String[] split = email.split("@");
        return split[0];
    }

    private void setupForm() {
        RadioGroup deadGroup = findViewById(R.id.form_option_dead);
        deadGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                switch (checkedId) {
                    case R.id.form_option_dead_yes:
                        findViewById(R.id.form_explain_dead).setVisibility(View.GONE);
                        break;
                    case R.id.form_option_dead_no:
                        findViewById(R.id.form_explain_dead).setVisibility(View.VISIBLE);
                        break;
                }
            }
        });

        RadioGroup catGroup = findViewById(R.id.form_option_cat);
        catGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                switch (checkedId) {
                    case R.id.form_option_cat_yes:
                        findViewById(R.id.form_explain_cat).setVisibility(View.GONE);
                        break;
                    case R.id.form_option_cat_no:
                        findViewById(R.id.form_explain_cat).setVisibility(View.VISIBLE);
                        break;
                }
            }
        });
    }

    public void logOut(View view) {
        FirebaseAuth.getInstance().signOut();
        finish();
    }
}
