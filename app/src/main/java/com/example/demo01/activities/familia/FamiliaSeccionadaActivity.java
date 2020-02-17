package com.example.demo01.activities.familia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.demo01.R;
import com.example.demo01.activities.models.Familia;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FamiliaSeccionadaActivity extends AppCompatActivity {

    TextView mnombre,mdescripcion,mclaveingresada;
    ImageView mimgfamilia;
    Button mbtnCancelar,mbtnUnirme;

    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseFirestore db;
    FirebaseStorage storage;
    StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_familia_seccionada);

        mAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        mnombre = findViewById(R.id.txtNombreFamilia);
        mdescripcion = findViewById(R.id.txtDescripcionFamilia);
        mclaveingresada = findViewById(R.id.txtClaveIngresada);
        mimgfamilia = findViewById(R.id.imgFamilia);

        mbtnCancelar = findViewById(R.id.btnCancelar);
        mbtnUnirme = findViewById(R.id.btnUnirme);

        Bundle args = getIntent().getExtras();
        assert args != null;
        final Familia familia = (Familia) args.getSerializable("familia");
        assert familia != null;
        mnombre.setText(familia.getNombre());
        mdescripcion.setText(familia.getDescripcion());
        
        storageRef.child("grupofamiliar/"+familia.getIdFamilia()+"/portada.jpg")
                .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(FamiliaSeccionadaActivity.this).load(uri).into(mimgfamilia);
            }
        });

        final String uid = user.getUid();

        mbtnUnirme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String claveFamilia = familia.getClave();
                String claveIngresada = mclaveingresada.getText().toString();

                if (claveFamilia.equals(claveIngresada)) {
                    agregarDatosDeGrupo(""+familia.getIdFamilia(),""+uid);
                } else {
                    Toast.makeText(FamiliaSeccionadaActivity.this, "La clave no concuerda con la familia seleccionada", Toast.LENGTH_SHORT).show();
                }
            }
        });


        mbtnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelar();
            }
        });
    }

    private void agregarDatosDeGrupo(final String uidFamilia, final String uid){
        DocumentReference grupoUsuarioRef = db.collection("usuario").document("" + uid);
        Map<String, Object> data = new HashMap<>();
        data.put("grupoFamiliar", FieldValue.arrayUnion(""+uidFamilia));

        grupoUsuarioRef.update(data).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                DocumentReference grupoFamiiaRef = db.collection("grupoFamiliar").document("" + uidFamilia);
                Map<String, Object> data = new HashMap<>();
                data.put("miembros", FieldValue.arrayUnion(""+uid));

                grupoFamiiaRef.update(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        agregarIntegranteFamilia(uidFamilia, uid);
                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(FamiliaSeccionadaActivity.this, "No se pudo guardar", Toast.LENGTH_SHORT).show();
                                //Log.w(TAG, "Error updating document", e);
                            }
                        });

            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(FamiliaSeccionadaActivity.this, "No se pudo guardar", Toast.LENGTH_SHORT).show();
                        //Log.w(TAG, "Error updating document", e);
                    }
                });

    }

    private void agregarIntegranteFamilia(final String uidFamilia, final String uid){
        //CREANDO REFERENCIA A "MIEMBROS"
        final DocumentReference grupoFamiiaRef = db.collection("grupoFamiliar").document(uidFamilia+"");
        final DocumentReference integranteFamiliaRef = grupoFamiiaRef.collection("miembros").document(uid+"");

        Map<String, Object> data = new HashMap<>();
        data.put("tipo","creador");
        data.put("funcion","integrante");
        data.put("fecha", new Timestamp(new Date()));
        data.put("idMiembro",uid);

        integranteFamiliaRef.set(data, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Log.d(TAG, "DocumentSnapshot successfully updated!");
                        agregarGrupoFamiliar(uidFamilia, uid);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(FamiliaSeccionadaActivity.this, "No se pudo guardar", Toast.LENGTH_SHORT).show();
                        //Log.w(TAG, "Error updating document", e);
                    }
                });
    }

    private void agregarGrupoFamiliar(final String uidFamilia, final String uid) {
        final DocumentReference gruposFamiliaresRef = db.collection("usuario").document("" + uid).collection("gruposFamiliares").document();

        Map<String, Object> data = new HashMap<>();
        data.put("idGrupo", uidFamilia);
        data.put("fecha", new Timestamp(new Date()));
        data.put("condicion", "ejecutor");
        data.put("rango", "padre");

        gruposFamiliaresRef.set(data, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(FamiliaSeccionadaActivity.this, "Bienvenido al grupo", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(FamiliaSeccionadaActivity.this, FamiliaActivity.class));
                finish();
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(FamiliaSeccionadaActivity.this, "No se pudo guardar", Toast.LENGTH_SHORT).show();
                        //Log.w(TAG, "Error updating document", e);
                    }
                });
    }

    private void cancelar(){
        startActivity(new Intent(FamiliaSeccionadaActivity.this, ListaFamiliasActivity.class));
        finish();
    }
}
