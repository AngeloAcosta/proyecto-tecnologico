package com.example.demo01.activities.recompensa;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.demo01.R;
import com.example.demo01.activities.dialog.CargandoDialogFragment;
import com.example.demo01.activities.dialog.GrupoDialogFrangment;
import com.example.demo01.activities.dialog.PuntosRecompensaDialogFragment;
import com.example.demo01.activities.models.Familia;
import com.example.demo01.activities.models.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CrearRecompensaActivity extends AppCompatActivity implements PuntosRecompensaDialogFragment.PuntosListener, GrupoDialogFrangment.GrupoListener {

    ListView listViewNombreGrupo;
    ListView listViewNombreMiembro;

    EditText mnombreRecompensa;
    TextView mfechaReclamo, mpuntosNecesarios, mgrupoFamiliar, mmiembro;
    Button mCrearRecompensa, mVolver;

    private static final String TAG = "CrearRecompensaActivity";

    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseFirestore db;
    FirebaseStorage storage;
    StorageReference storageRef;

    List<String> idGrupos;
    List<String> nombreGrupos;
    List<String> idMiembros;
    List<String> nombreMiembros;

    String idGrupo = "";
    int puntos = 0;
    String idDestino = "";

    private DatePickerDialog.OnDateSetListener mDateSetListenerInicio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_recompensa);

        mAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        final String uid = user.getUid();

        listViewNombreGrupo = new ListView(CrearRecompensaActivity.this);
        listViewNombreMiembro = new ListView(CrearRecompensaActivity.this);

        final CargandoDialogFragment cargandoDialogFragment = new CargandoDialogFragment(CrearRecompensaActivity.this);
        cargandoDialogFragment.startLoadingDialog();

        idGrupos = new ArrayList<>();
        nombreGrupos = new ArrayList<>();
        idMiembros = new ArrayList<>();
        nombreMiembros = new ArrayList<>();

        mnombreRecompensa = findViewById(R.id.txtNombreRecompensa);
        mfechaReclamo = findViewById(R.id.txtFechaReclamo);
        mpuntosNecesarios = findViewById(R.id.txtPuntosNecesarios);
        mgrupoFamiliar = findViewById(R.id.txtGrupoFamiliar);
        mmiembro = findViewById(R.id.txtMiembro);

        mCrearRecompensa = findViewById(R.id.btnCrearRecompensa);
        mVolver = findViewById(R.id.btnVolver);


        db.collection("grupoFamiliar").whereEqualTo("idCreador", uid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                final Familia familia = document.toObject(Familia.class);
                                idGrupos.add(familia.getIdFamilia());
                                nombreGrupos.add(familia.getNombre());
                                cargandoDialogFragment.dismissDialog();
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }

                    }
                });

        final ArrayAdapter<String> adapterFamilia = new ArrayAdapter<String>(CrearRecompensaActivity.this, android.R.layout.simple_list_item_1, nombreGrupos);
        listViewNombreGrupo.setAdapter(adapterFamilia);

        AlertDialog.Builder builderFamilia = new AlertDialog.Builder(CrearRecompensaActivity.this);
        builderFamilia.setCancelable(true);
        builderFamilia.setView(listViewNombreGrupo);
        final AlertDialog grupodialog = builderFamilia.create();

        mgrupoFamiliar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mmiembro.setText("SELECCIONAR");
                grupodialog.show();
            }
        });

        listViewNombreGrupo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                cargandoDialogFragment.startLoadingDialog();
                nombreMiembros.clear();
                idMiembros.clear();
                mgrupoFamiliar.setText(adapterFamilia.getItem(position));
                if(nombreGrupos.get(position) == mgrupoFamiliar.getText()){
                    idGrupo = idGrupos.get(position);

                    db.collection("usuario").whereArrayContains("grupoFamiliar",idGrupo)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            Log.d(TAG, document.getId() + " => " + document.getData());
                                            Usuario usuario = document.toObject(Usuario.class);
                                            String idMiembro = usuario.getIdUsuario();
                                            String nomApePat = usuario.getNombres()+" "+usuario.getApellidoPaterno();
                                            //nombredestino = nomApePat;
                                            idMiembros.add(idMiembro);
                                            nombreMiembros.add(nomApePat);
                                            cargandoDialogFragment.dismissDialog();
                                        }
                                    } else {
                                        Log.d(TAG, "Error getting documents: ", task.getException());
                                        cargandoDialogFragment.dismissDialog();
                                    }
                                }
                            });
                }else{
                    Toast.makeText(CrearRecompensaActivity.this, mgrupoFamiliar.toString()+ " no es igual a "+nombreGrupos.get(position), Toast.LENGTH_SHORT).show();
                }
                grupodialog.dismiss();
            }
        });

        final ArrayAdapter<String> adapterMiembro = new ArrayAdapter<String>(CrearRecompensaActivity.this,android.R.layout.simple_list_item_1, nombreMiembros);
        listViewNombreMiembro.setAdapter(adapterMiembro);

        AlertDialog.Builder builderMiembro = new AlertDialog.Builder(CrearRecompensaActivity.this);
        builderMiembro.setCancelable(true);
        builderMiembro.setView(listViewNombreMiembro);
        final AlertDialog miembroDialog = builderMiembro.create();

        mmiembro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mmiembro.getText().equals("SELECCIONAR") && nombreMiembros.size() == 0 ){
                    Toast.makeText(CrearRecompensaActivity.this, "Primero seleccione un grupo.  "+nombreMiembros.size(), Toast.LENGTH_SHORT).show();
                }else{
                    miembroDialog.show();
                }
            }
        });

        listViewNombreMiembro.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mmiembro.setText(adapterMiembro.getItem(position));
                idDestino = idMiembros.get(position);
                miembroDialog.dismiss();
            }
        });

        mfechaReclamo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(CrearRecompensaActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListenerInicio,
                        year,month,day);
                Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateSetListenerInicio = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                month = month + 1;
                final String diaInicio = day + "/" + month + "/" + year;
                mfechaReclamo.setText(diaInicio);
            }
        };

        mCrearRecompensa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cargandoDialogFragment.startLoadingDialog();
                DocumentReference recompensaRef = db.collection("recompensa").document();

                Map<String, Object> data = new HashMap<>();
                data.put("nombre",mnombreRecompensa.getText().toString());
                data.put("fechaReclamo",mfechaReclamo.getText().toString());
                data.put("idRecompensa",recompensaRef.getId());
                data.put("idUsuario",idDestino);
                data.put("fechaLimite",mfechaReclamo.getText().toString());
                data.put("estado","ACTIVO");
                data.put("idCreador",uid);
                data.put("idGrupo",mgrupoFamiliar.getText().toString());
                data.put("puntosNecesarios",puntos);

                if(mnombreRecompensa.getText().toString().isEmpty() &&
                        mpuntosNecesarios.getText().toString().isEmpty() &&
                        mfechaReclamo.getText().toString().isEmpty() &&
                        mgrupoFamiliar.getText().toString().isEmpty() &&
                        mmiembro.getText().toString().isEmpty()){
                    recompensaRef.set(data)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("", "DocumentSnapshot successfully written!");
                                    cargandoDialogFragment.dismissDialog();
                                    startActivity(new Intent(CrearRecompensaActivity.this, RecompensaActivity.class));
                                    finish();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w("", "Error writing document", e);
                                    cargandoDialogFragment.dismissDialog();
                                }
                            });
                } else {
                    Toast.makeText(CrearRecompensaActivity.this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mpuntosNecesarios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View puntoDiag) {
                DialogFragment puntosDialog = new PuntosRecompensaDialogFragment();
                puntosDialog.setCancelable(false);
                puntosDialog.show(getSupportFragmentManager(),"Puntos de la actividad");
            }
        });

        mVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CrearRecompensaActivity.this, RecompensaActivity.class));
                finish();
            }
        });

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onPositiveButtonClicked(String[] list, int position) {
        final String select = list[position];
            puntos = Integer.parseInt(select);
            mpuntosNecesarios.setText(select);
    }

    @Override
    public void onNegativeButtonClicked() {
    }

    @Override
    public void onPositiveButtonGrupo(List<String> list, int position) {
        mgrupoFamiliar.setText(list.get(position));
    }

    @Override
    public void onNegativeButtonGrupo() {

    }
}
