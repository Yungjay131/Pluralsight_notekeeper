package com.slyworks.pluralsight_notekeeper;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.slyworks.pluralsight_notekeeper.Database.NoteKeeperDatabaseContract;
import com.slyworks.pluralsight_notekeeper.Database.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.slyworks.pluralsight_notekeeper.Database.NoteKeeperDatabaseContract.NoteInfoEntry;

import java.util.List;

class NoteRecyclerAdapter extends RecyclerView.Adapter<NoteRecyclerAdapter.viewHolder> {
private final Context mContext;
    private final LayoutInflater mLayoutInflater;

    //Data structure for saving Notes
    //private final List<NoteInfo> mNotes;

    private Cursor mCursor;
    private int mCoursePos;
    private int mNoteTitlePos;
    private int mIDPos;

    NoteRecyclerAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;

        //LayoutInflater requires an Activity context for instantiation
        mLayoutInflater = LayoutInflater.from(mContext);

        //database
        populateColumnPositions();
    }

    private void populateColumnPositions() {
        //checking if cursor is null
        if(mCursor == null)
            return;

        //get column indexes from Cursor
        //fixing up to display note title
        mCoursePos = mCursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_TITLE);
        mNoteTitlePos = mCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        mIDPos = mCursor.getColumnIndex(NoteInfoEntry._ID);

        //next step for displaying the database info in the onBindViewHolder
    }

    public void changeCursor(Cursor cursor){
        if(mCursor != null)
            mCursor.close();

        mCursor = cursor;
        populateColumnPositions();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
     View itemView = mLayoutInflater.inflate(R.layout.item_note_list, parent, false);

     return new viewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
    //taking the cursor to the required object position
    mCursor.moveToPosition(position);

    //getting the actual values corresponding to the postion
    String course = mCursor.getString(mCoursePos);
    String noteTitle = mCursor.getString(mNoteTitlePos);
    int ID = mCursor.getInt(mIDPos);

    holder.mTextCourse.setText(course);
    holder.mTextTitle.setText(noteTitle);

    //setting the position of the note
     //holder.mID = position;

     //using database
     holder.mID = ID;
    }

    @Override
    public int getItemCount() {
        //return mNotes.size();
        return mCursor == null ? 0 : mCursor.getCount();
    }

    //creating nested class for custom ViewHolder implementation
public class viewHolder extends RecyclerView.ViewHolder{
//the view references should be public, so that the Outer class
//can access them
        public final TextView mTextCourse;
        public final TextView mTextTitle;
        public int mID;

        public viewHolder(@NonNull View itemView) {
        super(itemView);
            mTextCourse = itemView.findViewById(R.id.text_course);
            mTextTitle = itemView.findViewById(R.id.text_title);

            //creating click event for the entire body of the individual cardView
            //here in the constructor
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //showing the NoteActivity class once the cardView is clicked
                    //and passing the position of the note as an IntentExtra
                    Intent intent = new Intent(mContext, NoteActivity.class);
                    intent.putExtra(NoteActivity.NOTE_ID, mID);
                    mContext.startActivity(intent);
                }
            });
    }
}
}
