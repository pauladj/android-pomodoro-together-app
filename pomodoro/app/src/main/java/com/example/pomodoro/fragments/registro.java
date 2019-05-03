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

public class registro extends Fragment {

    private Button btnguardar;
    private EditText username;
    private EditText editemail;
    private EditText editpassword;


    private RegistroListener mListener;

    public registro() {
        // Required empty public constructor
    }

    public static registro newInstance(String param1, String param2) {
        registro fragment = new registro();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       View view =  inflater.inflate(R.layout.fragment_registro, container, false);
        btnguardar = view.findViewById(R.id.btnguardar);
        username = view.findViewById(R.id.Nombre);
        editemail = view.findViewById(R.id.editemail);
        editpassword = view.findViewById(R.id.editTpass);

        btnguardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editemail.getText().toString();
                String password = editpassword.getText().toString();
                String nombreusuarios = username.getText().toString();
                mListener.registrarse(nombreusuarios, password, email);
            }
        });
       return view;
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof RegistroListener) {
            mListener = (RegistroListener) context;
        } else {
            //
        }
    }


    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface RegistroListener {

        void registrarse(String nombreusuarios, String password, String email);
    }
}
