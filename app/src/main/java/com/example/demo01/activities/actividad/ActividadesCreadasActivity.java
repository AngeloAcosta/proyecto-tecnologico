package com.example.demo01.activities.actividad;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.demo01.R;
import com.example.demo01.activities.models.Actividad;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class ActividadesCreadasActivity extends AppCompatActivity {

    Button mbtnVolver;
    RecyclerView rv;

    ArrayList<Actividad> actividades;

    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseFirestore db;
    FirebaseStorage storage;
    StorageReference storageRef;

    FirestoreRecyclerAdapter actividadAdapter;

    final static String TAG = "ActividadesCreadasActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actividades_creadas);

        mAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        mbtnVolver = findViewById(R.id.btnVolver);

        rv = findViewById(R. id.rvActividadesCreadas);
        rv.setLayoutManager(new LinearLayoutManager(ActividadesCreadasActivity.this));
        actividades = new ArrayList<>();

        final String uid = user.getUid();

        CollectionReference actividadRef = db.collection("actividad");
        Query queryDestino = actividadRef.whereEqualTo("idCreador",uid);

        final FirestoreRecyclerOptions<Actividad> actividadOptionsCreador = new FirestoreRecyclerOptions.Builder<Actividad>()
                .setQuery(queryDestino, Actividad.class)
                .build();

        actividadAdapter = new FirestoreRecyclerAdapter<Actividad, ActividadesCreadasActivity.ActividadViewHolder>(actividadOptionsCreador) {
            @NonNull
            @Override
            public ActividadesCreadasActivity.ActividadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_actividad_item, parent, false);
                return new ActividadesCreadasActivity.ActividadViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final ActividadesCreadasActivity.ActividadViewHolder actividadViewHolder, final int i, @NonNull final Actividad actividad) {

                actividadViewHolder.nombre_item.setText(actividad.getNombre());
                actividadViewHolder.descripcion_item.setText(actividad.getDetalle());
                actividadViewHolder.puntos_item.setText(String.valueOf(actividad.getPuntos()));
                actividadViewHolder.estado_item.setText(actividad.getEstado());


                storageRef.child("actividad/" + actividad.getIdActividad() + "/imagen.jpg")
                        .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(ActividadesCreadasActivity.this).load(uri).into(actividadViewHolder.imagen_item);
                    }
                });

                actividadViewHolder.imagen_item.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle args = new Bundle();
                        Intent intent = new Intent(ActividadesCreadasActivity.this, ActividadSeleccionadaActivity.class);

                        args.putSerializable("actividad", actividad);
                        intent.putExtras(args);
                        startActivity(intent);
                    }
                });
            }

        };

        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(actividadAdapter);

        mbtnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ActividadesCreadasActivity.this, ActividadActivity.class));
                finish();
            }
        });
    }

    private class ActividadViewHolder extends RecyclerView.ViewHolder{

        private TextView nombre_item, descripcion_item, estado_item, puntos_item;
        private ImageView imagen_item;
        //private Button unirse_item;

        ActividadViewHolder(@NonNull final View itemView) {
            super(itemView);

            nombre_item = itemView.findViewById(R.id.txtNombre);
            imagen_item = itemView.findViewById(R.id.imgActividad);
            descripcion_item = itemView.findViewById(R.id.txtDescripcion);
            estado_item = itemView.findViewById(R.id.txtPrioridad);
            puntos_item = itemView.findViewById(R.id.txtPuntosRecompaensa);

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
}
