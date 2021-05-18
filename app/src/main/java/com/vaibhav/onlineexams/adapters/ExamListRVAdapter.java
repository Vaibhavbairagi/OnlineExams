package com.vaibhav.onlineexams.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.vaibhav.onlineexams.R;
import com.vaibhav.onlineexams.interfaces.OnExamClickedListener;
import com.vaibhav.onlineexams.models.Exam;

import java.util.ArrayList;

public class ExamListRVAdapter extends RecyclerView.Adapter<ExamListRVAdapter.ExamItemListViewHolder> {

    ArrayList<Exam> exams;
    OnExamClickedListener onExamClickedListener;

    public ExamListRVAdapter() {
    }

    public ExamListRVAdapter(ArrayList<Exam> exams, OnExamClickedListener onExamClickedListener) {
        this.exams = exams;
        this.onExamClickedListener = onExamClickedListener;
    }

    @NonNull
    @Override
    public ExamItemListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ExamItemListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.exam_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ExamItemListViewHolder holder, int position) {
        Exam e = exams.get(position);
        holder.startTimeTV.setText("Starts at: " + e.getStartTime());
        holder.endTimeTV.setText("Due: " + e.getEndTime());
        holder.titleTV.setText(e.getExamTitle());

        holder.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onExamClickedListener.onExamClick(e);
            }
        });
    }

    @Override
    public int getItemCount() {
        return exams.size();
    }

    static class ExamItemListViewHolder extends RecyclerView.ViewHolder {
        CardView parent;
        TextView titleTV, startTimeTV, endTimeTV;

        public ExamItemListViewHolder(@NonNull View itemView) {
            super(itemView);
            parent = itemView.findViewById(R.id.cv_exam_list_item_parent);
            titleTV = itemView.findViewById(R.id.tv_exam_title);
            startTimeTV = itemView.findViewById(R.id.tv_exam_start_time);
            endTimeTV = itemView.findViewById(R.id.tv_exam_end_time);
        }
    }
}
