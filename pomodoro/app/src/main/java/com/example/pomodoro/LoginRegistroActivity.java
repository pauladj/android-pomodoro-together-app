package com.example.pomodoro;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.Menu;
import android.widget.Toast;

import com.example.pomodoro.AsyncTasks.ConectarAlServidor;
import com.example.pomodoro.fragments.loginfragment;
import com.example.pomodoro.fragments.registro;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

public class LoginRegistroActivity extends AppCompatActivity implements ConectarAlServidor.AsyncResponse, loginfragment.OnFragmentInteractionListener , registro.RegistroListener {

    private SharedPreferences prefs;
    private Context contexto = this;
    private  Boolean eslogin = false;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_registro);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.labarra);
        setSupportActionBar(myToolbar);

        prefs = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.addTab(tabLayout.newTab().setText("Iniciar sesión"));
        tabLayout.addTab(tabLayout.newTab().setText("Registrarse"));


        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());

        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
             //   Toast.makeText(LoginRegistroActivity.this, "Selected -> "+tab.getText(), Toast.LENGTH_SHORT).show();
               int position = tab.getPosition();
                viewPager.setCurrentItem(position);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
               // Toast.makeText(LoginRegistroActivity.this, "Unselected -> "+tab.getText(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
               // Toast.makeText(LoginRegistroActivity.this, "Reselected -> "+tab.getText(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void guardarpreferencias(String correo, String password) {
        saveOnPreferences(correo,password);
    }

    @Override
    public void login(String correo, String password) {
        if (logeo(correo,password)){
            eslogin = true;
            String php = "https://134.209.235.115/ebracamonte001/WEB/login.php";
            JSONObject parametrosJSON = new JSONObject();
            try {
                parametrosJSON.put("email",correo);
                parametrosJSON.put("Password",password);

                ConectarAlServidor bdremota = new ConectarAlServidor(contexto,parametrosJSON, php);
                bdremota.execute();
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }

    @Override
    public String getemailprefrencias() {
        return prefs.getString("email", "");
    }

    @Override
    public String getcontraprefrencias() {
        return prefs.getString("pass", "");
    }

    @Override
    public void registrarenbdremota(String nombreusuarios, String password, String email, String phone, String direc){
        String php = "https://134.209.235.115/ebracamonte001/WEB/usuarios.php";
        JSONObject parametrosJSON1 = new JSONObject();
        try {
            parametrosJSON1.put("Nombre",nombreusuarios);
            parametrosJSON1.put("Password",password);
            parametrosJSON1.put("email",email);
            parametrosJSON1.put("telefono",phone);
            parametrosJSON1.put("direc",direc);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ConectarAlServidor bd = new ConectarAlServidor(contexto,parametrosJSON1, php);
        bd.execute();

    }

    @Override
    public void processFinish(String output) throws ParseException {
        if(eslogin){
            Toast.makeText(LoginRegistroActivity.this, "Aqui va el intent hacia otra activity", Toast.LENGTH_SHORT).show();
        }
        Toast.makeText(LoginRegistroActivity.this, "Usuario Registrado", Toast.LENGTH_SHORT).show();
    }



    private boolean logeo(String email, String password){

        if(!emailvalido(email)){
            Toast.makeText(this,"email no valido", Toast.LENGTH_LONG).show();
            return false;
        } else if (!passwordvalido(password)){
            Toast.makeText(this,"contraseña no válida",Toast.LENGTH_LONG).show();
            return false;
        } else{
            return true;

        }
    }
    //validamos el email
    private boolean  emailvalido(String email){
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    // método para comprobar si el password tiene más de 4 caracteres
    private boolean  passwordvalido(String password){
        return (password.length() > 4);
    }

    private void saveOnPreferences(String email, String password){
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("email", email);
            editor.putString("pass",password);
            editor.commit();
            editor.apply();

    }




}
