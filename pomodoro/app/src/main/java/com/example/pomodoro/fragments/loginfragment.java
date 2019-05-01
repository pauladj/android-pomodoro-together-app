package com.example.pomodoro.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import com.example.pomodoro.R;

public class loginfragment extends Fragment {

    EditText email;
    EditText contraseña;
    Button loginbutton;
    Button btnidioma;
    Switch switchRecordar;

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

         email = view.findViewById(R.id.editTextemail);
         contraseña = view.findViewById(R.id.textpassword);
         loginbutton = view.findViewById(R.id.loginbutton);
         btnidioma = view.findViewById(R.id.idioma);
         switchRecordar =  view.findViewById(R.id.switch1);

        if (!TextUtils.isEmpty(mListener.getemailprefrencias()) && !TextUtils.isEmpty(mListener.getcontraprefrencias())){
            //Si no es nulo, se  hacen set de los edittext
            email.setText(mListener.getemailprefrencias());
            contraseña.setText(mListener.getcontraprefrencias());
        }

        loginbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // cuando el usuario pulsa en el botón
             String correo = email.getText().toString();
             String password = contraseña.getText().toString();
             if(mListener != null) {
                 mListener.login(correo, password);
                 if(switchRecordar.isChecked()){
                    mListener.guardarpreferencias(correo,password);
                 }
                 }
             }

        });
        btnidioma.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {

                   // mListener.cambiaramaps(new Fragment());
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
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void guardarpreferencias(String correo, String password);
        void login(String correo, String password);
        String getemailprefrencias();
        String getcontraprefrencias();
    }
}
