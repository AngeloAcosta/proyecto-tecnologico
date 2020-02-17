package com.example.demo01.activities.familia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.demo01.R;
import com.example.demo01.activities.dialog.CargandoDialogFragment;
import com.example.demo01.activities.perfil.PerfilActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CrearFamiliaActivity extends AppCompatActivity {

    EditText mNombreFamilia, mDescripcionFamilia, mClaveFamilia;
    ImageView mImgFamilia;
    Button mCrearFamilia, mCancelar;

    static final int PICK_IMAGE_REQUEST = 1;
    Uri filePath;

    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseFirestore db;
    FirebaseStorage storage;
    StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_familia);

        mAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        final CargandoDialogFragment cargandoDialogFragment = new CargandoDialogFragment(CrearFamiliaActivity.this);

        mNombreFamilia = (EditText) findViewById(R.id.txtNombreFamilia);
        mDescripcionFamilia = (EditText) findViewById(R.id.txtDescripcionFamilia);
        mClaveFamilia = (EditText) findViewById(R.id.txtClaveFamiliar);

        mImgFamilia = (ImageView) findViewById(R.id.imgFamilia);

        mCrearFamilia = (Button) findViewById(R.id.btnCrearFamilia);
        mCancelar = (Button) findViewById(R.id.btnCancelar);

        mImgFamilia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                agregarImagen();
            }
        });

        mCrearFamilia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAllEnableFalse();
                cargandoDialogFragment.startLoadingDialog();
                final String uid = user.getUid();
                //REFERENCIA Y UID DEL GRUPO FAMILIAR
                final DocumentReference grupoFamiliaRef = db.collection("grupoFamiliar").document();
                final String uidGrupoFamiliar = grupoFamiliaRef.getId();
                //REFERENCIA DE ALMACENAMIENTO
                final StorageReference imagenRef = storageRef.child("grupofamiliar/"+uidGrupoFamiliar+"/portada.jpg");

                //COMBIRTIENDO LA IMAGEN EN BIT
                mImgFamilia.setDrawingCacheEnabled(true);
                mImgFamilia.buildDrawingCache();

                Bitmap bitmap = ((BitmapDrawable) mImgFamilia.getDrawable()).getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 25, baos);

                byte[] dataimg = baos.toByteArray();
                //SUBIENDO LA IMAGEN A FIRESTORE
                final UploadTask uploadTask = imagenRef.putBytes(dataimg);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        String nombre = mNombreFamilia.getText().toString();
                        String descripcion = mDescripcionFamilia.getText().toString();
                        String clave = mClaveFamilia.getText().toString();
                        String uriFamilia = uploadTask.getResult().toString();

                        Map<String, Object> data = new HashMap<>();
                        data.put("nombre", nombre);
                        data.put("descripcion", descripcion);
                        data.put("clave",clave);
                        data.put("idCreador", uid);
                        data.put("uriFamilia", uriFamilia);
                        data.put("fecha", new Timestamp(new Date()));
                        data.put("idFamilia", uidGrupoFamiliar);
                        data.put("grupoFamiliar", Arrays.asList(""+uid));

                        if(!nombre.isEmpty() && !descripcion.isEmpty() && !clave.isEmpty()) {
                            crearFamilia(data, grupoFamiliaRef, uid, uidGrupoFamiliar);
                            cargandoDialogFragment.dismissDialog();
                        } else {
                            cargandoDialogFragment.dismissDialog();
                            setAllEnableTrue();
                            Toast.makeText(CrearFamiliaActivity.this, "Tiene que poner un Nombre, Descripcion y Clave a su Familia.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        mCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelar();
            }
        });
    }

    private void agregarImagen(){
        fotoGaleria();
    }

    private void crearFamilia(Map data, DocumentReference grupoFamiliaRef, final String uid, final String uidFamilia){
        grupoFamiliaRef.set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        agregarIntegranteFamilia(uidFamilia, uid);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        setAllEnableTrue();
                        Toast.makeText(CrearFamiliaActivity.this, "No se pudo guardar", Toast.LENGTH_SHORT).show();
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
        data.put("funcion","padre");
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
                        setAllEnableTrue();
                        Toast.makeText(CrearFamiliaActivity.this, "No se pudo guardar", Toast.LENGTH_SHORT).show();
                        //Log.w(TAG, "Error updating document", e);
                    }
                });
    }

    private void agregarGrupoFamiliar(final String uidFamilia, final String uid) {
        final DocumentReference gruposFamiliaresRef = db.collection("usuario").document("" + uid).collection("gruposFamiliares").document();

        Map<String, Object> data = new HashMap<>();
        data.put("idGrupo", uidFamilia);
        data.put("fecha", new Timestamp(new Date()));
        data.put("condicion", "creador");
        data.put("rango", "padre");

        gruposFamiliaresRef.set(data, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                agregarDatosDeGrupo(uidFamilia,uid);
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        setAllEnableTrue();
                        Toast.makeText(CrearFamiliaActivity.this, "No se pudo guardar", Toast.LENGTH_SHORT).show();
                        //Log.w(TAG, "Error updating document", e);
                    }
                });
    }

    private void agregarDatosDeGrupo(final String uidFamilia, final String uid){
        final DocumentReference grupoUsuarioRef = db.collection("usuario").document("" + uid);
        Map<String, Object> data = new HashMap<>();
        data.put("grupoFamiliar", FieldValue.arrayUnion(""+uidFamilia));

        grupoUsuarioRef.update(data).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(CrearFamiliaActivity.this, "Familia Creada", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(CrearFamiliaActivity.this, FamiliaActivity.class));
                finish();
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        setAllEnableTrue();
                        Toast.makeText(CrearFamiliaActivity.this, "No se pudo guardar", Toast.LENGTH_SHORT).show();
                        //Log.w(TAG, "Error updating document", e);
                    }
                });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                mImgFamilia.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            Toast.makeText(this, "Algo paso con la imagen, intentelo nuevamente.", Toast.LENGTH_SHORT).show();
        }
    }

    private void cancelar(){
        startActivity(new Intent(CrearFamiliaActivity.this, FamiliaActivity.class));
        finish();
    }

    private void fotoGaleria(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Seleccionar Imagen"), PICK_IMAGE_REQUEST);
    }

    private void setAllEnableTrue(){
        mNombreFamilia.setEnabled(true);
        mDescripcionFamilia.setEnabled(true);
        mClaveFamilia.setEnabled(true);
        mImgFamilia.setEnabled(true);
        mCrearFamilia.setEnabled(true);
        mCancelar.setEnabled(true);
    }

    private void setAllEnableFalse(){
        mNombreFamilia.setEnabled(false);
        mDescripcionFamilia.setEnabled(false);
        mClaveFamilia.setEnabled(false);
        mImgFamilia.setEnabled(false);
        mCrearFamilia.setEnabled(false);
        mCancelar.setEnabled(false);
    }
}