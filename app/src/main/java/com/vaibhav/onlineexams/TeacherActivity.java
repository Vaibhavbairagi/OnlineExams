package com.vaibhav.onlineexams;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.vaibhav.onlineexams.fragments.TeacherMainFragment;
import com.vaibhav.onlineexams.models.ClassRoom;
import com.vaibhav.onlineexams.models.Student;

import java.util.ArrayList;
import java.util.Objects;

public class TeacherActivity extends AppCompatActivity {

    ArrayList<Student> students = new ArrayList<>();
    FirebaseFirestore firestore;

    RelativeLayout fullScreenProgressParent;
    String instituteName;
    SharedPreferences appPrefs;

    ArrayList<ClassRoom> classRooms = new ArrayList<>();
    FirebaseAuth firebaseAuth;

    boolean isClassRoomListFetched = false, isStudentListFetched = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        appPrefs = getSharedPreferences(MyConstants.APP_PREFS, Context.MODE_PRIVATE);
        setContentView(R.layout.activity_teacher);
        fullScreenProgressParent = findViewById(R.id.fullscreen_progress_layout);

        instituteName = appPrefs.getString(MyConstants.INSTITUTE_NAME, "");

        fetchStudentsList();
        fetchMyClassRoomsList();

        //attachSnapshotListenerOnStudentsData();
    }

    private void attachSnapshotListenerOnStudentsData() {
        CollectionReference studentCollectionsReference = firestore.collection(MyConstants.STUDENTS);
        studentCollectionsReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.d("StudentSnapException", error.getLocalizedMessage());
                } else if (value!=null){
                    students.clear();
                    for (DocumentSnapshot d :  value.getDocuments()) {
                        Student s = d.toObject(Student.class);
                        if (s != null && s.getInstituteName().equals(appPrefs.getString(MyConstants.INSTITUTE_NAME, ""))) {
                            students.add(s);
                        }
                    }
                }
            }
        });
    }

    private void fetchMyClassRoomsList() {
        fullScreenProgressParent.setVisibility(View.VISIBLE);
        firestore.collection(MyConstants.CLASSROOMS)
                .whereEqualTo(MyConstants.TEACHER_ID, firebaseAuth.getUid())
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                isClassRoomListFetched = true;
                Log.d("fetched", "class list");

                
                fullScreenProgressParent.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    if (task.getResult() != null) {
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            ClassRoom classRoom = documentSnapshot.toObject(ClassRoom.class);
                            classRooms.add(classRoom);
                        }
                    }
                } else {
                    Toast.makeText(getApplicationContext(), Objects.requireNonNull(task.getException()).getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void fetchStudentsList() {
        fullScreenProgressParent.setVisibility(View.VISIBLE);
        firestore.collection(MyConstants.STUDENTS)
                .whereEqualTo(MyConstants.INSTITUTE_NAME, instituteName)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                isStudentListFetched = true;
                Log.d("fetched", "student list");
                fullScreenProgressParent.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    if (task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Student student = document.toObject(Student.class);
                            students.add(student);
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "No classroom found", Toast.LENGTH_SHORT).show();
                    }
                    openTeacherMainFragment();
                } else {
                    Toast.makeText(getApplicationContext(), Objects.requireNonNull(task.getException()).getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void openTeacherMainFragment() {
        TeacherMainFragment fragment = new TeacherMainFragment(students, classRooms);
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out)
                .add(R.id.fcv_teacher_container, fragment)
                .setReorderingAllowed(true)
                .commit();
    }

    public void changeActionBarTitle(String title) {
        Objects.requireNonNull(getSupportActionBar()).setTitle(title);
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
            startActivity(new Intent(TeacherActivity.this, LoginSignUpActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}