package com.vaibhav.onlineexams.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.vaibhav.onlineexams.MyConstants;
import com.vaibhav.onlineexams.R;
import com.vaibhav.onlineexams.StudentActivity;
import com.vaibhav.onlineexams.TeacherActivity;
import com.vaibhav.onlineexams.adapters.ExamListRVAdapter;
import com.vaibhav.onlineexams.interfaces.OnExamClickedListener;
import com.vaibhav.onlineexams.interfaces.OnExamCreatedListener;
import com.vaibhav.onlineexams.models.ClassRoom;
import com.vaibhav.onlineexams.models.Exam;
import com.vaibhav.onlineexams.models.Student;

import java.util.ArrayList;
import java.util.Objects;

public class ClassRoomFragment extends Fragment implements OnExamClickedListener, OnExamCreatedListener {
    ArrayList<Student> students = new ArrayList<>(), studentList;
    ClassRoom classRoom;
    ArrayList<Exam> exams = new ArrayList<>();

    RelativeLayout fullScreenProgress;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;

    Button viewParticipantsBtn;
    //TODO: implement view participant functionality

    FloatingActionButton addNewExam;
    int containerId;

    RecyclerView examListRV;
    ExamListRVAdapter examListRVAdapter;

    SharedPreferences appPrefs;
    int userType = -1;

    TextView noClassroomTV;

    public ClassRoomFragment() {
    }

    public ClassRoomFragment(ClassRoom classRoom, ArrayList<Student> students, int containerId) {
        this.classRoom = classRoom;
        this.studentList = students;
        students.addAll(studentList);
        this.containerId = containerId;
    }

    public ClassRoomFragment(ClassRoom classRoom, int containerId) {
        this.classRoom = classRoom;
        this.containerId = containerId;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    private void fetchExamList() {
        fullScreenProgress.setVisibility(View.VISIBLE);
        firestore.collection(MyConstants.EXAMS)
                .whereEqualTo(MyConstants.CLASSROOM_ID, classRoom.getClassroomId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        fullScreenProgress.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            if (task.getResult() != null) {
                                exams.clear();
                                for (QueryDocumentSnapshot d : task.getResult()) {
                                    Exam exam = d.toObject(Exam.class);
                                    exams.add(exam);
                                }
                                if (examListRVAdapter != null) {
                                    noClassroomTV.setVisibility(View.GONE);
                                    examListRVAdapter.notifyDataSetChanged();
                                }
                                Log.d("fetched", "Exam list fetched for classroom " + classRoom.getTitle());
                            } else {
                                showToast("No exams scheduled yet");
                            }
                        } else {
                            showToast("No exams are there");
                        }
                    }
                });
    }

    private void showToast(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_class_room, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fullScreenProgress = view.findViewById(R.id.fullscreen_progress_layout);
        examListRV = view.findViewById(R.id.rv_exam_list);
        addNewExam = view.findViewById(R.id.fab_add_new_exam);
        viewParticipantsBtn = view.findViewById(R.id.btn_fcrt_view_participants);

        noClassroomTV = view.findViewById(R.id.tv_fcr_no_classroom);

        if (exams.size() == 0) {
            noClassroomTV.setVisibility(View.VISIBLE);
        }

        examListRVAdapter = new ExamListRVAdapter(exams, this);

        examListRV.setAdapter(examListRVAdapter);
        examListRV.setLayoutManager(new LinearLayoutManager(getContext()));
        examListRVAdapter.notifyDataSetChanged();

        appPrefs = requireActivity().getSharedPreferences(MyConstants.APP_PREFS, Context.MODE_PRIVATE);

        userType = appPrefs.getInt(MyConstants.USER_TYPE, -1);
        if (userType == MyConstants.USER_TYPE_STUDENT) {
            addNewExam.setVisibility(View.GONE);
            ((StudentActivity) requireActivity()).getSupportActionBar().setTitle(classRoom.getTitle());
        } else {
            ((TeacherActivity) requireActivity()).getSupportActionBar().setTitle(classRoom.getTitle());
        }

        fetchExamList();

        addNewExam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCreateExamFragment();
            }
        });
    }

    private void openCreateExamFragment() {
        CreateExamFragment fragment = new CreateExamFragment(classRoom.getClassroomId(), this);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_in, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out)
                .replace(containerId, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onExamClick(Exam e) {
        ExamFragment fragment = new ExamFragment(e);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_in, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out)
                .replace(containerId, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onExamCreated(Exam e) {
        noClassroomTV.setVisibility(View.GONE);
        examListRVAdapter.notifyDataSetChanged();
    }
}