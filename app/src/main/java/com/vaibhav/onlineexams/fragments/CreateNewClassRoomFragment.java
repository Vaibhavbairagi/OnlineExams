package com.vaibhav.onlineexams.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.vaibhav.onlineexams.MyConstants;
import com.vaibhav.onlineexams.R;
import com.vaibhav.onlineexams.TeacherActivity;
import com.vaibhav.onlineexams.adapters.StudentListRVAdapter;
import com.vaibhav.onlineexams.interfaces.OnClassRoomCreatedListener;
import com.vaibhav.onlineexams.interfaces.StudentAddClickListener;
import com.vaibhav.onlineexams.models.ClassRoom;
import com.vaibhav.onlineexams.models.Student;

import java.util.ArrayList;
import java.util.Objects;

public class CreateNewClassRoomFragment extends Fragment implements StudentAddClickListener {

    FirebaseFirestore firestore;
    ArrayList<Student> studentsList, students = new ArrayList<>();
    SearchView searchView;
    RecyclerView studentListRV;
    EditText titleET, branchET, batchET;
    Button createClassRoomBtn, addAllStudentsFromBranchBtn, addStudentsBtn;
    RelativeLayout addStudentsParent, fullScreenProgress;
    CardView takeSubDetailsParent;
    String branch, title, batch;
    StudentListRVAdapter adapter;
    ArrayList<String> studentInClassRoomIdList = new ArrayList<>();
    FirebaseAuth firebaseAuth;
    OnClassRoomCreatedListener onClassRoomCreatedListener;

    public CreateNewClassRoomFragment(ArrayList<Student> students, OnClassRoomCreatedListener onClassRoomCreatedListener) {
        this.studentsList = students;
        this.students.addAll(students);
        this.onClassRoomCreatedListener = onClassRoomCreatedListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_new_class_room, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((TeacherActivity) requireActivity()).changeActionBarTitle(getResources().getString(R.string.create_classroom));

        searchView = view.findViewById(R.id.sv_fcncr_search_student);
        studentListRV = view.findViewById(R.id.rv_fcncr_student_list);
        titleET = view.findViewById(R.id.et_fcncr_classroom_title);
        branchET = view.findViewById(R.id.et_fcncr_classroom_branch);
        createClassRoomBtn = view.findViewById(R.id.btn_fcncr_create_classroom);
        addAllStudentsFromBranchBtn = view.findViewById(R.id.btn_fcncr_add_all_branch_students);
        addStudentsBtn = view.findViewById(R.id.btn_fcncr_add_students);
        takeSubDetailsParent = view.findViewById(R.id.cv_fcncr_take_subject_details_parent);
        addStudentsParent = view.findViewById(R.id.rl_fcncr_add_students_parent);
        batchET = view.findViewById(R.id.et_fcncr_classroom_batch);
        fullScreenProgress = view.findViewById(R.id.fullscreen_progress_layout);

        addStudentsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                branch = branchET.getText().toString();
                title = titleET.getText().toString();
                batch = batchET.getText().toString();
                if (title.isEmpty()) {
                    showToast("Please enter classroom title");
                } else if (branch.isEmpty()) {
                    showToast("Please enter branch");
                } else if (batch.isEmpty()) {
                    showToast("Please enter batch");
                } else {
                    takeSubDetailsParent.setVisibility(View.GONE);
                    addStudentsParent.setVisibility(View.VISIBLE);
                    addStudentsToClassRoom();
                }
            }
        });
    }

    private void addStudentsToClassRoom() {
        Log.d("fetched","size: "+studentsList.size());
        adapter = new StudentListRVAdapter(students, true, this);
        studentListRV.setAdapter(adapter);
        studentListRV.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter.notifyDataSetChanged();
        addAllStudentsFromBranchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < students.size(); i++) {
                    if (students.get(i).getBranch().equals(branch) && String.valueOf(students.get(i).getGraduationYear()).equals(batch)) {
                        studentInClassRoomIdList.add(students.get(i).getUid());
                        students.remove(i);
                        i--;
                    }
                }
                adapter.students.clear();
                adapter.students.addAll(students);
                adapter.filterList = students;
                adapter.notifyDataSetChanged();
            }
        });


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d("fetched", "inside fragment" + query);
                adapter.filterListWithQuery(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d("fetched", "inside fragment" + newText);
                adapter.filterListWithQuery(newText);
                return false;
            }
        });

        createClassRoomBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullScreenProgress.setVisibility(View.VISIBLE);
                String t = title;
                t = t.replace(" ", "");
                String classroomID = t + batch + branch + firebaseAuth.getUid();
                Log.d("fetched", studentInClassRoomIdList.toString());
                ClassRoom classRoom = new ClassRoom(firebaseAuth.getUid(), branch, batch, title, studentInClassRoomIdList, classroomID);
                DocumentReference documentReference = firestore.collection(MyConstants.CLASSROOMS).document(classroomID);
                documentReference.set(classRoom).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        fullScreenProgress.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            onClassRoomCreatedListener.onClassRoomCreated(classRoom);
                            requireActivity().onBackPressed();
                        } else {
                            showToast(Objects.requireNonNull(task.getException()).getMessage());
                        }
                    }
                });
            }
        });

    }

    public void showToast(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStudentAddClick(Student s) {
        studentInClassRoomIdList.add(s.getUid());
    }
}