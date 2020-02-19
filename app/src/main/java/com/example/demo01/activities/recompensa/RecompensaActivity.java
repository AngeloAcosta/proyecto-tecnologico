package com.example.demo01.activities.recompensa;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.demo01.R;
import com.example.demo01.activities.actividad.ActividadActivity;
import com.example.demo01.activities.dialog.CargandoDialogFragment;
import com.example.demo01.activities.models.Familia;
import com.example.demo01.activities.models.Recompensa;
import com.example.demo01.activities.models.Usuario;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import javax.annotation.Nullable;

public class RecompensaActivity extends AppCompatActivity {

    RecyclerView rv;
    TextView mTotalPuntos;
    Button mbtnReclamos, mbtnCrear, mbtnVolver;

    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseFirestore db;
    FirebaseStorage storage;
    StorageReference storageRef;

    CollectionReference actividadRef;
    Query queryDestino;

    ArrayList<Recompensa> recompensas;

    FirestoreRecyclerAdapter recompensaAdapter;
    CargandoDialogFragment cargandoDialogFragment;

    Familia familia;
    String uidFamilia = "";

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

        cargandoDialogFragment = new CargandoDialogFragment(RecompensaActivity.this);
        cargandoDialogFragment.startLoadingDialog();

        mTotalPuntos = findViewById(R.id.txtTotalPuntos);

        mbtnCrear = findViewById(R.id.btnCrearRecompensa);
        mbtnReclamos = findViewById(R.id.btnReclamos);

        mbtnCrear.setVisibility(View.INVISIBLE);
        mbtnReclamos.setVisibility(View.INVISIBLE);

        mbtnVolver = findViewById(R.id.btnVolver);

        mbtnReclamos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle args = new Bundle();

                Intent intent = new Intent(RecompensaActivity.this, ReclamosActivity.class);

                args.putSerializable("familia", familia);
                intent.putExtras(args);
                startActivity(intent);
            }
        });

        final String uid = user.getUid();

        DocumentReference docRef = db.collection("usuario").document(uid);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.d(TAG, "Current data: " + snapshot.getData());
                    Usuario usuario = snapshot.toObject(Usuario.class);
                    mTotalPuntos.setText(usuario.getPuntos()+"");
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });

        rv = findViewById(R. id.rvReclamos);
        rv.setLayoutManager(new LinearLayoutManager(RecompensaActivity.this));

        recompensas = new ArrayList<>();

        Bundle args = getIntent().getExtras();
        if(args != null){
            familia = (Familia) args.getSerializable("familia");
        }
        if(familia != null){
            uidFamilia = familia.getIdFamilia();
        }

        actividadRef = db.collection("recompensa");

        if (uidFamilia.isEmpty() == false){
            queryDestino = actividadRef.whereEqualTo("idGrupo",uidFamilia);
        }

        if (uidFamilia.isEmpty() == true) {
            queryDestino = actividadRef.whereEqualTo("idCreador",uid);
            mbtnCrear.setVisibility(View.VISIBLE);
            mbtnReclamos.setVisibility(View.VISIBLE);
        }


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
                recompensaViewHolder.reclamar_item.setEnabled(false);

                final int misPuntos = Integer.parseInt(mTotalPuntos.getText().toString());

                if(misPuntos >= recompensa.getPuntosNecesarios()){
                    recompensaViewHolder.reclamar_item.setEnabled(true);
                    recompensaViewHolder.reclamar_item.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                                int totalRestante = misPuntos - recompensa.getPuntosNecesarios();
                                DocumentReference usuarioPuntosRef = db.collection("usuario").document(uid);
                                usuarioPuntosRef.update("puntos", totalRestante);
                                Toast.makeText(RecompensaActivity.this, "Felicidades", Toast.LENGTH_SHORT).show();
                            return false;
                        }
                    });
                } else {
                    recompensaViewHolder.reclamar_item.setText("AUN NO");
                }

                recompensaViewHolder.reclamar_item.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {

                        recompensaViewHolder.reclamar_item.setEnabled(false);

                        int misPuntos = Integer.parseInt(mTotalPuntos.getText().toString());

                        if(misPuntos >= recompensa.getPuntosNecesarios()){
                            int totalRestante = misPuntos - recompensa.getPuntosNecesarios();
                            DocumentReference usuarioPuntosRef = db.collection("usuario").document(uid);

                            usuarioPuntosRef.update("puntos", totalRestante);
                            recompensaViewHolder.reclamar_item.setEnabled(true);

                            Toast.makeText(RecompensaActivity.this, "Felicidades", Toast.LENGTH_SHORT).show();
                        }

                        return false;
                    }
                });
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
        private Button reclamar_item;

        RecompensaViewHolder(@NonNull final View itemView) {
            super(itemView);

            nombre_item = itemView.findViewById(R.id.txtNombrereclamo);
            fecha_item = itemView.findViewById(R.id.txtFechaReclamo);
            puntos_item = itemView.findViewById(R.id.txtPuntosRecompaensa);
            reclamar_item = itemView.findViewById(R.id.btnReclamar);

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
        cargandoDialogFragment.dismissDialog();
    }
}
