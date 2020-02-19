package com.example.demo01.activities.actividad;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.demo01.R;
import com.example.demo01.activities.dialog.CargandoDialogFragment;
import com.example.demo01.activities.models.Actividad;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ActividadSeleccionadaActivity extends AppCompatActivity {

    private static final String TAG = "ActividadSeleccionadaActivity";
    TextView mrecompensa, muriImagen,mgrupoFamiliar, midCreador, mcondicion, mestado, mfecha, mfechaInicio, mfechaFin, mhoraInicio, mhoraFin, mpuntos, mdestino, mprioridad, midActividad, mnombre, mdetalle;
    int ypuntos;
    ImageView mimgActividad;
    Button mbtnReaizado, mbtnVolver, mbtnEliminar, mEditar;

    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseFirestore db;
    FirebaseStorage storage;
    StorageReference storageRef;

    CargandoDialogFragment cargandoDialogFragment;
    Actividad actividad;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actividad_seleccionada);

        mAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        final String uid = user.getUid();

        cargandoDialogFragment = new CargandoDialogFragment(ActividadSeleccionadaActivity.this);
        cargandoDialogFragment.startLoadingDialog();

        mnombre = findViewById(R. id.txtSNombre);
        mdetalle = findViewById(R. id.txtSDetalle);
        mfechaInicio = findViewById(R.id.txtSFechaInicio);
        mfechaFin = findViewById(R.id.txtSFechaFin);
        mhoraInicio = findViewById(R.id.txtSHoraInicio);
        mhoraFin = findViewById(R.id.txtSHoraFin);
        mpuntos = findViewById(R.id.txtSPuntaje);
        mprioridad = findViewById(R.id.txtSPrioridad);
        mimgActividad = findViewById(R.id.imgActividad);

        mbtnReaizado = findViewById(R.id.btnRealizado);
        mbtnVolver = findViewById(R.id.btnVolver);
        mEditar = findViewById(R.id.btnEditarActividad);
        mbtnEliminar = findViewById(R.id.btnEliminarActividad);

        mEditar.setVisibility(View.INVISIBLE);
        mbtnEliminar.setVisibility(View.INVISIBLE);

        mbtnEliminar.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                cargandoDialogFragment.startLoadingDialog();
                db.collection("actividad").document(actividad.getIdActividad())
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @SuppressLint("LongLogTag")
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "DocumentSnapshot successfully deleted!");
                                cargandoDialogFragment.dismissDialog();
                                startActivity(new Intent(ActividadSeleccionadaActivity.this, ActividadActivity.class));
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @SuppressLint("LongLogTag")
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error deleting document", e);
                                cargandoDialogFragment.dismissDialog();
                            }
                        });
                return false;
            }
        });

        Bundle args = getIntent().getExtras();
        assert args != null;
        actividad = (Actividad) args.getSerializable("actividad");
        assert actividad != null;

        mbtnReaizado.setVisibility(View.INVISIBLE);
        if(actividad.getIdDestino().equals(uid)){
            mbtnReaizado.setVisibility(View.VISIBLE);
        }

        mnombre.setText(actividad.getNombre());
        mdetalle.setText(actividad.getDetalle());
        mfechaInicio.setText(actividad.getFechaInicio());
        mfechaFin.setText(actividad.getFechaFin());
        mhoraFin.setText(actividad.getHoraFin());
        mhoraInicio.setText(actividad.getHoraInicio());
        mpuntos.setText(String.valueOf(actividad.getPuntos()));
        mprioridad.setText(actividad.getPrioridad());

        mEditar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                Intent intent = new Intent(ActividadSeleccionadaActivity.this, NuevaActividadActivity.class);

                args.putSerializable("actividadEditar", actividad);
                intent.putExtras(args);
                startActivity(intent);
            }
        });

        if(uid.equals(actividad.getIdCreador())) {
            mEditar.setVisibility(View.VISIBLE);
            mbtnEliminar.setVisibility(View.VISIBLE);
            cargandoDialogFragment.dismissDialog();
        }

        storageRef.child("actividad/" + actividad.getIdActividad() + "/imagen.jpg")
                .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(ActividadSeleccionadaActivity.this).load(uri).into(mimgActividad);
                cargandoDialogFragment.dismissDialog();
            }
        });

        if(actividad.getEstado().equals("REALIZADO")){
            mbtnReaizado.setEnabled(false);
            mbtnReaizado.setText("YA COMPLETASTE ESTA ACTIVIDAD");

        }

        mbtnReaizado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cargandoDialogFragment.startLoadingDialog();
                DocumentReference actividadRef = db.collection("actividad").document(actividad.getIdActividad());
                actividadRef
                        .update("estado", "REALIZADO")
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @SuppressLint("LongLogTag")
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "DocumentSnapshot successfully updated!");
                                DocumentReference usuarioRef = db.collection("usuario").document(uid);
                                usuarioRef
                                        .update("puntos", FieldValue.increment(actividad.getPuntos()))
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "DocumentSnapshot successfully updated!");
                                                mbtnReaizado.setEnabled(false);
                                                cargandoDialogFragment.dismissDialog();
                                                Toast.makeText(ActividadSeleccionadaActivity.this, "Acabas de ganar "+actividad.getPuntos(), Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(ActividadSeleccionadaActivity.this, ActividadActivity.class));
                                                finish();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w(TAG, "Error updating document", e);
                                                cargandoDialogFragment.dismissDialog();
                                            }
                                        });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @SuppressLint("LongLogTag")
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error updating document", e);
                                cargandoDialogFragment.dismissDialog();
                            }
                        });

                Actividad actividadRealizado = new Actividad(
                        actividad.getDestino(),
                        actividad.getIdActividad(),
                        actividad.getNombre(),
                        actividad.getDetalle(),
                        "AUNNINGUNA",
                        actividad.getUriImagen(),
                        actividad.getIdGrupoFamiliar(),
                        actividad.getIdCreador(),
                        "CUMPLIDO",
                        actividad.getEstado(),
                        actividad.getIdDestino(),
                        actividad.getPrioridad(),
                        actividad.getFechaInicio(),
                        actividad.getFechaFin(),
                        actividad.getHoraInicio(),
                        actividad.getHoraFin(),
                        actividad.getFecha(),
                        actividad.getPuntos());

                DocumentReference docRef = db.collection("realizadas").document();
                        docRef.set(actividadRealizado);
            }
        });

        mbtnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ActividadSeleccionadaActivity.this, ActividadActivity.class));
                finish();
            }
        });

    }
}
