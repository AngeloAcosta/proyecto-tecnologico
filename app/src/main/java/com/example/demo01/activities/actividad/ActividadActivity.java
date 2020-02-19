package com.example.demo01.activities.actividad;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.demo01.activities.dialog.CargandoDialogFragment;
import com.example.demo01.activities.dialog.OpcioneDialogFragment;
import com.example.demo01.activities.models.Actividad;
import com.example.demo01.R;
import com.example.demo01.activities.familia.FamiliaActivity;
import com.example.demo01.activities.models.Familia;
import com.example.demo01.activities.notificacion.NotificacionRealizarActividad;
import com.example.demo01.activities.perfil.PerfilActivity;
import com.example.demo01.activities.recompensa.RecompensaActivity;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class ActividadActivity extends AppCompatActivity implements OpcioneDialogFragment.OpcioneListener{

     Button mbtnCrearActividad, mbtnActividadesCreadas, mbtnRecompensas;
     ImageButton mbtnFamilia, mbtnPerfil, mbtnActividad;
     RecyclerView rv;
     ArrayList<Actividad> actividades;

     FirebaseAuth mAuth;
     FirebaseUser user;
     FirebaseFirestore db;
     FirebaseStorage storage;
     StorageReference storageRef;

    FirestoreRecyclerAdapter actividadAdapter;
    CargandoDialogFragment cargandoDialogFragment;
    FirestoreRecyclerOptions<Actividad> actividadOptionsDestino;

    final static String TAG = "ActividadActivity";
    String dialogOpciones = "";
    String idActividad = "";

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actividad);

        mAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        cargandoDialogFragment = new CargandoDialogFragment(ActividadActivity.this);
        cargandoDialogFragment.startLoadingDialog();

        mbtnCrearActividad = findViewById(R. id.btnCrearActivdad);
        mbtnActividadesCreadas = findViewById(R.id.btnActividadesCreadas);

        mbtnActividad = findViewById(R. id.btnActividad);
        mbtnFamilia = findViewById(R. id.btnFamilia);
        mbtnPerfil = findViewById(R. id.btnPerfil);
        mbtnRecompensas = findViewById(R.id.btnRecompensas);

        mbtnCrearActividad.setVisibility(View.INVISIBLE);
        mbtnActividadesCreadas.setVisibility(View.INVISIBLE);
        mbtnRecompensas.setVisibility(View.INVISIBLE);

        rv = findViewById(R. id.rvActividades);
        rv.setLayoutManager(new LinearLayoutManager(ActividadActivity.this));

        actividades = new ArrayList<>();

        final String uid = user.getUid();

        db.collection("grupoFamiliar").whereEqualTo("idCreador", uid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Familia familia = document.toObject(Familia.class);
                        String creador = familia.getIdCreador();
                        if(creador.equals(uid)){
                            mbtnCrearActividad.setVisibility(View.VISIBLE);
                            mbtnActividadesCreadas.setVisibility(View.VISIBLE);
                            mbtnRecompensas.setVisibility(View.VISIBLE);
                        }
                    }
                    cargandoDialogFragment.dismissDialog();
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                    cargandoDialogFragment.dismissDialog();
                }
            }
        });

        mbtnActividadesCreadas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ActividadActivity.this, ActividadesCreadasActivity.class));
                finish();
            }
        });

        CollectionReference actividadRef = db.collection("actividad");
        Query queryDestino = actividadRef.whereEqualTo("idDestino",uid);

        actividadOptionsDestino = new FirestoreRecyclerOptions.Builder<Actividad>()
                .setQuery(queryDestino, Actividad.class)
                .build();

        actividadAdapter = new FirestoreRecyclerAdapter<Actividad, ActividadViewHolder>(actividadOptionsDestino) {
            @NonNull
            @Override
            public ActividadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_actividad_item, parent, false);
                return new ActividadViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final ActividadViewHolder actividadViewHolder, final int i, @NonNull final Actividad actividad) {

                actividadViewHolder.nombre_item.setText(actividad.getNombre());
                actividadViewHolder.descripcion_item.setText(actividad.getDetalle());
                actividadViewHolder.puntos_item.setText(String.valueOf(actividad.getPuntos()));
                actividadViewHolder.estado_item.setText(actividad.getEstado());


                storageRef.child("actividad/" + actividad.getIdActividad() + "/imagen.jpg")
                        .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(ActividadActivity.this).load(uri).into(actividadViewHolder.imagen_item);
                    }
                });

                actividadViewHolder.mvActividad.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle args = new Bundle();
                        Intent intent = new Intent(ActividadActivity.this, ActividadSeleccionadaActivity.class);

                        args.putSerializable("actividad", actividad);
                        intent.putExtras(args);
                        startActivity(intent);
                    }
                });

//                LocalDateTime myDateObj = LocalDateTime.now();
//                DateTimeFormatter formatHoraMinuto = DateTimeFormatter.ofPattern("HH:mm");
//                DateTimeFormatter formatDia = DateTimeFormatter.ofPattern("E");
//                DateTimeFormatter formatNumDia = DateTimeFormatter.ofPattern("dd");
//
//                String formattedDateHoraMinuto = myDateObj.format(formatHoraMinuto);
//
                Thread t = new Thread() {
                    @Override
                    public void run(){
                        try{
                            while (!isInterrupted()){
                                Thread.sleep(1000);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        long date = System.currentTimeMillis();
                                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                                        String dataString = sdf.format(date);

                                        if(dataString.equals(actividad.getHoraInicio())){
                                            crearNotificacionRealizar();
                                        }

                                        if(dataString.equals("00:00:00")){
                                            if(actividad.getEstado().equals("REALIZADO")){
                                                DocumentReference actividad1UpdateRef = db.collection("actividad").document(actividad.getIdActividad());
                                                actividad1UpdateRef
                                                        .update("estado", "ACTIVO")
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Log.d(TAG, "DocumentSnapshot successfully updated!");
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Log.w(TAG, "Error updating document", e);
                                                            }
                                                        });
                                            }
                                        }
                                    }
                                });
                            }
                        }catch (Exception e){

                        }
                    }
                };
                t.start();
            }

        };

        mbtnCrearActividad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ActividadActivity.this, NuevaActividadActivity.class));
                finish();
            }
        });

        mbtnFamilia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ActividadActivity.this, FamiliaActivity.class));
                finish();
            }
        });

        mbtnPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ActividadActivity.this, PerfilActivity.class));
                finish();
            }
        });

        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(actividadAdapter);

        mbtnRecompensas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ActividadActivity.this, RecompensaActivity.class));
                finish();
            }
        });



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
