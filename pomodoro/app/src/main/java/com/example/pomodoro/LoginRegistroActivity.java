package com.example.pomodoro;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.widget.EditText;
import android.widget.TextView;

import com.example.pomodoro.AsyncTasks.ConectarAlServidor;
import com.example.pomodoro.fragments.loginfragment;
import com.example.pomodoro.fragments.registro;
import com.example.pomodoro.utilities.MainToolbar;
import com.example.pomodoro.utilities.PagerAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.json.JSONObject;

public class LoginRegistroActivity extends MainToolbar implements
        loginfragment.OnFragmentInteractionListener, registro.RegistroListener {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_registro);

        // comprobar si el usuario tiene google play
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int code = api.isGooglePlayServicesAvailable(this);
        if (code == ConnectionResult.SUCCESS) {
            // nothing
        }
        else {
            // si no tiene google play se carga una plantilla en blanco
            setContentView(R.layout.blank);
            showToast(true, R.string.googlePlayNeeded);
            if (api.isUserResolvableError(code)){
                api.getErrorDialog(this, code, 58).show();
            }
            return;
        }

        // cargar barra
        loadToolbar();

        // Cargar pestañas login-registro
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.login));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.signup));

        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);

        PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());

        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                viewPager.setCurrentItem(position);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }



    /**
     * El usuario ha introducido su username y password y quiere logearse
     *
     * @param username - nombre de usuario
     * @param password - contraseña
     */
    @Override
    public void login(String username, String password) {
        if (validadoresIniciarSesion(username, password)) {
            JSONObject parametrosJSON = new JSONObject();
            try {
                parametrosJSON.put("username", username);
                parametrosJSON.put("password", password);

                String[] params = {username, password};
                getmTaskFragment().setAction("login");
                getmTaskFragment().setDireccion("https://134.209.235" +
                        ".115/ebracamonte001/WEB/pomodoro/login.php");
                getmTaskFragment().start(params);
            } catch (Exception e) {
                showToast(false, R.string.error);
            }


        }
    }

    /**
     * The login has been successfull
     *
     * @param username - the username
     */
    public void loginSuccess(String username) {
        // guardar el usuario en preferencias
        setActiveUsername(username);

        // abrir actividad principal
        Intent i = new Intent(this, ProyectosActivity.class);
        startActivity(i);
        finish();
    }

    /**
     * El registro ha sido satisfactorio
     */
    public void signUpSuccess() {
        // limpiar campos
        EditText username = (EditText) findViewById(R.id.Nombre);
        username.setText("");
        EditText email = (EditText) findViewById(R.id.editemail);
        email.setText("");
        EditText pass = (EditText) findViewById(R.id.editTpass);
        pass.setText("");
        // ir a log in
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        TabLayout.Tab tab = tabLayout.getTabAt(0);
        tab.select();
    }

    /**
     * El usuario ha introducido sus datos y quiere registrarse
     *
     * @param nombreusuario
     * @param password
     * @param email
     */
    @Override
    public void registrarse(String nombreusuario, String password, String email) {
        if (!validadoresIniciarSesion(nombreusuario, password) || email.trim().isEmpty()) {
            return;
        }

        JSONObject parametrosJSON1 = new JSONObject();
        try {
            parametrosJSON1.put("username", nombreusuario);
            parametrosJSON1.put("password", password);
            parametrosJSON1.put("email", email);

            String[] params = {nombreusuario, password, email};
            getmTaskFragment().setAction("signup");
            getmTaskFragment().setDireccion("https://134.209.235" +
                    ".115/ebracamonte001/WEB/pomodoro/signup.php");
            getmTaskFragment().start(params);
        } catch (Exception e) {
            showToast(false, R.string.error);
        }
    }


    /**
     * Validadores de iniciar sesión
     *
     * @param username
     * @param password
     * @return
     */
    private boolean validadoresIniciarSesion(String username, String password) {
        if (username.trim().isEmpty() || password.trim().isEmpty()) {
            showToast(false, R.string.valuesEmptyError);
            return false;
        }
        return true;
    }


}
