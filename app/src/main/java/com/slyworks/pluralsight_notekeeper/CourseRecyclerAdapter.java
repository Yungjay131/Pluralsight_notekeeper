package com.slyworks.pluralsight_notekeeper;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

class CourseRecyclerAdapter extends RecyclerView.Adapter<CourseRecyclerAdapter.viewHolder> {
private final Context mContext;
    private final LayoutInflater mLayoutInflater;

    //Data structure for saving Notes
    private final List<CourseInfo> mCourses;

    CourseRecyclerAdapter(Context context, List<CourseInfo> notes) {
        mContext = context;
        mCourses = notes;

        //LayoutInflater requires an Activity context for instantiation
        mLayoutInflater = LayoutInflater.from(mContext);

    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
     View itemView = mLayoutInflater.inflate(R.layout.item_course_list, parent, false);

     return new viewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
    CourseInfo course = mCourses.get(position);

    holder.mTextCourse.setText(course.getTitle());

    //setting the position of the note
        holder.mCurrentPosition = position;
    }

    @Override
    public int getItemCount() {
        return mCourses.size();
    }

    //creating nested class for custom ViewHolder implementation
public class viewHolder extends RecyclerView.ViewHolder{
//the view references should be public, so that the Outer class
//can access them
        public final TextView mTextCourse;
        public int mCurrentPosition;

        public viewHolder(@NonNull View itemView) {
        super(itemView);
            mTextCourse = itemView.findViewById(R.id.text_course);


            //creating click event for the entire body of the individual cardview
            //here in the constructor
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //showing the NoteActivity class once the cardView is clicked
                    //and passing the position of the note as an IntentExtra
                 /*
                    Intent intent = new Intent(mContext, NoteActivity.class);
                    intent.putExtra(NoteActivity.NOTE_POSITION, mCurrentPosition);
                    mContext.startActivity(intent);
                  */

                    Snackbar.make(v,mCourses.get(mCurrentPosition).getTitle() ,Snackbar.LENGTH_LONG).show();
                }
            });
    }
}
}
