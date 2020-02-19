package com.example.demo01.activities.familia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.demo01.R;
import com.example.demo01.activities.actividad.ActividadActivity;
import com.example.demo01.activities.dialog.CargandoDialogFragment;
import com.example.demo01.activities.dialog.OpcioneDialogFragment;
import com.example.demo01.activities.models.Actividad;
import com.example.demo01.activities.models.Familia;
import com.example.demo01.activities.models.Usuario;
import com.example.demo01.activities.notificacion.NotificacionRealizarActividad;
import com.example.demo01.activities.recompensa.RecompensaActivity;
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
import java.util.ArrayList;

public class FamiliarSeccionadoActivity extends AppCompatActivity implements OpcioneDialogFragment.OpcioneListener {

    TextView mtxtNombre, mtxtEmail, mtxtPuntos;
    RecyclerView rv;
    Button mVolver, mRetirar, mHistorial;
    ImageView mImgPerfil;

    ArrayList<Actividad> actividades;

    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseFirestore db;
    FirebaseStorage storage;
    StorageReference storageRef;
    FirestoreRecyclerAdapter usuarioAdapter;

    FirestoreRecyclerAdapter actividadAdapter;
    CargandoDialogFragment cargandoDialogFragment;
    FirestoreRecyclerOptions<Actividad> actividadOptionsDestino;

    final static String TAG = "ActividadActivity";
    String dialogOpciones = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_familiar_seccionado);

        mAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        final String uid = user.getUid();

        cargandoDialogFragment = new CargandoDialogFragment(FamiliarSeccionadoActivity.this);
        cargandoDialogFragment.startLoadingDialog();

        mtxtNombre = findViewById(R.id.txtNombre);
        mtxtEmail = findViewById(R.id.txtEmail);
        mtxtPuntos = findViewById(R.id.txtPuntos);

        mVolver = findViewById(R.id.btnVolver);
        mRetirar = findViewById(R.id.btnRetirar);
        mHistorial = findViewById(R.id.btnHistorial);

        Bundle args = getIntent().getExtras();
        assert args != null;
        final Usuario usuario = (Usuario) args.getSerializable("usuarioSelecto");
        final Familia familia = (Familia) args.getSerializable("grupoSelecto");
        assert usuario != null;
        assert familia != null;

        mRetirar.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                DocumentReference grupoDeleteRef = db.collection("grupoFamiliar").document(familia.getIdFamilia());
                grupoDeleteRef.update("miembros", FieldValue.arrayRemove(usuario.getIdUsuario()));
                DocumentReference usuarioDeleteRef = db.collection("grupoFamiliar").document(familia.getIdFamilia());
                usuarioDeleteRef.update("grupoFamiliar", FieldValue.arrayRemove(familia.getIdFamilia()));
                return false;
            }
        });

        mHistorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FamiliarSeccionadoActivity.this, RecompensaActivity.class));
                finish();
            }
        });

        mVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();

                Intent intent = new Intent(FamiliarSeccionadoActivity.this, MiGrupoFamiliarActivity.class);

                args.putSerializable("familia", familia);
                intent.putExtras(args);
                startActivity(intent);

            }
        });

        mImgPerfil = findViewById(R.id.imgPerfil);

        rv = findViewById(R.id.rvActividadesCumplidas);
        rv.setLayoutManager(new LinearLayoutManager(FamiliarSeccionadoActivity.this));

        mtxtNombre.setText(usuario.getNombres()+" "+usuario.getApellidoPaterno()+" "+usuario.getApellidoMaterno());
        mtxtEmail.setText(usuario.getEmail());
        mtxtPuntos.setText(usuario.getPuntos()+"");

        storageRef.child("imagen/" + usuario.getIdUsuario() + "/perfil.jpg")
                .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(FamiliarSeccionadoActivity.this).load(uri).into(mImgPerfil);
                cargandoDialogFragment.dismissDialog();
            }
        });

        CollectionReference actividadRef = db.collection("actividad");
        Query queryDestino = actividadRef.whereEqualTo("idDestino",usuario.getIdUsuario());
        Query queryRealizado = queryDestino.whereEqualTo("estado","REALIZADO");
        actividadOptionsDestino = new FirestoreRecyclerOptions.Builder<Actividad>()
                .setQuery(queryRealizado, Actividad.class)
                .build();

        actividadAdapter = new FirestoreRecyclerAdapter<Actividad, FamiliarSeccionadoActivity.ActividadViewHolder>(actividadOptionsDestino) {
            @NonNull
            @Override
            public FamiliarSeccionadoActivity.ActividadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_actividad_item, parent, false);
                return new FamiliarSeccionadoActivity.ActividadViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final FamiliarSeccionadoActivity.ActividadViewHolder actividadViewHolder, final int i, @NonNull final Actividad actividad) {

                actividadViewHolder.nombre_item.setText(actividad.getNombre());
                actividadViewHolder.descripcion_item.setText(actividad.getDetalle());
                actividadViewHolder.puntos_item.setText(String.valueOf(actividad.getPuntos()));
                actividadViewHolder.estado_item.setText(actividad.getEstado());


                storageRef.child("actividad/" + actividad.getIdActividad() + "/imagen.jpg")
                        .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(FamiliarSeccionadoActivity.this).load(uri).into(actividadViewHolder.imagen_item);
                    }
                });

                actividadViewHolder.mvActividad.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle args = new Bundle();
                        //Intent intent = new Intent(FamiliarSeccionadoActivity.this, ActividadSeleccionadaActivity.class);

                        args.putSerializable("actividad", actividad);
                        //intent.putExtras(args);
                        //startActivity(intent);
                    }
                });


            }

        };

        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(actividadAdapter);

    }

    @Override
    public void onPositiveButtonClicked(String string, int position) {
        dialogOpciones = string;
    }

    @Override
    public void onNegativeButtonClicked() {

    }

    private class ActividadViewHolder extends RecyclerView.ViewHolder{

        private TextView nombre_item, descripcion_item, estado_item, puntos_item;
        private ImageView imagen_item;
        private View mvActividad;
        //private Button unirse_item;

        ActividadViewHolder(@NonNull final View itemView) {
            super(itemView);

            nombre_item = itemView.findViewById(R.id.txtNombre);
            imagen_item = itemView.findViewById(R.id.imgActividad);
            descripcion_item = itemView.findViewById(R.id.txtDescripcion);
            estado_item = itemView.findViewById(R.id.txtPrioridad);
            puntos_item = itemView.findViewById(R.id.txtPuntosRecompaensa);
            mvActividad = itemView.findViewById(R.id.vActividad);

        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        actividadAdapter.stopListening();
    }

    @Override
    protected void onStart() {
        super.onStart();
        actividadAdapter.startListening();
    }

    private void crearNotificacionRealizar(){
        Intent fullScreenIntent = new Intent(this, NotificacionRealizarActividad.class);
        PendingIntent fullScreenPendingIntent = PendingIntent.getBroadcast(this, 0,
                fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        assert alarmManager != null;
        long timeMillis = 1000 * 10;
        alarmManager.set(AlarmManager.RTC_WAKEUP,System.currentTimeMillis(), fullScreenPendingIntent);
    }

}
