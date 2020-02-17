package com.example.demo01.activities.recompensa;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.demo01.R;
import com.example.demo01.activities.actividad.ActividadActivity;
import com.example.demo01.activities.models.Recompensa;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class RecompensaActivity extends AppCompatActivity {

    RecyclerView rv;
    TextView mpuntos;
    Button mbtnReclamos, mbtnCrear, mbtnVolver;

    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseFirestore db;
    FirebaseStorage storage;
    StorageReference storageRef;

    ArrayList<Recompensa> recompensas;

    FirestoreRecyclerAdapter recompensaAdapter;

    final static String TAG = "RecompensaActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recompensa);

        mAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        mbtnCrear = findViewById(R.id.btnCrearRecompensa);
        mpuntos = findViewById(R.id.txtPuntosRecompaensa);
        mbtnReclamos = findViewById(R.id.btnReclamos);

        mbtnVolver = findViewById(R.id.btnVolver);

        rv = findViewById(R. id.rvRecompensa);
        rv.setLayoutManager(new LinearLayoutManager(RecompensaActivity.this));

        recompensas = new ArrayList<>();
        final String uid = user.getUid();

        CollectionReference actividadRef = db.collection("recompensa");
        Query queryDestino = actividadRef.whereEqualTo("idCreador",uid);

        final FirestoreRecyclerOptions<Recompensa> recompensaOptionsDestino = new FirestoreRecyclerOptions.Builder<Recompensa>()
                .setQuery(queryDestino, Recompensa.class)
                .build();

        recompensaAdapter = new FirestoreRecyclerAdapter<Recompensa, RecompensaViewHolder>(recompensaOptionsDestino) {
            @NonNull
            @Override
            public RecompensaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_recompensa_item, parent, false);
                return new RecompensaViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final RecompensaViewHolder recompensaViewHolder, final int i, @NonNull final Recompensa recompensa) {
                //crearNotificacionRealizar();
                recompensaViewHolder.nombre_item.setText(recompensa.getNombre());
                recompensaViewHolder.fecha_item.setText(recompensa.getFechaReclamo());
                recompensaViewHolder.puntos_item.setText(String.valueOf(recompensa.getPuntosNecesarios()));
            }

        };

        mbtnCrear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RecompensaActivity.this, CrearRecompensaActivity.class));
                finish();
            }
        });

        mbtnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RecompensaActivity.this, ActividadActivity.class));
                finish();
            }
        });

        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(recompensaAdapter);

    }

    private class RecompensaViewHolder extends RecyclerView.ViewHolder{

        private TextView nombre_item, fecha_item, puntos_item;

        RecompensaViewHolder(@NonNull final View itemView) {
            super(itemView);

            nombre_item = itemView.findViewById(R.id.txtNombrereclamo);
            fecha_item = itemView.findViewById(R.id.txtFechaReclamo);
            puntos_item = itemView.findViewById(R.id.txtPuntosRecompaensa);

        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        recompensaAdapter.stopListening();
    }

    @Override
    protected void onStart() {
        super.onStart();
        recompensaAdapter.startListening();
    }
}
