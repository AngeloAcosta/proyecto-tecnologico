package com.example.demo01.activities.perfil;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.example.demo01.R;
import com.example.demo01.activities.dialog.CargandoDialogFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class EditarPerfilActivity extends AppCompatActivity {

    Button mCancelar, mGuardar, mCamara, mGaleria;
    EditText mNombres, mAPaterno, mAMaterno, mNacimiento;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 2;
    static final int PICK_IMAGE_REQUEST = 3;
    Uri filePath;

    String currentPhotoPath;

    ImageView mImgPerfil;

    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseFirestore db;
    FirebaseStorage storage;
    StorageReference storageRef;

    //StorageReference imagesRef = storageRef.child("images");

    String nombres = "";
    String apaterno = "";
    String amaterno = "";
    String apellidos = apaterno + " " + amaterno;
    String email = "";
    String nacimiento = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil);

        mAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        final CargandoDialogFragment cargandoDialogFragment = new CargandoDialogFragment(EditarPerfilActivity.this);
        cargandoDialogFragment.startLoadingDialog();
        cargarDatos();
        cargandoDialogFragment.dismissDialog();
        mNombres = (EditText) findViewById(R.id.txtNombres);
        mAPaterno = (EditText) findViewById(R.id.txtAPaterno);
        mAMaterno = (EditText) findViewById(R.id.txtAMaterno);
        mNacimiento = (EditText) findViewById(R.id.txtNacimiento);

        mGuardar = (Button) findViewById(R.id.btnGuardar);
        mCancelar = (Button) findViewById(R.id.btnCancelar);
        mCamara = (Button) findViewById(R.id.btnCamara);
        mGaleria = (Button) findViewById(R.id.btnGaleria);

        mImgPerfil = (ImageView) findViewById(R.id.imgPerfil);

        mGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cargandoDialogFragment.startLoadingDialog();
                guardarDatos();
                cargandoDialogFragment.dismissDialog();
            }
        });

        mCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EditarPerfilActivity.this, PerfilActivity.class));
                finish();
            }
        });

        mCamara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fotoCamara();
            }
        });

        mGaleria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fotoGaleria();
            }
        });
    }

    private void fotoCamara(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(Intent.createChooser(intent,""),REQUEST_TAKE_PHOTO);
    }

    private void fotoGaleria(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Seleccionar Imagen"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                mImgPerfil.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (requestCode == REQUEST_TAKE_PHOTO) {
            filePath = data.getData();
            try {
                Bitmap bitmap = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");

                mImgPerfil.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void guardarDatos(){

        String uid = user.getUid();

        mImgPerfil.setDrawingCacheEnabled(true);
        mImgPerfil.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) mImgPerfil.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 25, baos);
        byte[] dataimg = baos.toByteArray();

        StorageReference mountainsRef = storageRef.child("imagen/"+uid+"/perfil.jpg");

        UploadTask uploadTask = mountainsRef.putBytes(dataimg);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                String uid = user.getUid();
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                Map<String, Object> data = new HashMap<>();
                //data.put("idPadre", id);
                data.put("nombres", mNombres.getText().toString());
                data.put("apellidoPaterno", mAPaterno.getText().toString());
                data.put("apellidoMaterno", mAMaterno.getText().toString());
                data.put("nacimiento",mNacimiento.getText().toString());

                db.collection("usuario").document(uid+"")
                        .set(data, SetOptions.merge())
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                //Log.d(TAG, "DocumentSnapshot successfully updated!");
                                Toast.makeText(EditarPerfilActivity.this, "Guardado", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(EditarPerfilActivity.this, PerfilActivity.class));
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(EditarPerfilActivity.this, "No se pudo guardar", Toast.LENGTH_SHORT).show();
                                //Log.w(TAG, "Error updating document", e);
                            }
                        });
            }
        });

    }

    private void cargarDatos(){
        try {
            if (user != null) {
                String uid = user.getUid();
                db.collection("usuario").document(uid+"").
                        get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()){

                            nombres = documentSnapshot.getString("nombres");
                            apaterno = documentSnapshot.getString("apellidoPaterno");
                            amaterno = documentSnapshot.getString("apellidoMaterno");
                            nacimiento = documentSnapshot.getString("fechaNacimiento");

                            //apellidos = apaterno + " " +amaterno;
                            mNombres.setText(nombres);
                            mAPaterno.setText(apaterno);
                            mAMaterno.setText(amaterno);
                            mNacimiento.setText(nacimiento);

                        } else {
                            Toast.makeText(EditarPerfilActivity.this, "Error"+documentSnapshot, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                //final File localFile = File.createTempFile("Images", "bmp");
                storageRef.child("imagen/"+uid+"/perfil.jpg")
                        .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        // Got the download URL for 'users/me/profile.png'
                        //filePerfil = uri;
                        //Bitmap bitmap = BitmapFactory.decodeFile(uri);
                        Glide.with(EditarPerfilActivity.this).load(uri).into(mImgPerfil);

                        //mImgPerfil.set(stringUri);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors

                        Toast.makeText(EditarPerfilActivity.this, "No se pudo cargar la foto de perfil", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {

                Toast.makeText(this, "algo paso " + user, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e){
            Toast.makeText(this, "Error: "+e, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
