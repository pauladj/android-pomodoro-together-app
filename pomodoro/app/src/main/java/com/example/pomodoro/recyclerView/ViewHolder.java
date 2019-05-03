package com.example.pomodoro.recyclerView;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.pomodoro.R;


public class ViewHolder extends RecyclerView.ViewHolder  {

    TextView nombreProyecto;
    TextView estadoProyecto;

    public ViewHolder(View itemView) {
        super(itemView);

        nombreProyecto=(TextView) itemView.findViewById(R.id.tituloProyecto);
        estadoProyecto=(TextView) itemView.findViewById(R.id.estadoProyecto);


    }
}