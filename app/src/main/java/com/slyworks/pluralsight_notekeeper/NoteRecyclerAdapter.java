package com.slyworks.pluralsight_notekeeper;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

class NoteRecyclerAdapter extends RecyclerView.Adapter<NoteRecyclerAdapter.viewHolder> {
private final Context mContext;
    private final LayoutInflater mLayoutInflater;

    //Data structure for saving Notes
    private final List<NoteInfo> mNotes;

    NoteRecyclerAdapter(Context context, List<NoteInfo> notes) {
        mContext = context;
        mNotes = notes;

        //LayoutInflater requires an Activity context for instantiation
        mLayoutInflater = LayoutInflater.from(mContext);

    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
     View itemView = mLayoutInflater.inflate(R.layout.item_note_list, parent, false);

     return new viewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
    NoteInfo note = mNotes.get(position);

    holder.mTextCourse.setText(note.getCourse().getTitle());
    holder.mTextTitle.setText(note.getTitle());

    //setting the position of the note
        holder.mCurrentPosition = position;
    }

    @Override
    public int getItemCount() {
        return mNotes.size();
    }

    //creating nested class for custom ViewHolder implementation
public class viewHolder extends RecyclerView.ViewHolder{
//the view references should be public, so that the Outer class
//can access them
        public final TextView mTextCourse;
        public final TextView mTextTitle;
        public int mCurrentPosition;

        public viewHolder(@NonNull View itemView) {
        super(itemView);
            mTextCourse = itemView.findViewById(R.id.text_course);
            mTextTitle = itemView.findViewById(R.id.text_title);

            //creating click event for the entire body of the individual cardview
            //here in the constructor
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //showing the NoteActivity class once the cardview is clicked
                    //and passing the position of the note as an IntentExtra
                    Intent intent = new Intent(mContext, NoteActivity.class);
                    intent.putExtra(NoteActivity.NOTE_POSITION, mCurrentPosition);
                    mContext.startActivity(intent);
                }
            });
    }
}
}
