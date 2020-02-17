package com.example.demo01.activities.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.ListAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.demo01.R;

import java.util.ArrayList;
import java.util.List;

public class GrupoDialogFrangment extends DialogFragment {

    List<String> grupos = new ArrayList<>();


    int position = 0;

    public interface GrupoListener{
        void onPositiveButtonGrupo(List<String> list, int position);
        void onNegativeButtonGrupo();
    }

    GrupoDialogFrangment.GrupoListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (GrupoDialogFrangment.GrupoListener) context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder grupo = new AlertDialog.Builder(getActivity());
        grupos.add("uno");
        grupos.add("dos");
        grupos.add("tres");

        grupo.setTitle("Selecciona el prioridad")
                .setSingleChoiceItems((ListAdapter) grupos, position, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        position = i;
                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        mListener.onPositiveButtonGrupo(grupos,position);
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        mListener.onNegativeButtonGrupo();
                    }
                });

        return grupo.create();
    }
}
