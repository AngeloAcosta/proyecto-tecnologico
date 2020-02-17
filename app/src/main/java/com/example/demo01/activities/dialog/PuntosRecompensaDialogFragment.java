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

public class PuntosRecompensaDialogFragment extends DialogFragment {

    int position = 0;

    public interface PuntosListener{
        void onPositiveButtonClicked(String[] list, int position);
        void onNegativeButtonClicked();
    }

    PuntosRecompensaDialogFragment.PuntosListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (PuntosRecompensaDialogFragment.PuntosListener) context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final String[] list = getActivity().getResources().getStringArray(R.array.puntosRecompansa_item);

        builder.setTitle("Selecciona el puntaje")
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

        return builder.create();
    }
}
