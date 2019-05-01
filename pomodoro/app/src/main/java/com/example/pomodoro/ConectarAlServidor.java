package com.example.pomodoro;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ProtocolException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;

import javax.net.ssl.HttpsURLConnection;


public class ConectarAlServidor extends AsyncTask<Void, Void, String> {
    public AsyncResponse delegate=null;
    private Context mContext;
    private JSONObject datos;
    private String php;


    public ConectarAlServidor(Context context, JSONObject pdatos , String pphp) {
        delegate = (AsyncResponse)context;
        mContext = context;
        datos = pdatos;
        php = pphp;

    }

    protected String doInBackground(Void... voids) {

        String error = "";
        HttpsURLConnection urlConnection= GeneradorConexionesSeguras.getInstance().crearConexionSegura(mContext,php);

        try {
            urlConnection.setRequestMethod("POST");
        } catch (ProtocolException e) {
            e.printStackTrace();
        }
        urlConnection.setDoOutput(true);
        urlConnection.setRequestProperty("Content-Type","application/json");

        Log.d("parametros", "registrarenbdremota: "+ datos.toString());

        try {
            PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
            out.print(datos.toString());
            Log.d("parametros", "registrarenbdremota: "+ datos);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int statusCode = 0;
        try {
            statusCode = urlConnection.getResponseCode();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            error = urlConnection.getResponseMessage();
        } catch (IOException e) {
            e.printStackTrace();
        }
        urlConnection.disconnect();
        Log.d("respuesta", "registrarenbdremota: "+ statusCode);

        if ( statusCode == 200 ){
            BufferedInputStream inputStream = null;
            try {
                inputStream = new BufferedInputStream(urlConnection.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            BufferedReader bufferedReader = null;
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String line = "", result="";
            while (true){
                try {
                    if (!(null != (line = bufferedReader.readLine()))) break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                result += line;
            }
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return result;

        }
        else{

            Log.i("error", "el error es : " + error);
            return null;
        }

    }


    protected void onPostExecute(final String result) {

        if(result != null){
            Log.i("tag", "onPostExecute: " + result);
            try {
                delegate.processFinish(result);
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }

    }

    public interface AsyncResponse {
        void processFinish(String output) throws ParseException;
    }



}
