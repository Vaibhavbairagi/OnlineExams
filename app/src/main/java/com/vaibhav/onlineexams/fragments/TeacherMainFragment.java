package com.vaibhav.onlineexams.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.vaibhav.onlineexams.R;
import com.vaibhav.onlineexams.TeacherActivity;
import com.vaibhav.onlineexams.adapters.ClassRoomRVAdapter;
import com.vaibhav.onlineexams.interfaces.OnClassRoomClickedListener;
import com.vaibhav.onlineexams.interfaces.OnClassRoomCreatedListener;
import com.vaibhav.onlineexams.models.ClassRoom;
import com.vaibhav.onlineexams.models.Student;

import java.util.ArrayList;

public class TeacherMainFragment extends Fragment implements OnClassRoomCreatedListener, OnClassRoomClickedListener {

    FloatingActionButton createNewClassRoomFAB;
    ArrayList<Student> students;
    ArrayList<ClassRoom> classRooms;
    ClassRoomRVAdapter classRoomRVAdapter;
    RecyclerView classRoomRV;
    TextView noClassroomTV;

    public TeacherMainFragment(ArrayList<Student> students, ArrayList<ClassRoom> classRooms) {
        this.students = students;
        this.classRooms = classRooms;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_teacher_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        createNewClassRoomFAB = view.findViewById(R.id.fab_create_new_classroom);
        classRoomRV = view.findViewById(R.id.rv_ftcr_classroom_list);
        ((TeacherActivity) requireActivity()).changeActionBarTitle(getResources().getString(R.string.classrooms));
        noClassroomTV = view.findViewById(R.id.tv_ftm_no_classroom);

        if (classRooms.size() == 0) {
            noClassroomTV.setVisibility(View.VISIBLE);
        }

        classRoomRVAdapter = new ClassRoomRVAdapter(classRooms, this);
        classRoomRV.setAdapter(classRoomRVAdapter);
        classRoomRV.setLayoutManager(new LinearLayoutManager(getContext()));
        classRoomRVAdapter.notifyDataSetChanged();
        createNewClassRoomFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCreateNewClassRoomFragment();
            }
        });
    }

    private void openCreateNewClassRoomFragment() {
        CreateNewClassRoomFragment fragment = new CreateNewClassRoomFragment(students, this);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_in, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out)
                .replace(R.id.fcv_teacher_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onClassRoomCreated(ClassRoom classRoom) {
        classRooms.add(classRoom);
        classRoomRVAdapter.notifyDataSetChanged();
        Toast.makeText(getContext(), "Classroom created", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClassRoomClicked(ClassRoom classRoom) {
        ClassRoomFragment fragment = new ClassRoomFragment(classRoom, students, R.id.fcv_teacher_container);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_in, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out)
                .replace(R.id.fcv_teacher_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}