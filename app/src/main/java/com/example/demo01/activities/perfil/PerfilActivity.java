package com.example.demo01.activities.perfil;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.demo01.R;
import com.example.demo01.activities.actividad.ActividadActivity;
import com.example.demo01.activities.dialog.CargandoDialogFragment;
import com.example.demo01.activities.familia.FamiliaActivity;
import com.example.demo01.activities.inicioLogin.MainActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class PerfilActivity extends AppCompatActivity {

    Button mbtnCerrarSesion, mbtnEditar;
    TextView mNombres, mApellidos, mEmail, mNacimiento;
    ImageView mPerfil;

    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseFirestore db;
    FirebaseStorage storage;
    StorageReference storageRef;

    String nombres = "";
    String apaterno = "";
    String amaterno = "";
    String apellidos = apaterno + " " + amaterno;
    String email = "";
    String nacimiento = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        mAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        final CargandoDialogFragment cargandoDialogFragment = new CargandoDialogFragment(PerfilActivity.this);
        cargandoDialogFragment.startLoadingDialog();
        cargarDatos();
        cargandoDialogFragment.dismissDialog();
        mbtnCerrarSesion = (Button) findViewById(R. id.btnCerrarSesion);

        ImageButton mbtnActividad = findViewById(R.id.btnActividad);
        ImageButton mbtnFamilia = findViewById(R.id.btnFamilia);
        ImageButton mbtnPerfil = findViewById(R.id.btnPerfil);

        mPerfil = (ImageView) findViewById(R.id.imgPerfil);

        mbtnEditar = (Button) findViewById(R.id.btnEditarPerfil);

        mNombres = (TextView) findViewById(R.id.txtNombre);
        mApellidos = (TextView) findViewById(R.id.txtApellidoCompleto);
        mEmail = (TextView) findViewById(R.id.txtEmail);
        mNacimiento = (TextView) findViewById(R.id.txtNacimiento2);

        mbtnEditar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    startActivity(new Intent(PerfilActivity.this, EditarPerfilActivity.class));
                    finish();
            }
        });

        mbtnCerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mAuth.signOut();
                    startActivity(new Intent(PerfilActivity.this, MainActivity.class));
                    finish();
                    Toast.makeText(PerfilActivity.this, "Hasta pronto!!", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(PerfilActivity.this, "Ocurrio un error", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });

        mbtnActividad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PerfilActivity.this, ActividadActivity.class));
                finish();
            }
        });

        mbtnFamilia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PerfilActivity.this, FamiliaActivity.class));
                finish();
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
                            email = documentSnapshot.getString("email");
                            nacimiento = documentSnapshot.getString("nacimiento");

                            apellidos = apaterno + " " +amaterno;
                            mNombres.setText(nombres);
                            mApellidos.setText(apellidos);
                            mEmail.setText(email);
                            mNacimiento.setText(nacimiento);

                        } else {
                            Toast.makeText(PerfilActivity.this, "Error"+documentSnapshot, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                storageRef.child("imagen/"+uid+"/perfil.jpg")
                        .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        // Got the download URL for 'users/me/profile.png'
                        //filePerfil = uri;
                        //Bitmap bitmap = BitmapFactory.decodeFile(uri);
                        Glide.with(PerfilActivity.this).load(uri).into(mPerfil);

                        //mImgPerfil.set(stringUri);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                        Toast.makeText(PerfilActivity.this, "No se pudo cargar la foto de perfil", Toast.LENGTH_SHORT).show();
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
