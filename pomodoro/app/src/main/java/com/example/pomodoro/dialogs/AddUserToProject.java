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

public class AddUserToProject extends DialogFragment {

    ListenerDelDialogo miListener;

    public interface ListenerDelDialogo {
        void yesAddUser(String username);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        miListener = (ListenerDelDialogo) getActivity();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.addUserToProject));
        LayoutInflater inflater=getActivity().getLayoutInflater();
        final View elaspecto= inflater.inflate(R.layout.nuevo_proyecto_dialog,null);
        TextView body = elaspecto.findViewById(R.id.textToShowLabel);
        body.setText(R.string.username);
        builder.setView(elaspecto);

        final String positiveButton = getResources().getString(R.string.add);
        String negativeButton = getResources().getString(R.string.cancel);

        builder.setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String username =
                        ((TextView) elaspecto.findViewById(R.id.inputTextToShow)).getText().toString();
                if (!username.isEmpty()){
                    miListener.yesAddUser(username);
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
