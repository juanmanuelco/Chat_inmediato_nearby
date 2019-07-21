package com.facci.chatinmediato.NEGOCIO;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.facci.chatinmediato.DB.DB_SOSCHAT;
import com.facci.chatinmediato.Entities.Mensaje;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;

public class Listeners {
    private Context context;
    private String nombre_peer;
    static DB_SOSCHAT db;

    public Listeners(Context c, String nombre_peer){
        this.context =c;
        this.nombre_peer = nombre_peer;
        this.db = ESTE_DISPOSITIVO.db;
    }
    public OnSuccessListener success = new OnSuccessListener<Void>() {
        @Override
        public void onSuccess(Void aVoid) {
            Toast.makeText(context, "Buscando...", Toast.LENGTH_SHORT).show();
        }
    };
    public OnFailureListener fail = new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            Toast.makeText(context, "Fallo"+ e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    public final ConnectionLifecycleCallback connectionLifecycleCallback = new ConnectionLifecycleCallback() {
        @Override
        public void onConnectionInitiated(final String endpointId, ConnectionInfo connectionInfo) {
            Nearby.getConnectionsClient(context).acceptConnection(endpointId, payloadCallback);
            Toast.makeText(context, "Conectado con el dispositivo", Toast.LENGTH_SHORT).show();

            List<Mensaje> listado_bloque = db.todos_mensajes();
            for (final Mensaje mensaje: listado_bloque) {
                try {
                    byte[] msg = Validaciones.NearbySerialize(mensaje);
                    Payload bytesPayload = Payload.fromBytes(msg);
                    Nearby.getConnectionsClient(context).sendPayload(endpointId, bytesPayload);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onConnectionResult(String endpointId, ConnectionResolution result) {
            conectividad(endpointId, result);
            Nearby.getConnectionsClient(context).acceptConnection(endpointId, payloadCallback);
            Toast.makeText(context, "Conectado con el dispositivo", Toast.LENGTH_SHORT).show();

            List<Mensaje> listado_bloque = db.todos_mensajes();
            for (final Mensaje mensaje: listado_bloque) {
                try {
                    byte[] msg = Validaciones.NearbySerialize(mensaje);
                    Payload bytesPayload = Payload.fromBytes(msg);
                    Nearby.getConnectionsClient(context).sendPayload(endpointId, bytesPayload);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onDisconnected(String endpointId) {

        }
    };

    public final EndpointDiscoveryCallback endpointDiscoveryCallback = new EndpointDiscoveryCallback() {
        @Override
        public void onEndpointFound(String endpointId, DiscoveredEndpointInfo info) {
            Nearby.getConnectionsClient(context)
            .requestConnection(nombre_peer, endpointId, connectionLifecycleCallback)
            .addOnSuccessListener(success)
            .addOnFailureListener(fail);
            Toast.makeText(context, "Punto de conexión encontrado", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onEndpointLost(String endpointId) {
            Toast.makeText(context, "Punto de conexion perdido", Toast.LENGTH_SHORT).show();
        }
    };
    private PayloadCallback payloadCallback = new PayloadCallback() {
        @Override
        public void onPayloadReceived(@NonNull String s, @NonNull Payload payload) {

            try {
                Mensaje msg = (Mensaje) Validaciones.NearbyDeserialize(payload.asBytes());
                Toast.makeText(context, "Recepcion de: "+msg.getTexto(), Toast.LENGTH_SHORT).show();
                db.guardarRegistro(msg);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onPayloadTransferUpdate(@NonNull String s, @NonNull PayloadTransferUpdate payloadTransferUpdate) {

        }
    };

    private void conectividad (String endpointId, ConnectionResolution result){
        switch (result.getStatus().getStatusCode()) {
            case ConnectionsStatusCodes.STATUS_OK:
                Toast.makeText(context, "Se realizo la conexion con éxito", Toast.LENGTH_SHORT).show();
                break;
            case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                Toast.makeText(context, "Conexion rechazada", Toast.LENGTH_SHORT).show();
                break;
            case ConnectionsStatusCodes.STATUS_ERROR:
                Toast.makeText(context, "Error extraño", Toast.LENGTH_SHORT).show();
                break;
            default:
                // Unknown status code
        }
    }
}
