package com.example.pomodoro.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.pomodoro.R;

public class NuevoProyecto extends DialogFragment {

    ListenerDelDialogo miListener;

    public interface ListenerDelDialogo {
        void yesAddProject(String nameOfNewProject);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        miListener = (ListenerDelDialogo) getActivity();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.newProject));
        LayoutInflater inflater=getActivity().getLayoutInflater();
        final View elaspecto= inflater.inflate(R.layout.nuevo_proyecto_dialog,null);
        builder.setView(elaspecto);

        final String positiveButton = getResources().getString(R.string.create);
        String negativeButton = getResources().getString(R.string.cancel);

        builder.setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String proyectoTitulo =
                        ((TextView) elaspecto.findViewById(R.id.inputTextToShow)).getText().toString();
                if (!proyectoTitulo.isEmpty()){
                    miListener.yesAddProject(proyectoTitulo);
                }
            }
        });

        builder.setNegativeButton(negativeButton, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // do nothing
            }
        });

        return builder.create();
    }
}
