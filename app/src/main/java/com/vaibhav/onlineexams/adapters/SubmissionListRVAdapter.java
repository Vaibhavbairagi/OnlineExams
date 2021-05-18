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
import com.vaibhav.onlineexams.interfaces.OnSubmissionClickedListener;
import com.vaibhav.onlineexams.models.Exam;
import com.vaibhav.onlineexams.models.Submission;

import java.util.ArrayList;

public class SubmissionListRVAdapter extends RecyclerView.Adapter<SubmissionListRVAdapter.SubmissionListItemViewHolder> {
    ArrayList<Submission> submissions;
    OnSubmissionClickedListener onSubmissionClickedListener;

    public SubmissionListRVAdapter() {
    }

    public SubmissionListRVAdapter(ArrayList<Submission> submissions, OnSubmissionClickedListener onSubmissionClickedListener) {
        this.submissions = submissions;
        this.onSubmissionClickedListener = onSubmissionClickedListener;
    }

    @NonNull
    @Override
    public SubmissionListItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SubmissionListItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.submission_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SubmissionListItemViewHolder holder, int position) {
        Submission s = submissions.get(position);
        holder.rollNoTV.setText(s.getRollNo());

        holder.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSubmissionClickedListener.onSubmissionClicked(s);
            }
        });
    }

    @Override
    public int getItemCount() {
        return submissions.size();
    }

    static class SubmissionListItemViewHolder extends RecyclerView.ViewHolder {
        CardView parent;
        TextView rollNoTV;

        public SubmissionListItemViewHolder(@NonNull View itemView) {
            super(itemView);
            parent = itemView.findViewById(R.id.cv_subli_parent);
            rollNoTV = itemView.findViewById(R.id.tv_subli_roll_no);
        }
    }
}
