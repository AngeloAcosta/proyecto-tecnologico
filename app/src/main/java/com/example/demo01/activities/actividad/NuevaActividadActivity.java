package com.example.demo01.activities.actividad;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.demo01.R;
import com.example.demo01.activities.dialog.CargandoDialogFragment;
import com.example.demo01.activities.dialog.GrupoDialogFrangment;
import com.example.demo01.activities.dialog.PrioridadDialogFragment;
import com.example.demo01.activities.dialog.PuntosDialogFragment;
import com.example.demo01.activities.models.Familia;
import com.example.demo01.activities.models.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class NuevaActividadActivity extends AppCompatActivity implements PuntosDialogFragment.PuntosListener, PrioridadDialogFragment.PrioridadListener, GrupoDialogFrangment.GrupoListener {

    ListView listViewNombreGrupo;
    ListView listViewNombreMiembro;

    List<String> idGrupos;
    List<String> nombreGrupos;
    List<String> idMiembros;
    List<String> nombreMiembros;

    private static final String TAG = "NuevaActividadActivity";
    EditText midActividad, mnombre, mdetalle;
    TextView mrecompensa, muriImagen,mgrupoFamiliar, midCreador, mcondicion, mestado, mfecha, mfechaInicio, mfechaFin, mhoraInicio, mhoraFin, mpuntos, mdestino, mprioridad;
    List<String> ydestino;
    int ypuntos;
    Button mbtnCrear,btnAgregarFoto, mbtnCancelar;
    ImageView mimgActivdad;

    private DatePickerDialog.OnDateSetListener mDateSetListenerInicio, mDateSetListenerFin;
    private TimePickerDialog.OnTimeSetListener mDateSetListenerHoraInicio, mDateSetListenerHoraFin;

    static final int PICK_IMAGE_REQUEST = 1;
    Uri filePath;

    String nombre = "";
    String actividad = "";
    String recompensa = "";
    String detalle = "";
    String uriImagen = "";
    String idGrupo = "";
    String condicion = "";
    String estado = "ACTIVO";
    String prioridad = "";
    ArrayList destino = new ArrayList();
    String idDestino = "";
    int puntos = 0;
    String fechaInicio = "";
    String fechaFin = "";
    String horaInicio = "";
    String horaFin = "";
    String nombredestino = "";

    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseFirestore db;
    FirebaseStorage storage;
    StorageReference storageRef;

    @SuppressLint({"ResourceType", "CutPasteId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nueva_actividad);

        mAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        final String uid = user.getUid();

        listViewNombreGrupo = new ListView(NuevaActividadActivity.this);
        listViewNombreMiembro = new ListView(NuevaActividadActivity.this);

        final CargandoDialogFragment cargandoDialogFragment = new CargandoDialogFragment(NuevaActividadActivity.this);
        cargandoDialogFragment.startLoadingDialog();

        idGrupos = new ArrayList<>();
        nombreGrupos = new ArrayList<>();
        idMiembros = new ArrayList<>();
        nombreMiembros = new ArrayList<>();

        mnombre = findViewById(R. id.txtNombreActividad);
        mdetalle = findViewById(R. id.txtDetalleActividad);
        mcondicion = findViewById(R.id.txtPrioridad);
        mfechaInicio = findViewById(R.id.txtFechaInicio);
        mfechaFin = findViewById(R.id.txtFechaFin);
        mhoraInicio = findViewById(R.id.txtHoraInicio);
        mhoraFin = findViewById(R.id.txtHoraFin);
        mpuntos = findViewById(R.id.txtPuntosRecompaensa);
        mdestino = findViewById(R.id.txtDestino);
        mprioridad = findViewById(R.id.txtPrioridad);
        mgrupoFamiliar = findViewById(R.id.txtGrupoFamilia);

        mimgActivdad = findViewById(R.id.imgActividad);

        mbtnCrear = (Button) findViewById(R. id.btnCrearActivdad);
        mbtnCancelar = findViewById(R.id.btnCancelar);

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

        final ArrayAdapter<String> adapterFamilia = new ArrayAdapter<String>(NuevaActividadActivity.this, android.R.layout.simple_list_item_1, nombreGrupos);
        listViewNombreGrupo.setAdapter(adapterFamilia);

        AlertDialog.Builder builderFamilia = new AlertDialog.Builder(NuevaActividadActivity.this);
        builderFamilia.setCancelable(true);
        builderFamilia.setView(listViewNombreGrupo);
        final AlertDialog grupodialog = builderFamilia.create();

        mgrupoFamiliar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mdestino.setText("SELECCIONAR");
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
                    Toast.makeText(NuevaActividadActivity.this, mgrupoFamiliar.toString()+ " no es igual a "+nombreGrupos.get(position), Toast.LENGTH_SHORT).show();
                }
                grupodialog.dismiss();
            }
        });

        final ArrayAdapter<String> adapterMiembro = new ArrayAdapter<String>(NuevaActividadActivity.this,android.R.layout.simple_list_item_1, nombreMiembros);
        listViewNombreMiembro.setAdapter(adapterMiembro);

        AlertDialog.Builder builderMiembro = new AlertDialog.Builder(NuevaActividadActivity.this);
        builderMiembro.setCancelable(true);
        builderMiembro.setView(listViewNombreMiembro);
        final AlertDialog miembroDialog = builderMiembro.create();

        mdestino.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mdestino.getText().equals("SELECCIONAR") && nombreMiembros.size() == 0 ){
                    Toast.makeText(NuevaActividadActivity.this, "Primero seleccione un grupo.  "+nombreMiembros.size(), Toast.LENGTH_SHORT).show();
                }else{
                    miembroDialog.show();
                }
            }
        });

        listViewNombreMiembro.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mdestino.setText(adapterMiembro.getItem(position));
                idDestino = idMiembros.get(position);
                miembroDialog.dismiss();
            }
        });

        mprioridad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View prioridadDiag) {
                DialogFragment prioridadDialog = new PrioridadDialogFragment();
                prioridadDialog.setCancelable(false);
                prioridadDialog.show(getSupportFragmentManager(),"Prioridad de la actividad");
            }
        });

        mpuntos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View puntoDiag) {
                DialogFragment puntosDialog = new PuntosDialogFragment();
                puntosDialog.setCancelable(false);
                puntosDialog.show(getSupportFragmentManager(),"Puntos de la actividad");
            }
        });

        mhoraInicio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int hora = cal.get(Calendar.HOUR_OF_DAY);
                int minuto = cal.get(Calendar.MINUTE);

                TimePickerDialog dialog  =new TimePickerDialog(NuevaActividadActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListenerHoraInicio, hora, minuto,false);
                Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mhoraFin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int hora = cal.get(Calendar.HOUR_OF_DAY);
                int minuto = cal.get(Calendar.MINUTE);

                TimePickerDialog dialog  =new TimePickerDialog(NuevaActividadActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListenerHoraFin, hora, minuto,false);
                Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mfechaInicio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(NuevaActividadActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListenerInicio,
                        year,month,day);
                Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mfechaFin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(NuevaActividadActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListenerFin,
                        year,month,day);
                Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateSetListenerHoraInicio = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                final String horaInicio = hourOfDay + ":" + minute;
                mhoraInicio.setText(horaInicio);
            }
        };

        mDateSetListenerHoraFin = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                final String horaFin = hourOfDay + ":" + minute;
                mhoraFin.setText(horaFin);
            }
        };

        mDateSetListenerInicio = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                month = month + 1;
                final String diaInicio = day + "/" + month + "/" + year;
                mfechaInicio.setText(diaInicio);
            }
        };

        mDateSetListenerFin = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                month = month + 1;
                final String diaFin = day + "/" + month + "/" + year;
                mfechaFin.setText(diaFin);
            }
        };

        mimgActivdad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fotoGaleria();
            }
        });

        mbtnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelar();
            }
        });

        mbtnCrear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAllEnableFalse();
                cargandoDialogFragment.startLoadingDialog();
                final String uid = user.getUid();
                final DocumentReference actividadRef = db.collection("actividad").document();
                final String uidActividad = actividadRef.getId();
                //REFERENCIA DE ALMACENAMIENTO
                final StorageReference imagenRef = storageRef.child("actividad/"+uidActividad+"/imagen.jpg");

                //COMBIRTIENDO LA IMAGEN EN BIT
                mimgActivdad.setDrawingCacheEnabled(true);
                mimgActivdad.buildDrawingCache();
                try {
                    Bitmap bitmap = ((BitmapDrawable) mimgActivdad.getDrawable()).getBitmap();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 25, baos);

                    byte[] dataimg = baos.toByteArray();
                    //SUBIENDO LA IMAGEN A FIRESTORE
                    final UploadTask uploadTask = imagenRef.putBytes(dataimg);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            nombre = mnombre.getText().toString();
                            fechaInicio = mfechaInicio.getText().toString();
                            fechaFin = mfechaFin.getText().toString();
                            horaInicio = mhoraInicio.getText().toString();
                            horaFin = mhoraFin.getText().toString();
                            detalle = mdetalle.getText().toString();

                            if(!nombre.isEmpty() && !detalle.isEmpty()) {

                                Map<String, Object> data = new HashMap<>();
                                data.put("nombre", nombre);
                                data.put("idActividad", uidActividad);
                                data.put("detalle",detalle);
                                data.put("idGrupoFamiliar",idGrupo);
                                data.put("idDestino",idDestino);
                                data.put("idCreador",uid);
                                data.put("estado",estado);
                                data.put("destino",nombredestino);
                                data.put("fecha", new Timestamp(new Date()));
                                data.put("fechaInicio",fechaInicio);
                                data.put("fechaFin",fechaFin);
                                data.put("horaInicio",horaInicio);
                                data.put("horaFin",horaFin);
                                data.put("puntos", puntos);
                                data.put("prioridad",prioridad);

                                actividadRef.set(data)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                startActivity(new Intent(NuevaActividadActivity.this, ActividadActivity.class));
                                                finish();
                                                Toast.makeText(NuevaActividadActivity.this, "Actividad creada!!", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w(TAG, "Error adding document", e);
                                                setAllEnableTrue();
                                                cargandoDialogFragment.dismissDialog();
                                                Toast.makeText(NuevaActividadActivity.this, "No se pudo crear la actividad", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                Toast.makeText(NuevaActividadActivity.this, ""+mimgActivdad, Toast.LENGTH_SHORT).show();
                                setAllEnableTrue();
                                cargandoDialogFragment.dismissDialog();
                                Toast.makeText(NuevaActividadActivity.this, "Tiene que poner un Nombre, Descripcion y Clave a su Familia.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                catch(Exception e) {
                    setAllEnableTrue();
                    cargandoDialogFragment.dismissDialog();
                    Toast.makeText(NuevaActividadActivity.this, "Coloque una imagen", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                mimgActivdad.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            Toast.makeText(this, "Algo paso con la imagen, intentelo nuevamente.", Toast.LENGTH_SHORT).show();
        }
    }

    private void fotoGaleria(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Seleccionar Imagen"), PICK_IMAGE_REQUEST);
    }

    private void cancelar(){
        startActivity(new Intent(NuevaActividadActivity.this, ActividadActivity.class));
        finish();
    }



    @SuppressLint("SetTextI18n")
    @Override
    public void onPositiveButtonClicked(String[] list, int position) {
        final String select = list[position];
        if(list.length == 4){
            prioridad = select;
            mprioridad.setText(select);
        } else if(list.length == 7) {
            puntos = Integer.parseInt(select);
            mpuntos.setText(select + " puntos");
        }
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

    private void setAllEnableTrue(){
        //midActividad.setEnabled(true);
        mnombre.setEnabled(true);
        mdetalle.setEnabled(true);
        //mrecompensa.setEnabled(true);
        //muriImagen.setEnabled(true);
        mgrupoFamiliar.setEnabled(true);
        //midCreador.setEnabled(true);
        mcondicion.setEnabled(true);
        //mestado.setEnabled(true);
        //mfecha.setEnabled(true);
        mfechaInicio.setEnabled(true);
        mfechaFin.setEnabled(true);
        mhoraInicio.setEnabled(true);
        mhoraFin.setEnabled(true);
        mpuntos.setEnabled(true);
        mdestino.setEnabled(true);
        mprioridad.setEnabled(true);
        mbtnCrear.setEnabled(true);
        //btnAgregarFoto.setEnabled(true);
        mbtnCancelar.setEnabled(true);
        mimgActivdad.setEnabled(true);
    }

    private void setAllEnableFalse(){
        //midActividad.setEnabled(false);
        mnombre.setEnabled(false);
        mdetalle.setEnabled(false);
        //mrecompensa.setEnabled(false);
        //muriImagen.setEnabled(false);
        mgrupoFamiliar.setEnabled(false);
        //midCreador.setEnabled(false);
        mcondicion.setEnabled(false);
        //mestado.setEnabled(false);
        //mfecha.setEnabled(false);
        mfechaInicio.setEnabled(false);
        mfechaFin.setEnabled(false);
        mhoraInicio.setEnabled(false);
        mhoraFin.setEnabled(false);
        mpuntos.setEnabled(false);
        mdestino.setEnabled(false);
        mprioridad.setEnabled(false);
        mbtnCrear.setEnabled(false);
        //btnAgregarFoto.setEnabled(false);
        mbtnCancelar.setEnabled(false);
        mimgActivdad.setEnabled(false);
    }

}
