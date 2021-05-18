package com.vaibhav.onlineexams;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.vaibhav.onlineexams.fragments.StudentMainFragment;
import com.vaibhav.onlineexams.models.ClassRoom;
import com.vaibhav.onlineexams.models.Student;

import java.util.ArrayList;
import java.util.Objects;

public class StudentActivity extends AppCompatActivity {

    RelativeLayout fullScreenProgressParent;
    String instituteName, rollNo, graduationYear, userId;
    SharedPreferences appPrefs;

    ArrayList<ClassRoom> classRooms = new ArrayList<>();
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;
    ArrayList<Student> students = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);

        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        appPrefs = getSharedPreferences(MyConstants.APP_PREFS, Context.MODE_PRIVATE);
        fullScreenProgressParent = findViewById(R.id.fullscreen_progress_layout);

        instituteName = appPrefs.getString(MyConstants.INSTITUTE_NAME, "");
        rollNo = appPrefs.getString(MyConstants.STUDENT_ROLL_NO, "");
        graduationYear = appPrefs.getString(MyConstants.STUDENT_GRADUATION_YEAR, "");
        userId = firebaseAuth.getUid();

        fetchMyClassRoomsList();

    }

    private void fetchMyClassRoomsList() {
        fullScreenProgressParent.setVisibility(View.VISIBLE);
        firestore.collection(MyConstants.CLASSROOMS)
                .whereArrayContains(MyConstants.STUDENT_IDS, userId)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                Log.d("fetched", "class list");
                fullScreenProgressParent.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    if (task.getResult() != null) {
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            ClassRoom classRoom = documentSnapshot.toObject(ClassRoom.class);
                            classRooms.add(classRoom);
                        }
                    }
                    openStudentClassMainFragment();
                } else {
                    Toast.makeText(getApplicationContext(), Objects.requireNonNull(task.getException()).getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void openStudentClassMainFragment() {
        StudentMainFragment fragment = new StudentMainFragment(classRooms);
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out)
                .add(R.id.fcv_student_container, fragment)
                .setReorderingAllowed(true)
                .commit();
    }

    public void changeActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            firebaseAuth.signOut();
            appPrefs.edit().clear().apply();
            startActivity(new Intent(StudentActivity.this, LoginSignUpActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}