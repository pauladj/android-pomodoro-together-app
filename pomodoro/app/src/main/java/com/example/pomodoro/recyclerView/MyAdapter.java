package com.example.pomodoro.recyclerView;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.pomodoro.R;
import com.example.pomodoro.models.Project;

import java.util.ArrayList;


public class MyAdapter extends RecyclerView.Adapter<ViewHolder>  implements View.OnClickListener {

    Context c;
    ArrayList<Project> proyectos;

    private View.OnClickListener listener;

    public MyAdapter(Context c, ArrayList<Project> proyectos) {
        this.c = c;
        this.proyectos = proyectos;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v=LayoutInflater.from(parent.getContext()).inflate(R.layout.proyectos_card_view,parent,
                false);
        v.setOnClickListener(this); // add listener
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Project project = proyectos.get(position);
        holder.nombreProyecto.setText(project.getNombre());
        if (project.getEstado().equals("ACTIVO")){
            // si hay un pomodoro activo dentro mostrarlo aquí
            holder.estadoProyecto.setText(R.string.active);
        }
    }

    @Override
    public int getItemCount() {
        return proyectos.size();
    }


    // Add listeners
    public void setOnClickListener(View.OnClickListener listener){
        this.listener = listener;
    }


    @Override
    public void onClick(View view){
        if (listener != null){
            listener.onClick(view);
        }
    }
}