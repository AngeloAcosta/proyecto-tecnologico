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
import com.google.android.gms.tasks.OnSuccessListener;
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

public class ReclamosActivity extends AppCompatActivity {

    RecyclerView rv;
    Button mbtnVolver;

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

    final static String TAG = "ReclamosActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reclamos);

        mAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        cargandoDialogFragment = new CargandoDialogFragment(ReclamosActivity.this);
        cargandoDialogFragment.startLoadingDialog();

        mbtnVolver = findViewById(R.id.btnVolver2);

        final String uid = user.getUid();

        rv = findViewById(R. id.rvReclamos);
        rv.setLayoutManager(new LinearLayoutManager(ReclamosActivity.this));

        recompensas = new ArrayList<>();

        actividadRef = db.collection("recompensa");

        Bundle args = getIntent().getExtras();
        if(args != null){
            familia = (Familia) args.getSerializable("familia");
        }
        if(familia != null){
            uidFamilia = familia.getIdFamilia();
        }

        queryDestino = actividadRef.whereEqualTo("idGrupo",uidFamilia);
        queryDestino = actividadRef.whereEqualTo("estado","RECLAMADO");

        final FirestoreRecyclerOptions<Recompensa> recompensaOptionsDestino = new FirestoreRecyclerOptions.Builder<Recompensa>()
                .setQuery(queryDestino, Recompensa.class)
                .build();
        recompensaAdapter = new FirestoreRecyclerAdapter<Recompensa, ReclamosActivity.RecompensaViewHolder>(recompensaOptionsDestino) {
            @NonNull
            @Override
            public ReclamosActivity.RecompensaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_recompensa_item, parent, false);

                return new ReclamosActivity.RecompensaViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final ReclamosActivity.RecompensaViewHolder recompensaViewHolder, final int i, @NonNull final Recompensa recompensa) {
                //crearNotificacionRealizar();
                recompensaViewHolder.nombre_item.setText(recompensa.getNombre());
                recompensaViewHolder.fecha_item.setText(recompensa.getFechaReclamo());
                recompensaViewHolder.puntos_item.setText(String.valueOf(recompensa.getPuntosNecesarios()));
                recompensaViewHolder.reclamar_item.setEnabled(false);

                String idUsu = recompensa.getIdUsuario();
                db.collection("usuario").document(idUsu)
                        .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Usuario usuario = documentSnapshot.toObject(Usuario.class);
                        recompensaViewHolder.reclamar_item.setText("RECLAMADO POR"+usuario.getNombres());
                    }
                });
            }

        };

        mbtnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ReclamosActivity.this, RecompensaActivity.class));
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