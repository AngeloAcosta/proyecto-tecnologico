package com.example.demo01.activities.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;

import androidx.fragment.app.DialogFragment;

import com.example.demo01.R;

public class CargandoDialogFragment extends DialogFragment {

    private Activity activity;
    private AlertDialog dialog;

    public CargandoDialogFragment(Activity mactivity){
        activity = mactivity;
    }

    public void startLoadingDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.layout_dialog_cargando, null));
        builder.setCancelable(false);

        dialog = builder.create();
        dialog.show();
    }

    public void dismissDialog(){
        dialog.dismiss();
    }

}
