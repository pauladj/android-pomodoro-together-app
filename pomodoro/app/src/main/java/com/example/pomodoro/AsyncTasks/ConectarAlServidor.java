package com.example.pomodoro.AsyncTasks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Pair;

import com.example.pomodoro.R;
import com.example.pomodoro.utilities.GeneradorConexionesSeguras;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

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
            }else if(action.equals("sendphoto") && success){
                // la foto se ha subido correctamente al servidor

            }else if(action == "downloadimage" && success){
                // la foto se ha descargado correctamente

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
                    // El usuario quiere logearse
                    username = strings[0];
                    parametrosJSON.put("username", username);
                    parametrosJSON.put("password", strings[1]);
                    parametrosJSON.put("token", strings[2]);

                    PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
                    out.print(parametrosJSON.toString());
                    out.close();

                    JSONObject json = getJsonFromResponse(urlConnection);

                    // if ok
                    if (json.containsKey("success") && json.containsKey("imagepath")) {

                        if (json.get("imagepath") != null){
                            // el usuario tiene una imagen
                            // descargar una imagen
                            String imageUrl = strings[0];

                            // la nota es una imagen, se descarga y se guarda
                            String imageLocalPath = downloadAndSaveImage(imageUrl);
                            if (imageLocalPath.equals("")) {
                                // se ha producido un error al descargar/guardar la imagen
                                success = false;
                            }else{
                                success = true;

                                // guardar path de la imagen
                                SharedPreferences prefs_especiales= getActivity().getSharedPreferences(
                                        "preferencias_especiales",
                                        Context.MODE_PRIVATE);

                                // guardar fecha y hora del último fetch
                                SharedPreferences.Editor editor2= prefs_especiales.edit();
                                editor2.putString("imagePath", imageLocalPath);
                                editor2.apply();
                            }

                        }else{
                            success = true;
                        }

                    } else {
                        String error = json.get("error").toString();
                        if (error.equals("wrong_credentials")) {
                            // show toast
                            return Pair.create(true, R.string.incorrectPassword);
                        } else {
                            throw new Exception("connection_error");
                        }
                    }
                }else if(action == "sendphoto"){
                    // Mandar una imagen tomada por la cámara de fotos

                    // obtener, comprimir y transformar la imagen a Base64
                    String uri = strings[0];
                    String username = strings[1];

                    Bitmap mBitmap = null;
                    if (uri.contains("content://")){
                        mBitmap =
                                BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(Uri.parse(uri)));
                    }else{
                        Uri imagen = Uri.fromFile(new File(uri));

                        mBitmap =
                                MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(),
                                        imagen);

                    }

                    ByteArrayOutputStream stream = new ByteArrayOutputStream();

                    mBitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                    byte[] fototransformada = stream.toByteArray();
                    String fotoen64 = Base64.encodeToString(fototransformada,Base64.DEFAULT);

                    // formar parámetros a enviar
                    urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                    Uri.Builder builder = new Uri.Builder()
                            .appendQueryParameter("action", action)
                            .appendQueryParameter("username", username)
                            .appendQueryParameter("image", fotoen64);
                    String parametrosURL = builder.build().getEncodedQuery();

                    PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
                    out.print(parametrosURL);
                    out.close();

                    JSONObject json = getJsonFromResponse(urlConnection);

                    // si se ha producido un problema
                    if (!json.containsKey("success")){
                        throw new Exception("connection_error");
                    }

                    // guardar path de la imagen
                    SharedPreferences prefs_especiales= getActivity().getSharedPreferences(
                            "preferencias_especiales",
                            Context.MODE_PRIVATE);

                    // guardar fecha y hora del último fetch
                    SharedPreferences.Editor editor2= prefs_especiales.edit();
                    editor2.putString("imagePath", uri);
                    editor2.apply();

                    success = true;
                }
            } catch (Exception e) {
                // error
                // toast of error
                return Pair.create(true, R.string.serverError);
            }
            return null;
        }

        /**
         * Download an image from the server and save it on the phone
         * @param url - the url of the image to download
         * @return - the local path of the image
         */
        private String downloadAndSaveImage(String url){
            try {
                HttpsURLConnection conn =
                        GeneradorConexionesSeguras.getInstance().crearConexionSegura(getActivity(), url);

                int responseCode = 0;

                responseCode = conn.getResponseCode();
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    // se guarda la foto de tamaño normal
                    Bitmap elBitmap = BitmapFactory.decodeStream(conn.getInputStream());

                    File eldirectorio = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    String fileName = timeStamp + "_";

                    File imagenFich = new File(eldirectorio, fileName + ".jpg");
                    OutputStream os = new FileOutputStream(imagenFich);

                    elBitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
                    os.flush();
                    os.close();

                    // avisar a la galería de que hay una nueva foto
                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    Uri contentUri = Uri.fromFile(imagenFich);
                    mediaScanIntent.setData(contentUri);
                    getActivity().sendBroadcast(mediaScanIntent);

                    return imagenFich.getPath(); // devolver el path local de la imagen

                }else{
                    throw new Exception();
                }
            } catch (Exception e) {
                // error downloading image
                return "";
            }
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