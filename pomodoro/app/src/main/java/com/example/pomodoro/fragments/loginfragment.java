package com.example.pomodoro.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.pomodoro.R;

public class loginfragment extends Fragment {

    EditText username;
    EditText contraseña;
    Button loginbutton;

    private OnFragmentInteractionListener mListener;

    public loginfragment() {
        // Required empty public constructor
    }


    public static loginfragment newInstance(String param1, String param2) {
        loginfragment fragment = new loginfragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_loginfragment, container, false);

        username = view.findViewById(R.id.editUsername);
        contraseña = view.findViewById(R.id.textpassword);
        loginbutton = view.findViewById(R.id.loginbutton);

        loginbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // cuando el usuario pulsa en el botón
                if (mListener != null) {
                    mListener.login(username.getText().toString(), contraseña.getText().toString());
                }
            }

        });

        return view;


    }


    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            //
        }
    }

    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void login(String username, String password);
    }
}
