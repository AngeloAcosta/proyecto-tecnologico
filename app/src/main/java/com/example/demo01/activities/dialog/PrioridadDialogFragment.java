package com.example.demo01.activities.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.demo01.R;

public class PrioridadDialogFragment extends DialogFragment {

    int position = 0;

    public interface PrioridadListener{
        void onPositiveButtonClicked(String[] list, int position);
        void onNegativeButtonClicked();
    }

    PrioridadDialogFragment.PrioridadListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (PrioridadDialogFragment.PrioridadListener) context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder prioridad = new AlertDialog.Builder(getActivity());
        final String[] list = getActivity().getResources().getStringArray(R.array.prioridad_item);

        prioridad.setTitle("Selecciona el prioridad")
                .setSingleChoiceItems(list, position, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        position = i;
                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        mListener.onPositiveButtonClicked(list,position);
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        mListener.onNegativeButtonClicked();
                    }
                });

        return prioridad.create();
    }
}
