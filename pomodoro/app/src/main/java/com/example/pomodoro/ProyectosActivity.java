package com.example.pomodoro;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.example.pomodoro.dialogs.NuevoProyecto;
import com.example.pomodoro.models.Project;
import com.example.pomodoro.models.UserProyectos;
import com.example.pomodoro.recyclerView.MyAdapter;
import com.example.pomodoro.utilities.MainToolbar;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseError;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ProyectosActivity extends MainToolbar implements NuevoProyecto.ListenerDelDialogo {

    private DatabaseReference databaseReference;
    private DatabaseReference databaseReferenceUserProyectos;
    private DatabaseReference databaseReferenceProyectos;
    private ProgressDialog progressDialog;
    private ArrayList<Project> list = new ArrayList<>();
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;

    private String nameOfNewProject;

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
        adapter = new MyAdapter(ProyectosActivity.this, list);
        recyclerView.setAdapter(adapter);

        progressDialog = new ProgressDialog(ProyectosActivity.this);
        progressDialog.setMessage(getResources().getString(R.string.loadingData));
        progressDialog.show();

        String actualUser = getActiveUsername();
        actualUser = "nombreUsuario"; // TODO

        // Add listener to bottom menu
        BottomNavigationView bottomMenu = findViewById(R.id.bottomNavigationView);
        addListenerToBottomMenu(bottomMenu);

        // Sincronizar proyectos
        databaseReference = FirebaseDatabase.getInstance().getReference();

        databaseReferenceUserProyectos =
                FirebaseDatabase.getInstance().getReference("UserProyectos");
        Query query = databaseReferenceUserProyectos.orderByChild("user").equalTo(actualUser);

        databaseReferenceProyectos = FirebaseDatabase.getInstance().getReference("Proyectos");

        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                UserProyectos userProyecto = dataSnapshot.getValue(UserProyectos.class);
                databaseReferenceProyectos.child(userProyecto.getProyecto()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Project proyecto = dataSnapshot.getValue(Project.class);
                        proyecto.setKey(dataSnapshot.getKey());
                        list.add(proyecto);

                        adapter.notifyItemInserted(list.size()-1);

                        if (progressDialog != null){
                            progressDialog.dismiss();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                databaseReferenceProyectos.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        Project proyecto = dataSnapshot.getValue(Project.class);
                        proyecto.setKey(dataSnapshot.getKey());
                        int index = list.indexOf(proyecto);
                        if (index != -1) {
                            list.set(index, proyecto);

                            adapter.notifyItemChanged(index);

                        }
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


                });
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }


            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                UserProyectos userProyecto = dataSnapshot.getValue(UserProyectos.class);
                String proyectoKey = userProyecto.getProyecto();
                Project proyecto = new Project();
                proyecto.setKey(proyectoKey);
                int index = list.indexOf(proyecto);
                list.remove(index);

                adapter.notifyItemRemoved(index);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }


            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });

    }

    /**
     * El usuario clicka en "nuevo proyecto"
     * @param view
     */
    public void nuevoProyecto(View view){
        DialogFragment dialog = new NuevoProyecto();
        dialog.show(getSupportFragmentManager(), "nuevoProyecto");
    }

    /**
     * El usuario ha introducido el nombre del proyecto que desea crear y no está vacío
     * @param name
     */
    @Override
    public void yesAddProject(String name) {
        // mirar si ya tiene uno con el mismo nombre, si es así no se puede añadir
        nameOfNewProject = name;

        Project nuevoProyecto = new Project();
        nuevoProyecto.setNombre(nameOfNewProject);
        nuevoProyecto.setEstado("");

        databaseReferenceProyectos.push().setValue(nuevoProyecto, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                String key = databaseReference.getKey();
                UserProyectos userProyecto = new UserProyectos();

                String actualUser = getActiveUsername();
                actualUser = "nombreUsuario"; // TODO

                userProyecto.setUser(actualUser);
                userProyecto.setProyecto(key);
                databaseReferenceUserProyectos.push().setValue(userProyecto, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        showToast(true, R.string.projectCreated);
                    }
                });
            }
        });


        // TODO fallos, mirar que pasa si no hay internet, el problema es con el nombre user que
        // no va no se porque

    }
}
