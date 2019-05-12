package com.example.pomodoro.recyclerView_chat;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.pomodoro.R;


public class ViewHolderChat extends RecyclerView.ViewHolder  {

    public TextView noteMessage;
    public TextView noteDate;
    public TextView noteWho;

    public ViewHolderChat(View v) {
        super(v);
        noteMessage = v.findViewById(R.id.text_message_body);
        noteDate = v.findViewById(R.id.text_message_time);
        noteWho = v.findViewById(R.id.text_message_who);
    }
}