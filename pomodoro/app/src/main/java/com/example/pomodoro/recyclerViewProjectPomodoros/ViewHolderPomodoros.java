package com.example.pomodoro.recyclerViewProjectPomodoros;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.pomodoro.R;

import org.w3c.dom.Text;


public class ViewHolderPomodoros extends RecyclerView.ViewHolder  {

    TextView minutosTrabajo;
    TextView minutosDescanso;
    TextView estado;

    ImageButton removePomodoro;

    public ViewHolderPomodoros(View itemView) {
        super(itemView);

        minutosDescanso=(TextView) itemView.findViewById(R.id.minutosDescanso);
        minutosTrabajo=(TextView) itemView.findViewById(R.id.minutosTrabajo);
        estado = (TextView) itemView.findViewById(R.id.estado);
        removePomodoro = (ImageButton) itemView.findViewById(R.id.buttonDeletePomodoro);

    }
}