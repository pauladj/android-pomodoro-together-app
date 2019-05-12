package com.example.pomodoro.recyclerView_chat;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.pomodoro.R;
import com.example.pomodoro.models.Chat;
import com.example.pomodoro.models.Pomodoro;

import java.util.ArrayList;


public class MyAdapterChat extends RecyclerView.Adapter<ViewHolderChat> {

    Context c;
    ArrayList<Chat> chats;


    public MyAdapterChat(Context c, ArrayList<Chat> chats) {
        this.c = c;
        this.chats = chats;
    }

    /**
     * Get preference knowing the key
     *
     * @param key - the key of the preference you want to know
     * @return - the value of that key
     */
    private String getPreference(String key) {
        SharedPreferences prefs_especiales = c.getSharedPreferences(
                "preferencias_especiales",
                Context.MODE_PRIVATE);

        return prefs_especiales.getString(key, null);
    }

    @Override
    public int getItemViewType(int position) {
        if (this.chats.get(position).getUsuario().equals(getPreference("activeUsername"))) {
            // el mensaje lo ha escrito este usuario
            return 0; // este usuario
        } else {
            return 2; // otro usuario
        }
    }

    @Override
    public ViewHolderChat onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout;
        if (viewType == 0) {
            layout = R.layout.message_sent_me;
        } else {
            layout = R.layout.message_sent;
        }
        View ellayoutdelafila = LayoutInflater.from(parent.getContext()).inflate(layout, null);
        ViewHolderChat evh = new ViewHolderChat(ellayoutdelafila);
        return evh;
    }


    @Override
    public void onBindViewHolder(ViewHolderChat elViewHolder, int i) {
        elViewHolder.noteMessage.setText(chats.get(i).getText());
        elViewHolder.noteDate.setText(chats.get(i).getTimestamp());
        if (elViewHolder.noteWho != null){
            // el mensaje no es mio
            elViewHolder.noteWho.setText(chats.get(i).getUsuario());
        }
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }


}