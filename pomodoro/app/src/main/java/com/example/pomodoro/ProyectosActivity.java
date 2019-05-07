package com.example.pomodoro;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.example.pomodoro.dialogs.NuevoProyecto;
import com.example.pomodoro.models.Pomodoro;
import com.example.pomodoro.models.Project;
import com.example.pomodoro.models.UserProyectos;
import com.example.pomodoro.recyclerView.MyAdapter;
import com.example.pomodoro.services.Timer;
import com.example.pomodoro.utilities.MainToolbar;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ProyectosActivity extends MainToolbar implements NuevoProyecto.ListenerDelDialogo {

    private DatabaseReference databaseReference;
    private DatabaseReference databaseReferenceUserProyectos;
    private DatabaseReference databaseReferenceProyectos;
    private ArrayList<Project> list = new ArrayList<>();
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;

    private String nameOfNewProject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // load main activity with fragment(s)
        setContentView(R.layout.activity_proyectos);
        // load top toolbar
        loadToolbar();

        // listener, recuperar valores si la aplicación se ha cerrado y había un pomodoro en marcha
        DatabaseReference databaseReferenceProyectosPomodoro =
                FirebaseDatabase.getInstance().getReference(
                        "ProyectosPomodoro");

        databaseReferenceProyectosPomodoro.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean empezado = false;
                for (DataSnapshot oneData : dataSnapshot.getChildren()) {
                    Pomodoro pomodoro = oneData.getValue(Pomodoro.class);

                    if (pomodoro.getEmpezado()) {
                        if (pomodoro.getUsuario().equals(getActiveUsername())) {
                            empezado = true;
                        }

                        // fecha ahora
                        java.util.Date fechaActual = new java.util.Date();
                        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
                        calendar.setTime(fechaActual);
                        long milisecondsNow = calendar.getTimeInMillis();

                        Date fin = stringToDate(pomodoro.getHoraDescansoFin());
                        calendar = Calendar.getInstance(Locale.ENGLISH);
                        calendar.setTime(fin);
                        long milisecondsFin = calendar.getTimeInMillis();

                        if (milisecondsFin - milisecondsNow <= 0) {
                            // el pomodoro ya ha terminado, pero pone empezado, cambiar
                            if (getStringPreference("pomodoroKey") != null && getStringPreference(
                                    "pomodoroKey").equals(oneData.getKey())) {
                                // se ha quitado internet antes de actualizar el fin del pomodoro,
                                // y este era el dueño

                                // ya ha terminado
                                setStringPreference("pomodoroKey", null);
                                // parar servicio
                                if (servicioEnMarcha(Timer.class) && !getBooleanPreference("individual")) {
                                    Intent e = new Intent(ProyectosActivity.this,
                                            Timer.class);
                                    e.putExtra("stop", true);
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        startForegroundService(e);
                                    } else {
                                        startService(e);
                                    }
                                }
                            }
                            // actualizar datos firebase
                            databaseReference = FirebaseDatabase.getInstance().getReference(
                                    "ProyectosPomodoro").child(oneData.getKey());
                            databaseReference.child("empezado").setValue(false).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    int tiempo = Toast.LENGTH_SHORT;
                                    Context context = getApplicationContext();
                                    Toast aviso = Toast.makeText(context, getResources().getString(R.string.error),
                                            tiempo);
                                    aviso.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, 100);
                                    aviso.show();
                                }
                            });
                        } else if (pomodoro.getUsuario().equals(getActiveUsername()) && getStringPreference("pomodoroKey") == null && !getBooleanPreference("individual") && !servicioEnMarcha(Timer.class)) {
                            // si está empezado y esto es null, es que se ha salido de la aplicación
                            setStringPreference("pomodoroKey", oneData.getKey());
                            // iniciar otra vez servicio
                            setBooleanPreference("individual", false);
                            Intent e = new Intent(ProyectosActivity.this, Timer.class);
                            e.putExtra("horaTrabajoFin", pomodoro.getHoraWorkFin());
                            e.putExtra("horaDescansoFin", pomodoro.getHoraDescansoFin());
                            e.putExtra("pomodoroKey", oneData.getKey());
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                startForegroundService(e);
                            } else {
                                startService(e);
                            }
                        }
                    }
                }
                if (!empezado) {
                    if (getStringPreference("pomodoroKey") != null) {
                        // hay un servicio todavía activo
                        // dos dispositivos con la misma cuenta de usuario, uno de ellos con
                        // internet y el otro sin internet, son dueños de un pomodoro grupal, y
                        // el segundo como no tiene internet si acaba el servicio no se termina,
                        // por eso esta condición
                        Intent e = new Intent(ProyectosActivity.this,
                                Timer.class);
                        e.putExtra("stop", true);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            startForegroundService(e);
                        } else {
                            startService(e);
                        }
                    }
                    setStringPreference("pomodoroKey", null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showToast(false, R.string.error);
            }
        });


        recyclerView = (RecyclerView)

                findViewById(R.id.elreciclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new

                LinearLayoutManager(this, LinearLayoutManager.VERTICAL,
                false));
        adapter = new

                MyAdapter(ProyectosActivity.this, list);
        // Add listeners
        ((MyAdapter) adapter).

                setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Se clicka en un proyecto
                        int clickedPosition = recyclerView.getChildAdapterPosition(v);

                        try {
                            // obtener el proyecto correspondiente a la posición
                            Project proyecto = list.get(clickedPosition);

                            // abrir actividad para ver los pomodoros dentro del proyecto
                            Intent i = new Intent(ProyectosActivity.this, ProyectoPomodorosActivity.class);
                            i.putExtra("projectKey", proyecto.getKey());
                            i.putExtra("projectName", proyecto.getNombre());
                            startActivity(i);
                        } catch (IndexOutOfBoundsException e) {
                            showToast(false, R.string.error);
                        }

                    }
                });

        recyclerView.setAdapter(adapter);

        String actualUser = getActiveUsername();

        // Add listener to bottom menu
        BottomNavigationView bottomMenu = findViewById(R.id.bottomNavigationView);

        selectProjects(bottomMenu);

        addListenerToBottomMenu(bottomMenu);

        // Sincronizar proyectos
        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReferenceUserProyectos = FirebaseDatabase.getInstance().getReference("UserProyectos");
        Query query = databaseReferenceUserProyectos.orderByChild("usuario").equalTo(actualUser);
        databaseReferenceProyectos = FirebaseDatabase.getInstance().getReference("Proyectos");
        query.addChildEventListener(new  ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                UserProyectos userProyecto = dataSnapshot.getValue(UserProyectos.class);
                databaseReferenceProyectos.child(userProyecto.getProyecto()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Project proyecto = dataSnapshot.getValue(Project.class);
                        proyecto.setKey(dataSnapshot.getKey());
                        list.add(proyecto);

                        adapter.notifyItemInserted(list.size() - 1);

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
                showToast(false, R.string.error);
            }
        });

    }

    /**
     * El usuario clicka en "nuevo proyecto"
     *
     * @param view
     */
    public void nuevoProyecto(View view) {
        DialogFragment dialog = new NuevoProyecto();
        dialog.show(getSupportFragmentManager(), "nuevoProyecto");
    }

    /**
     * El usuario quiere crear un pomodoro individual
     *
     * @param view
     */
    public void nuevoPomodoro(View view) {
        boolean servicioEnMarcha = servicioEnMarcha(Timer.class);
        if (servicioEnMarcha) {
            showToast(false, R.string.pomodoroActive);
            return;
        }
        Intent i = new Intent(this, NewPomodoro.class);
        startActivity(i);
    }

    /**
     * El usuario ha introducido el nombre del proyecto que desea crear y no está vacío
     *
     * @param name
     */
    @Override
    public void yesAddProject(String name) {
        if (!isNetworkAvailable()) {
            // se necesita internet
            showToast(true, R.string.internetNeeded);
            return;
        }

        nameOfNewProject = name;

        Project nuevoProyecto = new Project();
        nuevoProyecto.setNombre(nameOfNewProject);
        nuevoProyecto.setEstado("");

        databaseReferenceProyectos.push().setValue(nuevoProyecto, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError != null) {
                    // error
                    showToast(false, R.string.error);
                    return;
                }
                String key = databaseReference.getKey();
                UserProyectos userProyecto = new UserProyectos();

                String actualUser = getActiveUsername();

                userProyecto.setUsuario(actualUser);
                userProyecto.setProyecto(key);
                databaseReferenceUserProyectos.push().setValue(userProyecto, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if (databaseError != null) {
                            // error
                            showToast(false, R.string.error);
                            return;
                        }
                        showToast(true, R.string.projectCreated);
                    }
                });
            }
        });

    }
}
