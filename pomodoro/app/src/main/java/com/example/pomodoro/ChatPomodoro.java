package com.example.pomodoro;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.View;

import com.example.pomodoro.models.Chat;
import com.example.pomodoro.models.Pomodoro;
import com.example.pomodoro.recyclerView_chat.MyAdapterChat;
import com.example.pomodoro.services.Timer;
import com.example.pomodoro.utilities.MainToolbar;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class ChatPomodoro extends MainToolbar {

    private ArrayList<Chat> list = new ArrayList<>();
    private ChildEventListener listener;
    private DatabaseReference databaseReference;
    private MyAdapterChat eladaptador;


    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // el método que se ejecutará cuando se reciba un broadcast
            
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Change the topbar options
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_pomodoro);

        // load top toolbar
        loadToolbar();
        showBackButtonOption();

        // load recycler view
        RecyclerView layoutRecycler = findViewById(R.id.chat);

        eladaptador = new MyAdapterChat(this, list);


        layoutRecycler.setAdapter(eladaptador);
        LinearLayoutManager elLayoutLineal = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        layoutRecycler.setLayoutManager(elLayoutLineal);
        layoutRecycler.smoothScrollToPosition(eladaptador.getItemCount()); // el scroll está abajo
        eladaptador.notifyDataSetChanged();

        loadData();

        // se registra el "broadcast" para cuando se reciban mensajes cortos
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("stopChat"));
    }

    private void loadData(){
        String pomodoroKey = getStringPreference("timerKey");
        if (pomodoroKey == null){
            showToast(true, R.string.error);
            finish();
        }else{
            listener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    Chat unChat = dataSnapshot.getValue(Chat.class);
                    // añadir nuevo mensaje a la lista
                    list.add(unChat);
                    // notificar al adaptador de que se ha añadido un nuevo mensaje
                    eladaptador.notifyItemInserted(list.size()-1);
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };

            databaseReference = FirebaseDatabase.getInstance().getReference().child(
                    "Chats").child(pomodoroKey);
            databaseReference.addChildEventListener(listener);
        }
    }

    @Override
    protected void onDestroy() {
        // Unregister the broadcast
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        // quitar listener del chat
        databaseReference.removeEventListener(listener);
        super.onDestroy();
    }

    public void sendMessage(View v){
        if (!servicioEnMarcha(Timer.class)){
            // servicio no en marcha
            showToast(false, R.string.pomodoroStopped);
            return;
        }
        // el servicio está en marcha
        if (!isNetworkAvailable()){
            showToast(true, R.string.internetNeeded);
            return;
        }
        // hay internet

    }
}
