package com.example.demo01.activities.notificacion;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.demo01.R;

public class NotificacionRealizarActividad extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "nofiticacionRealizar")
                .setSmallIcon(R.drawable.ic_add_alert_black_24dp)
                .setContentTitle("Tienes una actividad nueva")
                .setContentText("Revisa la actividad que tienes que cumplir")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(200,builder.build());
    }
}
