package com.example.pomodoro.AsyncTasks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Pair;

import com.example.pomodoro.R;
import com.example.pomodoro.utilities.GeneradorConexionesSeguras;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;

import javax.net.ssl.HttpsURLConnection;


public class ConectarAlServidor extends Fragment {

    private ProgressDialog progressDialog;
    private boolean isTaskRunning = false;
    private String action; // action to do, signup, login....
    private boolean success = false; // if the asynctask has been successful...
    private String direccion; // url

    private String username; // username

    public interface TaskCallbacks {
        void showToast(Boolean acrossWindows, int messageId);

        void loginSuccess(String username);

        void signUpSuccess();
    }

    private TaskCallbacks mCallbacks;
    private DummyTask mTask;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = (TaskCallbacks) activity;
    }

    /**
     * Solo se llamará una vez
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Mantener el fragmento aunque se rote la pantalla.
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Si la pantalla se ha girado y la tarea todavía se está ejecutando mostrar otra vez el
        // progressdialog
        if (isTaskRunning) {
            // Se configura el progress dialog
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setIndeterminate(true);
            progressDialog.setTitle(getResources().getString(R.string.loadingTitle));
            progressDialog.setMessage(getResources().getString(R.string.loadingBody));
            progressDialog.show();
        }
    }

    /**
     * Start the task
     */
    public void start(String[] params) {
        // Create and execute the background task.
        mTask = new DummyTask();
        mTask.execute(params);
    }

    /**
     * Set the action to execute (login,..)
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * Establecer la url
     *
     * @param url
     */
    public void setDireccion(String url) {
        this.direccion = url;
    }


    @Override
    public void onDetach() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        super.onDetach();
        mCallbacks = null;
    }


    private class DummyTask extends AsyncTask<String, Integer, Pair<Boolean, Integer>> {

        @Override
        public void onPreExecute() {
            isTaskRunning = true;

            // configurar y enseñar el diálogo
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setIndeterminate(true);
            progressDialog.setTitle(getResources().getString(R.string.loadingTitle));
            progressDialog.setMessage(getResources().getString(R.string.loadingBody));
            progressDialog.show();
        }

        @Override
        public void onPostExecute(Pair<Boolean, Integer> result) {
            if (progressDialog != null) {
                // cerrar el diálogo
                progressDialog.dismiss();
            }
            isTaskRunning = false;
            if (result != null) {
                // si hay mensaje toast enviarlo a la actividad padre
                mCallbacks.showToast(result.first, result.second);
            }
            if (action.equals("login") && success) {
                mCallbacks.loginSuccess(username);
            } else if (action.equals("signup") && success) {
                mCallbacks.signUpSuccess();
            }
        }


        @Override
        protected Pair<Boolean, Integer> doInBackground(String... strings) {
            try {
                success = false;

                // se forma la url con sus opciones para pedir datos al servidor
                HttpsURLConnection urlConnection =
                        GeneradorConexionesSeguras.getInstance().crearConexionSegura(getActivity(), direccion);

                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Content-Type", "application/json");

                // se forma el json y se procesa la respuesta según el método que se quiera

                JSONObject parametrosJSON = new JSONObject();
                parametrosJSON.put("action", action);
                if (action == "signup") {
                    // El usuario quiere registrarse
                    parametrosJSON.put("username", strings[0]);
                    parametrosJSON.put("password", strings[1]);
                    parametrosJSON.put("email", strings[2]);

                    PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
                    out.print(parametrosJSON.toString());
                    out.close();

                    JSONObject json = getJsonFromResponse(urlConnection);

                    // if ok
                    if (json.containsKey("success")) {
                        // toast
                        success = true;
                        return Pair.create(true, R.string.userSuccessfullyRegistered);
                    } else {
                        String error = json.get("error").toString();
                        if (error.equals("username_exists")) {
                            // show toast
                            return Pair.create(true, R.string.userAlreadyExists);
                        } else {
                            throw new Exception("connection_error");
                        }
                    }
                } else if (action == "login") {
                    // El usuario quiere registrarse
                    username = strings[0];
                    parametrosJSON.put("username", username);
                    parametrosJSON.put("password", strings[1]);
                    parametrosJSON.put("token", strings[2]);

                    PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
                    out.print(parametrosJSON.toString());
                    out.close();

                    JSONObject json = getJsonFromResponse(urlConnection);

                    // if ok
                    if (json.containsKey("success")) {
                        // ok
                        success = true;
                    } else {
                        String error = json.get("error").toString();
                        if (error.equals("wrong_credentials")) {
                            // show toast
                            return Pair.create(true, R.string.incorrectPassword);
                        } else {
                            throw new Exception("connection_error");
                        }
                    }
                }
            } catch (Exception e) {
                // error
                // toast of error
                return Pair.create(true, R.string.serverError);
            }
            return null;
        }


        /**
         * We get the json from the http request
         *
         * @param urlConnection
         * @return - the json object of the response
         * @throws Exception - if there is a connection error
         */
        private JSONObject getJsonFromResponse(HttpURLConnection urlConnection) throws Exception {
            try {
                int statusCode = urlConnection.getResponseCode();

                if (statusCode == 200) {
                    BufferedInputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                    String line, result = "";
                    while ((line = bufferedReader.readLine()) != null) {
                        result += line;
                    }
                    inputStream.close();

                    // get the json of the response
                    JSONParser parser = new JSONParser();
                    JSONObject json = (JSONObject) parser.parse(result);

                    return json;
                } else {
                    throw new Exception("connection_error");
                }
            } catch (Exception e) {
                // error
                // there was a connection error
                if (urlConnection != null) {
                    // cerrar conexión
                    urlConnection.disconnect();
                }
                throw new Exception("connection_error");
            }

        }

    }
}