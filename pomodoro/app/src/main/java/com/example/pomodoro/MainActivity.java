package com.example.pomodoro;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.EditText;

import com.example.pomodoro.models.Project;
import com.example.pomodoro.recyclerView.MyAdapter;
import com.example.pomodoro.utilities.FirebaseHelper;
import com.example.pomodoro.utilities.MainToolbar;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends MainToolbar {

    DatabaseReference databaseReference;
    ProgressDialog progressDialog;
    ArrayList<Project> list = new ArrayList<>();
    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // load main activity with fragment(s)
        setContentView(R.layout.activity_main);
        // load top toolbar
        loadToolbar();

        recyclerView = (RecyclerView) findViewById(R.id.elreciclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,
         false));

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Loading Data from Firebase Database");
        progressDialog.show();

        databaseReference = FirebaseDatabase.getInstance().getReference("Proyectos");
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Project proyecto = dataSnapshot.getValue(Project.class);
                list.add(proyecto);

                adapter = new MyAdapter(MainActivity.this, list);
                recyclerView.setAdapter(adapter);
                progressDialog.dismiss();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {


            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });

    }

}
