package com.example.pomodoro;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.View;

import com.example.pomodoro.dialogs.AddUserToProject;
import com.example.pomodoro.dialogs.ConfirmAbandonarProyecto;
import com.example.pomodoro.models.Pomodoro;
import com.example.pomodoro.models.UserProyectos;
import com.example.pomodoro.recyclerViewProjectPomodoros.MyAdapterPomodoros;
import com.example.pomodoro.utilities.MainToolbar;
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

public class ProyectoPomodorosActivity extends MainToolbar implements ConfirmAbandonarProyecto.ListenerDelDialogo, AddUserToProject.ListenerDelDialogo {

    private String projectKey; // de que proyecto son los pomodoros

    private DatabaseReference databaseReferenceUserProyectos;
    private DatabaseReference databaseReferenceProyectosPomodoro;

    private ArrayList<Pomodoro> list = new ArrayList<>();
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;

    private String projectName; // el nombre del proyecto

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toolbar_proyecto, menu);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("projectKey", projectKey); // Se guarda
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proyecto_pomodoros);


        Bundle b = getIntent().getExtras();
        if (b != null){
            // obtener los datos del proyecto
            projectName = b.getString("projectName", null);
            projectKey = b.getString("projectKey", null);

            if (projectName == null || projectKey == null) {
                // error
                showToast(true, R.string.error);
                finish();
            }
        }

        // load top toolbar
        loadToolbar(projectName);
        showBackButtonOption();

        recyclerView = (RecyclerView) findViewById(R.id.elreciclerviewProyectoPomodoros);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,
                false));
        adapter = new MyAdapterPomodoros(ProyectoPomodorosActivity.this, list);
        // Add listeners
        ((MyAdapterPomodoros) adapter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Se clicka en un pomodoro
                int clickedPosition = recyclerView.getChildAdapterPosition(v);

                try {
                    // obtener el proyecto correspondiente a la posición
                    Pomodoro pomodoro = list.get(clickedPosition);

                    if (pomodoro.getEmpezado()){
                        // abrir actividad para ver pomodoro
                        // TODO
                    }else{
                        // mensaje de que el pomodoro no está activo
                        showToast(false, R.string.pomodoroNotActive);
                    }

                }catch (IndexOutOfBoundsException e){
                    showToast(false, R.string.error);
                }

            }
        });

        recyclerView.setAdapter(adapter);

        // Add listener to bottom menu
        BottomNavigationView bottomMenu = findViewById(R.id.bottomNavigationView);
        selectProjects(bottomMenu);
        addListenerToBottomMenu(bottomMenu);

        databaseSincronizacion();

    }

    /**
     * Actualizar los datos según firebase la primera vez y cada vez que se actualicen
     */
    private void databaseSincronizacion(){
        // Sincronizar pomodoros
        databaseReferenceUserProyectos = FirebaseDatabase.getInstance().getReference(
                "UserProyectos");

        databaseReferenceProyectosPomodoro =
                FirebaseDatabase.getInstance().getReference("ProyectosPomodoro");
        Query query = databaseReferenceProyectosPomodoro.orderByChild("proyecto").equalTo(projectKey);

        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Pomodoro pomodoro = dataSnapshot.getValue(Pomodoro.class);

                pomodoro.setKey(dataSnapshot.getKey());
                if (pomodoro.getEmpezado()){
                    pomodoro.setHoraFin(dataSnapshot.child("horaFin").getValue().toString());
                }
                list.add(pomodoro);

                adapter.notifyItemInserted(list.size()-1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Pomodoro pomodoro = dataSnapshot.getValue(Pomodoro.class);
                pomodoro.setKey(dataSnapshot.getKey());
                int index = list.indexOf(pomodoro);
                if (index != -1) {
                    list.set(index, pomodoro);
                    adapter.notifyItemChanged(index);
                }
            }


            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Pomodoro pomodoro = dataSnapshot.getValue(Pomodoro.class);
                pomodoro.setKey(dataSnapshot.getKey());
                int index = list.indexOf(pomodoro);
                list.remove(index);

                adapter.notifyItemRemoved(index);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }


            @Override
            public void onCancelled(DatabaseError databaseError) {
                showToast(false, R.string.error);
            }
        });
    }

    /**
     * The user confirms they want to leave the project
     */
    @Override
    public void yesLeaveProject() {
        // TODO
        String user = getActiveUsername();
        user = "nombreUsuario";

        Query query =
                databaseReferenceUserProyectos.orderByChild("usuario").equalTo(user);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot oneData : dataSnapshot.getChildren()){
                    UserProyectos userProyecto = oneData.getValue(UserProyectos.class);
                    // la clave de la relación entre usuario y proyecto
                    String key = oneData.getKey();
                    if (userProyecto.getProyecto().equals(projectKey)){
                        databaseReferenceUserProyectos.child(key).removeValue(new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                if (databaseError != null){
                                    showToast(false, R.string.error);
                                }else{
                                    // El proyecto se ha abandonado correctamente
                                    showToast(true, R.string.projectAbandoned);
                                    finish();
                                }
                            }
                        });
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showToast(false, R.string.error);
            }
        });
    }

    /**
     * The user wants to add another user to the project knowing their username
     * @param username
     */
    @Override
    public void yesAddUser(final String username) {
        String activeUser = getActiveUsername();
        activeUser = "nombreUsuario"; // TODO

        if (activeUser.equals(username)){
            // un usuario no puede invitarse a si mismo
            showToast(false, R.string.error);
        }

        // TODO mirar si funciona a la vez, es decir, a la otra persona le sale automáticamente,
        // mirar que pasa si dos personas distintas intentan añadir al mismo user al mismo tiempo
        final UserProyectos userProyecto = new UserProyectos();
        userProyecto.setUsuario(username);
        userProyecto.setProyecto(projectKey);

        // Se utilizan transacciones para evitar que un mismo usuario sea añadido más de una vez
        // al mismo proyecto.
        databaseReferenceUserProyectos.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                for (MutableData child : mutableData.getChildren()){
                    String proyecto = child.child("proyecto").getValue().toString();
                    String usuario = child.child("usuario").getValue().toString();
                    if (proyecto.equals(projectKey) && usuario.equals(username)){
                        // la relación existe
                        return Transaction.success(mutableData);
                    }
                }
                // la relación no existe, añadirla
                mutableData.child(username + projectKey).setValue(userProyecto);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                if (databaseError != null){
                    showToast(false, R.string.error);
                    return;
                }
                showToast(false, R.string.userAddedToProject);
            }
        });
    }
}
