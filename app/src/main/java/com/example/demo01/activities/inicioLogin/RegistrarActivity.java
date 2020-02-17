package com.example.demo01.activities.inicioLogin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.demo01.R;
import com.example.demo01.activities.actividad.ActividadActivity;
import com.example.demo01.activities.dialog.CargandoDialogFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RegistrarActivity extends AppCompatActivity {

    private EditText mNombres, mAPaterno, mAMaterno, mEmail, mClave, mRepetirClave;
    private Button mRegistrar;

    CargandoDialogFragment cargandoDialogFragment;

    String nombres = "";
    String aPaterno = "";
    String aMaterno = "";
    String email = "";
    String clave = "";
    String repetirClave;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar);

        FirebaseApp.initializeApp(RegistrarActivity.this);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        cargandoDialogFragment = new CargandoDialogFragment(RegistrarActivity.this);

        mNombres = (EditText)findViewById(R. id.txtNombres);
        mAPaterno = (EditText)findViewById(R. id.txtAPaterno);
        mAMaterno = (EditText)findViewById(R. id.txtAMaterno);
        mEmail = (EditText)findViewById(R. id.txtEmail);
        mClave = (EditText)findViewById(R. id.txtClave);
        mRegistrar = (Button)findViewById(R. id.btnRegistrar);
        mRepetirClave = (EditText)findViewById(R. id.txtRepetirClave);

        mRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cargandoDialogFragment.startLoadingDialog();
                nombres = mNombres.getText().toString();
                aPaterno = mAPaterno.getText().toString();
                aMaterno = mAMaterno.getText().toString();
                email = mEmail.getText().toString();
                clave = mClave.getText().toString();
                repetirClave = mRepetirClave.getText().toString();

                if(!nombres.isEmpty() && !aPaterno.isEmpty() && !aMaterno.isEmpty() && !email.isEmpty() && !clave.isEmpty()){
                    if(clave.length() >= 6){
                        if(clave.equals(repetirClave)){
                            registrarUsuario();
                        } else {
                            cargandoDialogFragment.dismissDialog();
                            Toast.makeText(RegistrarActivity.this, "Las claves no coinciden.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        cargandoDialogFragment.dismissDialog();

                        Toast.makeText(RegistrarActivity.this, "la clave debe tener como minimo 6 digitos", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    cargandoDialogFragment.dismissDialog();

                    Toast.makeText(RegistrarActivity.this, "Debe completar todos los campos", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void registrarUsuario(){
        mAuth.createUserWithEmailAndPassword(email, clave)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            String id = Objects.requireNonNull(user).getUid();
                            agregarDatos(id);
                            //Toast.makeText(RegistrarActivity.this, "Usuario creado.", Toast.LENGTH_SHORT).show();
                        } else {
                            cargandoDialogFragment.dismissDialog();
                            Toast.makeText(RegistrarActivity.this, "No se pudo registrar este usuario, intentelo nuevamente", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void agregarDatos(String id){
        // Add a new document with a generated id.
        //Toast.makeText(RegistrarActivity.this, "agregando datos.", Toast.LENGTH_SHORT).show();
        Map<String, Object> data = new HashMap<>();
        data.put("idUsuario",id);
        data.put("nombres", nombres);
        data.put("apellidoPaterno", aPaterno);
        data.put("apellidoMaterno", aMaterno);
        data.put("email", email);
        data.put("clave", clave);
        data.put("fechaCreacion", new Timestamp(new Date()));

        db.collection("usuario").document(id+"")
                .set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                        Toast.makeText(RegistrarActivity.this, "Datos agregados", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegistrarActivity.this, ActividadActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Log.w(TAG, "Error adding document", e);
                        cargandoDialogFragment.dismissDialog();
                        Toast.makeText(RegistrarActivity.this, "Error al agregar los datos", Toast.LENGTH_SHORT).show();
                    }
                });

    }

}
