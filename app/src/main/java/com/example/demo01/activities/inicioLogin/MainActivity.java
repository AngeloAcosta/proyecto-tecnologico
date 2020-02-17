package com.example.demo01.activities.inicioLogin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.demo01.R;
import com.example.demo01.activities.actividad.ActividadActivity;
import com.example.demo01.activities.dialog.CargandoDialogFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

     EditText mtxtEmail, mtxtClave;
     TextView mtxtRecuperar, mtxtRegistrar;
     Button mbtnIngresar;

     String email = "";
     String clave = "";

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final CargandoDialogFragment cargandoDialogFragment = new CargandoDialogFragment(MainActivity.this);
        cargandoDialogFragment.startLoadingDialog();
        alInicio();
        cargandoDialogFragment.dismissDialog();

        FirebaseApp.initializeApp(MainActivity.this);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        mtxtEmail = (EditText) findViewById(R. id.txtEmail);
        mtxtClave = (EditText) findViewById(R. id.txtClave);
        mtxtRecuperar = (TextView) findViewById(R. id.txtRecuperar);

        mbtnIngresar = (Button) findViewById(R. id.btnIngresar);
        mtxtRegistrar = (TextView) findViewById(R. id.txtRegistrar);

        mbtnIngresar.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                email = mtxtEmail.getText().toString();
                clave = mtxtClave.getText().toString();

                if(!email.isEmpty() && !clave.isEmpty()){
                    mbtnIngresar.setEnabled(false);
                    mAuth.signInWithEmailAndPassword(email,clave).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                startActivity(new Intent(MainActivity.this, ActividadActivity.class));
                                finish();
                                Toast.makeText(MainActivity.this, "Bienvenido "+email, Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(MainActivity.this, "No se pudo iniciar sesion", Toast.LENGTH_SHORT).show();
                                Log.d("",""+task);
                                mbtnIngresar.setEnabled(true);
                            }
                        }
                    });
                } else {
                    Toast.makeText(MainActivity.this, "Rellene todos los campos", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mtxtRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent( MainActivity.this, RegistrarActivity.class);
                startActivity(i);
            }
        });

        mtxtRecuperar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, RecuperarCuentaActivity.class);
                startActivity(i);
            }
        });
    }

    private void alInicio(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // User is signed in
            startActivity(new Intent(MainActivity.this, ActividadActivity.class));
            Toast.makeText(this, "Bienvenido", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            // No user is signed in
            Toast.makeText(this, "Hola, inicia sesion!! o crea una cuenta.", Toast.LENGTH_SHORT).show();
        }
    }
}
