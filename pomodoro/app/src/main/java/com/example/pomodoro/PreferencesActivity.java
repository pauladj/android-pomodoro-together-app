package com.example.pomodoro;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.Menu;

import com.example.pomodoro.utilities.MainToolbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PreferencesActivity extends MainToolbar {

    private String uri; // si se quiere sacar una foto aquí se guardará su localización
    private String photoName; // nombre de la foto que se quiere sacar con la cámara
    int CODIGO_GALERIA = 111;
    int CODIGO_FOTO =222;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preferences_activity);
        // load the top bar
        loadToolbar();
        // show the back button
        showBackButtonOption();

        // Add listener to bottom menu
        BottomNavigationView bottomMenu = findViewById(R.id.bottomNavigationView);
        selectConfiguration(bottomMenu);
        addListenerToBottomMenu(bottomMenu);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Change the topbar options
        getMenuInflater().inflate(R.menu.preferences_toolbar, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        // If the back button is pressed
        Intent i = new Intent (this, ProyectosActivity.class);
        // clear the activity stack, so the mainactivity view is recreated
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();

    }

    /**
     * The user wants to take a photo with the camera
     */
    public void tryTakingPhotoWithTheCamera(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!=
                PackageManager.PERMISSION_GRANTED) {
            //EL PERMISO NO ESTÁ CONCEDIDO, PEDIRLO
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)){
                // MOSTRAR AL USUARIO UNA EXPLICACIÓN DE POR QUÉ ES NECESARIO EL PERMISO

            }
            else{
                //EL PERMISO NO ESTÁ CONCEDIDO TODAVÍA O EL USUARIO HA INDICADO
                //QUE NO QUIERE QUE SE LE VUELVA A SOLICITAR

            }
            //PEDIR EL PERMISO
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    200);
        }
        else {
            //EL PERMISO ESTÁ CONCEDIDO, EJECUTAR LA FUNCIONALIDAD
            takePhoto();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 200:{
                // Si la petición se cancela, granResults estará vacío
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // PERMISO CONCEDIDO, EJECUTAR LA FUNCIONALIDAD
                    takePhoto();
                }
                else {
                    // PERMISO DENEGADO, DESHABILITAR LA FUNCIONALIDAD O EJECUTAR ALTERNATIVA

                }
                return;
            }
        }
    }

    /**
     * El usuario tiene permisos para sacar una foto
     */
    private void takePhoto(){
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = timeStamp + "_";

        File directorio = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File fichImg = null;
        Uri uriimagen = null;
        try {
            fichImg = File.createTempFile(fileName, ".jpg", directorio);
            uriimagen = FileProvider.getUriForFile(this, "com.example.proyecto1.provider",
                    fichImg);

            uri = fichImg.getPath();
            photoName = uri.split(".jpg")[0];
            Intent elIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            elIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriimagen);
            startActivityForResult(elIntent, 100);
        } catch (Exception e) {
            showToast(false, R.string.error);
        }
    }

    /**
     * Respuesta después de sacar la foto
     * @param requestCode - to specify this is the callback of capturing an image
     * @param resultCode - the result code of capturing an image
     * @param data - the data
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100 && resultCode == RESULT_OK) {
            // se guarda la foto en miniatura si su ancho es mayor de 240
            Uri contentUri = Uri.fromFile(new File(uri));
            try {
                Bitmap elBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),
                        contentUri);


                // mandar aviso a la galería de que se ha añadido una imagen
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(contentUri);
                this.sendBroadcast(mediaScanIntent);

                // mandar foto al servidor
                String[] params = {uri};
                getmTaskFragment().setAction("sendphoto");
                getmTaskFragment().start(params);
            }catch (Exception e){
                showToast(false, R.string.error);
            }


        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("uri", uri);
        outState.putString("photoName", photoName);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        uri = savedInstanceState.getString("uri");
        photoName = savedInstanceState.getString("photoName");
    }


}
