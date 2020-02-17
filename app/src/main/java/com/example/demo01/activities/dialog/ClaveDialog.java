package com.example.demo01.activities.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.demo01.R;

import java.util.Objects;

public class ClaveDialog extends DialogFragment {

    private EditText mClaveFamilia;
    private ClaveDialogListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_dialog_clave, null);

        mClaveFamilia = view.findViewById(R.id.txtClaveUnirme);

        builder.setView(view)
                .setTitle("Unirse al Grupo Familiar")
                .setNegativeButton("cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {

                    }
                })
                .setPositiveButton("UNIRME", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String clave = mClaveFamilia.getText().toString();
                        String claveRec = "";
                        listener.onDialogAction(clave, claveRec);
                    }
                });

        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (ClaveDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(e
                    + " must implement NoticeDialogListener");
        }

    }

    public interface ClaveDialogListener {
        void onDialogAction(String claveEnviar, String claveRecivido);
        void onDialogPositiveClick(DialogFragment dialog);
        void onDialogNegativeClick(DialogFragment dialog);

    }


}
