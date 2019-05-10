package com.example.pomodoro.recyclerViewProjectPomodoros;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.pomodoro.R;
import com.example.pomodoro.models.Pomodoro;

import java.util.ArrayList;


public class MyAdapterPomodoros extends RecyclerView.Adapter<ViewHolderPomodoros>  implements View.OnClickListener {

    Context c;
    ArrayList<Pomodoro> pomodoros;

    public interface ClickListener {
        void onDeleteClicked(int position);
        void onStartClicked(int position);
    }

    private View.OnClickListener listener;

    public MyAdapterPomodoros(Context c, ArrayList<Pomodoro> pomodoros) {
        this.c = c;
        this.pomodoros = pomodoros;
    }

    @Override
    public ViewHolderPomodoros onCreateViewHolder(ViewGroup parent, int viewType) {
        View v=LayoutInflater.from(parent.getContext()).inflate(R.layout.proyectos_pomodoros_card_view,parent,
                false);
        v.setOnClickListener(this); // add listener
        return new ViewHolderPomodoros(v);
    }

    @Override
    public void onBindViewHolder(ViewHolderPomodoros holder, int position) {
        Pomodoro pomodoro = pomodoros.get(position);
        holder.minutosTrabajo.setText(String.valueOf(pomodoro.getWork()));
        holder.minutosDescanso.setText(String.valueOf(pomodoro.getRelax()));
        if (pomodoro.getEmpezado()){
            // El pomodoro está iniciado
            holder.estado.setText(R.string.active);
            holder.removePomodoro.setVisibility(View.INVISIBLE);
        }else{
            holder.estado.setText(R.string.start);
        }
    }

    @Override
    public int getItemCount() {
        return pomodoros.size();
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