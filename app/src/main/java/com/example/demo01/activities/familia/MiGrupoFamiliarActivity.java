package com.example.demo01.activities.familia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.example.demo01.activities.models.Familia;
import com.example.demo01.activities.models.Miembro;
import com.example.demo01.activities.models.Usuario;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Objects;

public class MiGrupoFamiliarActivity extends AppCompatActivity {

    TextView mnombrefamilia, mdescripcion;
    ImageView mFamilia;
    RecyclerView mFamiliares;
    Button mvolver;

    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseFirestore db;
    FirebaseStorage storage;
    StorageReference storageRef;
    FirestoreRecyclerAdapter usuarioAdapter;

    ArrayList<Miembro> miembros;
    ArrayList<Usuario> usuarios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mi_grupo_familiar);

        mAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        mnombrefamilia = findViewById(R.id.txtNombreFamilia);
        mdescripcion = findViewById(R.id.txtDescripcion);
        mFamilia = findViewById(R.id.imgFamilia);
        mFamiliares = findViewById(R.id.rcvFamiliares);
        mvolver = findViewById(R.id.btnVolver);

        mvolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelar();
            }
        });

        Bundle args = getIntent().getExtras();
        assert args != null;
        Familia familia = (Familia) args.getSerializable("familia");
        assert familia != null;
        mnombrefamilia.setText(familia.getNombre());
        mdescripcion.setText(familia.getDescripcion());

        storageRef.child("grupofamiliar/"+familia.getIdFamilia()+"/portada.jpg")
                .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(MiGrupoFamiliarActivity.this).load(uri).into(mFamilia);
            }
        });

        miembros = new ArrayList<Miembro>();
        usuarios = new ArrayList<Usuario>();

        CollectionReference docRef = db.collection("grupoFamiliar").document(familia.getIdFamilia()+"").collection("miembros");
        docRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                Miembro miembro = document.toObject(Miembro.class);
                                miembros.add(miembro);
                            }
                        } else {
                            Log.d("", "Error getting documents: ", task.getException());
                        }
                        //Toast.makeText(MiGrupoFamiliarActivity.this, miembros.size()+"", Toast.LENGTH_SHORT).show();
                        for (int i = 0; i < miembros.size(); i++) {
                            String idUsurio = miembros.get(i).getIdMiembro();
                            DocumentReference usuarioRef = db.collection("usuario").document(idUsurio+"");
                            //final int finalI = i;
                            usuarioRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    Usuario usuario = documentSnapshot.toObject(Usuario.class);
                                    usuarios.add(usuario);
                                    //Toast.makeText(MiGrupoFamiliarActivity.this, ""+usuarios.get(finalI).getNombres(), Toast.LENGTH_SHORT).show();
                                }
                            });

                        }

                    }
                });



        Query query = db.collection("usuario").whereArrayContains("grupoFamiliar",familia.getIdFamilia());
        final FirestoreRecyclerOptions<Usuario> usuarioOptions = new FirestoreRecyclerOptions.Builder<Usuario>()
                .setQuery(query, Usuario.class)
                .build();

        usuarioAdapter = new FirestoreRecyclerAdapter<Usuario, UsuarioViewHolder>(usuarioOptions) {
            @NonNull
            @Override
            public UsuarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_usuario_item, parent, false);
                return new UsuarioViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final UsuarioViewHolder usuarioViewHolder, final int i, @NonNull final Usuario usuario) {

                String nombreCompleto = usuario.getNombres()+" "+usuario.getApellidoPaterno()+" "+usuario.getApellidoMaterno();
                usuarioViewHolder.nombre_item.setText(nombreCompleto);

                storageRef.child("imagen/" + usuario.getIdUsuario() + "/perfil.jpg")
                        .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(MiGrupoFamiliarActivity.this).load(uri).into(usuarioViewHolder.imagen_item);
                    }
                });

                usuarioViewHolder.view_item.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle args = new Bundle();

                        Intent intent = new Intent(MiGrupoFamiliarActivity.this, FamiliaSeccionadaActivity.class);

                        args.putSerializable("familia", usuario);
                        intent.putExtras(args);
                        startActivity(intent);
                    }
                });
            }

        };

        mFamiliares.setHasFixedSize(true);
        mFamiliares.setLayoutManager(new LinearLayoutManager(this));
        mFamiliares.setAdapter(usuarioAdapter);

    }


    private class UsuarioViewHolder extends RecyclerView.ViewHolder{

    private TextView nombre_item;
    private ImageView imagen_item;
    private View view_item;

        UsuarioViewHolder(@NonNull final View itemView) {
            super(itemView);

            nombre_item = itemView.findViewById(R.id.txtNombreActividad);
            imagen_item = itemView.findViewById(R.id.imgUsuario);
            view_item = itemView.findViewById(R.id.viewUsuario);

        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        usuarioAdapter.stopListening();
    }

    @Override
    protected void onStart() {
        super.onStart();
        usuarioAdapter.startListening();
    }

    private void cancelar(){
        startActivity(new Intent(MiGrupoFamiliarActivity.this, FamiliaActivity.class));
        finish();
    }

}