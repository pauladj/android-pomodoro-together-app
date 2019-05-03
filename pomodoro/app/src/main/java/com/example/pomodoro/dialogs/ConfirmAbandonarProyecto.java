package com.example.pomodoro.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.example.pomodoro.R;

public class ConfirmAbandonarProyecto extends DialogFragment {

    ListenerDelDialogo miListener;

    public interface ListenerDelDialogo {
        void yesLeaveProject();
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        return new AlertDialog.Builder(getActivity())
            .setTitle(R.string.confirmarAbandono)
            .setMessage(R.string.confirmarAbandono_body)
            .setCancelable(false)
            .setPositiveButton(R.string.continuar, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    miListener.yesLeaveProject();
                }
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        miListener = (ListenerDelDialogo) activity;
    }
}
