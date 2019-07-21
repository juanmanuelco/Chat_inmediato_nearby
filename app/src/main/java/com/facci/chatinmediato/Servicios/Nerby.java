package com.facci.chatinmediato.Servicios;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facci.chatinmediato.NEGOCIO.Dispositivo;
import com.facci.chatinmediato.NEGOCIO.Listeners;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import static com.google.android.gms.nearby.connection.Strategy.P2P_CLUSTER;

public class Nerby extends Service {
    String nombre_peer = Dispositivo.getDeviceName();
    String id_clus = "";
    Context context = null;
    Listeners listeners;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        id_clus = getPackageName();
        context = this.getApplicationContext();
        listeners = new Listeners(context, nombre_peer);
        startAdvertising();
        startDiscovery();
        return super.onStartCommand(intent, flags, startId);
    }

    private void startAdvertising() {
        AdvertisingOptions advertisingOptions = new AdvertisingOptions.Builder().setStrategy(P2P_CLUSTER).build();
        Nearby.getConnectionsClient(context)
            .startAdvertising(nombre_peer,id_clus , listeners.connectionLifecycleCallback, advertisingOptions)
            .addOnSuccessListener(listeners.success)
            .addOnFailureListener(listeners.fail);
    }

    private void startDiscovery() {
        DiscoveryOptions discoveryOptions = new DiscoveryOptions.Builder().setStrategy(P2P_CLUSTER).build();
        Nearby.getConnectionsClient(context)
                .startDiscovery(id_clus, listeners.endpointDiscoveryCallback, discoveryOptions)
                .addOnSuccessListener(listeners.success)
                .addOnFailureListener(listeners.fail);
    }
}
