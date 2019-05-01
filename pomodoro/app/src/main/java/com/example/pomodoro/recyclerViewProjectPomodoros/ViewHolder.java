package com.example.pomodoro.recyclerViewProjectPomodoros;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.pomodoro.R;


public class ViewHolder extends RecyclerView.ViewHolder  {

    TextView minutosTrabajo;
    TextView minutosDescanso;
    TextView estado;

    public ViewHolder(View itemView) {
        super(itemView);

        minutosDescanso=(TextView) itemView.findViewById(R.id.minutosDescanso);
        minutosTrabajo=(TextView) itemView.findViewById(R.id.minutosTrabajo);
        estado = (TextView) itemView.findViewById(R.id.estado);


    }
}