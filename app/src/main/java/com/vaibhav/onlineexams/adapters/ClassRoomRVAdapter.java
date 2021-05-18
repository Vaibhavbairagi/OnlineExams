package com.vaibhav.onlineexams.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.vaibhav.onlineexams.R;
import com.vaibhav.onlineexams.interfaces.OnClassRoomClickedListener;
import com.vaibhav.onlineexams.models.ClassRoom;

import java.util.ArrayList;

public class ClassRoomRVAdapter extends RecyclerView.Adapter<ClassRoomRVAdapter.ClassRoomViewHolder> {

    ArrayList<ClassRoom> classRooms;
    OnClassRoomClickedListener onClassRoomClickedListener;

    public ClassRoomRVAdapter(ArrayList<ClassRoom> classRooms, OnClassRoomClickedListener onClassRoomClickedListener) {
        this.classRooms = classRooms;
        this.onClassRoomClickedListener = onClassRoomClickedListener;
    }

    @NonNull
    @Override
    public ClassRoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ClassRoomViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.classroom_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ClassRoomViewHolder holder, int position) {
        ClassRoom c = classRooms.get(position);
        holder.branchTV.setText(c.getBranch());
        holder.batchTV.setText(c.getBatch());
        holder.titleTV.setText(c.getTitle());

        //TODO: setOnclicklistener and do all the opening of fragment and all
        holder.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClassRoomClickedListener.onClassRoomClicked(c);
            }
        });
    }

    @Override
    public int getItemCount() {
        return classRooms.size();
    }

    static class ClassRoomViewHolder extends RecyclerView.ViewHolder {
        CardView parent;
        TextView titleTV, batchTV, branchTV;

        public ClassRoomViewHolder(@NonNull View itemView) {
            super(itemView);
            parent = itemView.findViewById(R.id.cv_classroom_list_item);
            titleTV = itemView.findViewById(R.id.tv_classroom_title);
            batchTV = itemView.findViewById(R.id.tv_classroom_batch);
            branchTV = itemView.findViewById(R.id.tv_classroom_branch);
        }
    }
}
