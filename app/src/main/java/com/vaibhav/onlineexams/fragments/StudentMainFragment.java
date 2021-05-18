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

import com.vaibhav.onlineexams.R;
import com.vaibhav.onlineexams.StudentActivity;
import com.vaibhav.onlineexams.adapters.ClassRoomRVAdapter;
import com.vaibhav.onlineexams.interfaces.OnClassRoomClickedListener;
import com.vaibhav.onlineexams.models.ClassRoom;

import java.util.ArrayList;
import java.util.Objects;

public class StudentMainFragment extends Fragment implements OnClassRoomClickedListener {

    ArrayList<ClassRoom> classRooms;
    ClassRoomRVAdapter classRoomRVAdapter;
    RecyclerView classRoomRV;
    TextView noClassRooms;

    public StudentMainFragment() {
        // Required empty public constructor
    }

    public StudentMainFragment(ArrayList<ClassRoom> classRooms) {
        this.classRooms = classRooms;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_student_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        classRoomRV = view.findViewById(R.id.rv_fsm_classroom_list);
        ((StudentActivity) requireActivity()).changeActionBarTitle(getResources().getString(R.string.classrooms));
        noClassRooms = view.findViewById(R.id.tv_fsm_no_classrooms_yet);

        if (classRooms.size() == 0) {
            noClassRooms.setVisibility(View.VISIBLE);
        }
        classRoomRVAdapter = new ClassRoomRVAdapter(classRooms, this);
        classRoomRV.setAdapter(classRoomRVAdapter);
        classRoomRV.setLayoutManager(new LinearLayoutManager(getContext()));
        classRoomRVAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClassRoomClicked(ClassRoom classRoom) {
        ClassRoomFragment fragment = new ClassRoomFragment(classRoom, R.id.fcv_student_container);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_in, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out)
                .replace(R.id.fcv_student_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}