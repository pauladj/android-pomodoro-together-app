package com.example.pomodoro.recyclerViewProjectPomodoros;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.pomodoro.R;
import com.example.pomodoro.models.Pomodoro;

import java.util.ArrayList;


public class MyAdapterPomodoros extends RecyclerView.Adapter<ViewHolderPomodoros> {

    Context c;
    ArrayList<Pomodoro> pomodoros;

    private ImageButton deleteButton;
    private TextView startButton;

    private ClickListener listener;

    private View v;

    public interface ClickListener {
        void onDeleteClicked(int position, View view);

        void onStartClicked(int position);
    }


    public MyAdapterPomodoros(Context c, ArrayList<Pomodoro> pomodoros, ClickListener listener) {
        this.c = c;
        this.pomodoros = pomodoros;
        this.listener = listener;
    }

    @Override
    public ViewHolderPomodoros onCreateViewHolder(ViewGroup parent, int viewType) {
        v =
                LayoutInflater.from(parent.getContext()).inflate(R.layout.proyectos_pomodoros_card_view, parent,
                        false);
        deleteButton = v.findViewById(R.id.buttonDeletePomodoro);
        startButton = v.findViewById(R.id.estado);
        return new ViewHolderPomodoros(v);
    }


    @Override
    public void onBindViewHolder(ViewHolderPomodoros holder, int position) {
        Pomodoro pomodoro = pomodoros.get(position);
        holder.minutosTrabajo.setText(String.valueOf(pomodoro.getWork()));
        holder.minutosDescanso.setText(String.valueOf(pomodoro.getRelax()));
        if (pomodoro.getEmpezado()) {
            // El pomodoro está iniciado
            holder.estado.setText(R.string.active);
            holder.removePomodoro.setVisibility(View.INVISIBLE);
        } else {
            holder.estado.setText(R.string.start);
        }

        if (holder.removePomodoro != null) {
            holder.removePomodoro.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onDeleteClicked(holder.getAdapterPosition(), holder.removePomodoro);
                }
            });
        }
        if (holder.estado != null) {
            holder.estado.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onStartClicked(holder.getAdapterPosition());
                }
            }); // add listener
        }
    }

    @Override
    public int getItemCount() {
        return pomodoros.size();
    }

}