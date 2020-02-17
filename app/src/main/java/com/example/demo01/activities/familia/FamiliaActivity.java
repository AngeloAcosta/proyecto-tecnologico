package com.example.demo01.activities.familia;

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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.demo01.R;
import com.example.demo01.activities.actividad.ActividadActivity;
import com.example.demo01.activities.dialog.CargandoDialogFragment;
import com.example.demo01.activities.models.Familia;
import com.example.demo01.activities.perfil.PerfilActivity;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FamiliaActivity extends AppCompatActivity {

    ImageButton mbtnPerfil, mbtnFamilia, mbtnActividad;
    Button mCrearFamilia, mUnirseFamilia;
    RecyclerView mrcvFamilias;

    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseFirestore db;
    FirebaseStorage storage;
    StorageReference storageRef;
    FirestoreRecyclerAdapter familiaAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_familia);

        mAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        final String uid = user.getUid();

        final CargandoDialogFragment cargandoDialogFragment = new CargandoDialogFragment(FamiliaActivity.this);
        cargandoDialogFragment.startLoadingDialog();

        mbtnActividad = findViewById(R. id.btnActividad);
        mbtnFamilia = findViewById(R. id.btnFamilia);
        mbtnPerfil = findViewById(R. id.btnPerfil);

        mrcvFamilias = findViewById(R.id.rcvFamilias);

        mCrearFamilia = findViewById(R.id.btnCrearFamilia);
        mUnirseFamilia = findViewById(R.id.btnUnirseFamilia);

        Query query = db.collection("grupoFamiliar").whereArrayContains("miembros",uid);

        final FirestoreRecyclerOptions<Familia> familiaOptions = new FirestoreRecyclerOptions.Builder<Familia>()
                .setQuery(query, Familia.class)
                .build();

        familiaAdapter = new FirestoreRecyclerAdapter<Familia, FamiliaViewHolder>(familiaOptions) {
            @NonNull
            @Override
            public FamiliaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_familia_item, parent, false);
                return new FamiliaViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final FamiliaViewHolder familiaViewHolder, final int i, @NonNull final Familia familia) {

                familiaViewHolder.nombre_item.setText(familia.getNombre());

                storageRef.child("grupofamiliar/" + familia.getIdFamilia() + "/portada.jpg")
                        .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(FamiliaActivity.this).load(uri).into(familiaViewHolder.imagen_item);
                    }
                });

                familiaViewHolder.imagen_item.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle args = new Bundle();

                        Intent intent = new Intent(FamiliaActivity.this, MiGrupoFamiliarActivity.class);

                        args.putSerializable("familia", familia);
                        intent.putExtras(args);
                        startActivity(intent);
                    }
                });
            }

        };

        mbtnActividad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FamiliaActivity.this, ActividadActivity.class));
                finish();
            }
        });

        mbtnPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FamiliaActivity.this, PerfilActivity.class));
                finish();
            }
        });

        mCrearFamilia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FamiliaActivity.this, CrearFamiliaActivity.class));
                finish();
            }
        });

        mUnirseFamilia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FamiliaActivity.this, ListaFamiliasActivity.class));
                finish();
            }
        });

        mrcvFamilias.setHasFixedSize(true);
        mrcvFamilias.setLayoutManager(new LinearLayoutManager(this));
        mrcvFamilias.setAdapter(familiaAdapter);

        cargandoDialogFragment.dismissDialog();

    }

    private class FamiliaViewHolder extends RecyclerView.ViewHolder{

        private TextView nombre_item, clave_item;
        private ImageView imagen_item;
        //private Button unirse_item;

        FamiliaViewHolder(@NonNull final View itemView) {
            super(itemView);

            nombre_item = itemView.findViewById(R.id.txtNombreFamilia);
            imagen_item = itemView.findViewById(R.id.imgFamilia);

        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        familiaAdapter.stopListening();
    }

    @Override
    protected void onStart() {
        super.onStart();
        familiaAdapter.startListening();
    }
}
