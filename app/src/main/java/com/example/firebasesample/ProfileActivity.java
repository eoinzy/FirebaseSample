package com.example.firebasesample;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.firebasesample.data.FirebaseHandler;
import com.example.firebasesample.model.Report;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {
    private final static String TAG = ProfileActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_data);

        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseHandler fbh = new FirebaseHandler();
        DatabaseReference ref = fbh.getSubjectReport(currentUser.getUid());
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    buildUI(dataSnapshot);
                } else {
                    launchFormScreen();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to get value from DB", databaseError.toException());
            }
        });
    }

    private void launchFormScreen() {
        Intent i = new Intent(this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }

    private void buildUI(DataSnapshot dataSnapshot) {
        Report report = dataSnapshot.getValue(Report.class);

        ((TextView) findViewById(R.id.view_email)).setText(report.getEmail());
        ((TextView) findViewById(R.id.view_question_one)).setText(report.getQuestion_one().getQuestion());
        ((TextView) findViewById(R.id.view_answer_one)).setText(report.getQuestion_one().getAnswer());
        ((TextView) findViewById(R.id.view_reason_one)).setText(report.getQuestion_one().getReason());
        ((TextView) findViewById(R.id.view_question_two)).setText(report.getQuestion_two().getQuestion());
        ((TextView) findViewById(R.id.view_answer_two)).setText(report.getQuestion_two().getAnswer());
        ((TextView) findViewById(R.id.view_reason_two)).setText(report.getQuestion_two().getReason());
        Picasso.get().load(report.getProfile_image()).into((ImageView) findViewById(R.id.view_image));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_edit) {
            launchFormScreen();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
