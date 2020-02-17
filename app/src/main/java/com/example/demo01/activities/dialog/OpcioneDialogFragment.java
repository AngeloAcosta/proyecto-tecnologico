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

public class OpcioneDialogFragment extends DialogFragment {

    private int position = 0;

    public interface OpcioneListener{
        void onPositiveButtonClicked(String string, int position);
        void onNegativeButtonClicked();
    }

    private OpcioneDialogFragment.OpcioneListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (OpcioneDialogFragment.OpcioneListener) context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //final String[] list = getActivity().getResources().getStringArray(R.array.puntos_item);

        builder.setTitle("REALIZASTE LA ACTIVIDAD")
                .setPositiveButton("SI!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        mListener.onPositiveButtonClicked("REALIZADO", position);
                    }
                })
                .setNegativeButton("AUN NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        mListener.onNegativeButtonClicked();
                    }
                });

        return builder.create();
    }
}
