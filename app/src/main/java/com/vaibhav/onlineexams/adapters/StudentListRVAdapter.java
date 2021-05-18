package com.vaibhav.onlineexams.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.vaibhav.onlineexams.R;
import com.vaibhav.onlineexams.interfaces.StudentAddClickListener;
import com.vaibhav.onlineexams.models.Student;

import java.util.ArrayList;

public class StudentListRVAdapter extends RecyclerView.Adapter<StudentListRVAdapter.StudentListItemViewHolder> {

    public ArrayList<Student> students = new ArrayList<>();
    public ArrayList<Student> filterList  = new ArrayList<>();
    boolean deleteOnTouch;
    StudentAddClickListener studentAddClickListener;

    public StudentListRVAdapter(ArrayList<Student> students, boolean deleteOnTouch) {
        this.students.addAll(students);
        this.filterList = students;
        this.deleteOnTouch = deleteOnTouch;
    }

    public StudentListRVAdapter(ArrayList<Student> students, boolean deleteOnTouch, StudentAddClickListener studentAddClickListener) {
        this.students.addAll(students);
        this.filterList.addAll(students);
        this.deleteOnTouch = deleteOnTouch;
        this.studentAddClickListener = studentAddClickListener;
    }

    public StudentListRVAdapter(ArrayList<Student> students, ArrayList<Student> studentArrayList, boolean deleteOnTouch, StudentAddClickListener studentAddClickListener) {
        this.students.addAll(students);
        this.filterList = students;
        this.deleteOnTouch = deleteOnTouch;
        this.studentAddClickListener = studentAddClickListener;
    }


    @NonNull
    @Override
    public StudentListItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new StudentListItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.student_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull StudentListItemViewHolder holder, int position) {
        Student s = filterList.get(position);
        holder.nameTV.setText(s.getName());
        holder.rollNoTV.setText(s.getRollNo());
        holder.branchTV.setText(s.getBranch());
        if (deleteOnTouch) {
            holder.parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    studentAddClickListener.onStudentAddClick(s);
                    filterList.remove(position);
                    notifyDataSetChangedToThis();
                }
            });
        }
    }

    private void notifyDataSetChangedToThis() {
        this.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return filterList.size();
    }

    public void filterListWithQuery(String query) {
        filterList.clear();
        if (query.isEmpty()) {
            filterList.addAll(students);
        } else {
            for (Student s : students) {
                if (s.getRollNo().toLowerCase().contains(query.toLowerCase()) || s.getName().toLowerCase().contains(query.toLowerCase())) {
                    filterList.add(s);
                }
            }
        }
        Log.d("fetched", "inside adapter" + filterList.size());
        Log.d("fetched", "inside adapter ssize: " + students.size());
        this.notifyDataSetChanged();
    }

    static class StudentListItemViewHolder extends RecyclerView.ViewHolder {
        TextView nameTV, rollNoTV, branchTV;
        CardView parent;

        public StudentListItemViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTV = itemView.findViewById(R.id.tv_stu_li_name);
            rollNoTV = itemView.findViewById(R.id.tv_stu_li_roll);
            branchTV = itemView.findViewById(R.id.tv_stu_li_branch);
            parent = itemView.findViewById(R.id.cv_stu_li_parent);
        }
    }
}
