package com.example.pomodoro.utilities;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.example.pomodoro.models.Project;

import java.util.ArrayList;

public class FirebaseHelper {

    DatabaseReference db;
    Boolean saved = null;
    ArrayList<String> projects = new ArrayList<>();

    public FirebaseHelper(DatabaseReference db) {
        this.db = db;
    }

    /**
     * Save project object to the firebase database
     * @param project
     * @return - true if success
     */
    public Boolean save(Project project){
        if(project == null){
            saved = false;
        }else {
            try{
                db.child("Project").push().setValue(project);
                saved=true;
            }catch (DatabaseException e){
                saved=false;
            }
        }

        return saved;
    }

    /**
     * Read from firebase database if something has changed
     * @return - the retrived list
     */
    public ArrayList<String> retrieve(){
        db.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                fetchData(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                fetchData(dataSnapshot);

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return projects;
    }

    private void fetchData(DataSnapshot dataSnapshot)
    {
        projects.clear();
        for (DataSnapshot ds : dataSnapshot.getChildren())
        {
            String name=ds.getValue(Project.class).getNombre();
            projects.add(name);
        }
    }

}