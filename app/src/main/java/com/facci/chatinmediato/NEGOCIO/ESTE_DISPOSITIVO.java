package com.facci.chatinmediato.NEGOCIO;

import android.content.Context;
import android.location.LocationManager;
import android.widget.Toast;

import com.facci.chatinmediato.DB.DB_SOSCHAT;
import com.facci.chatinmediato.DB.Tablas.TB_mensajes;
import com.facci.chatinmediato.Entities.Mensaje;
import com.facci.chatinmediato.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import static com.facci.chatinmediato.NEGOCIO.Validaciones.comparar_mac;

public class ESTE_DISPOSITIVO {
    public static double longitud=0;
    public static double latitud=0;

    public static boolean ubicacion = false;

    public static String miNickName="";
    public static String miMacAddress="";

    public static Context context;

    public static DB_SOSCHAT db;

    public static GoogleMap map;


    public static boolean checkLocation(LocationManager locationManager) {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public static void ubicacion(GoogleMap map){
        if(!ubicacion){
            if(map!=null){
                LatLng posicion = new LatLng(latitud, longitud);
                map.clear();
                map.addMarker(new MarkerOptions()
                        .position(posicion)
                        .title("Mi ubicación"));
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(posicion, 16.0f));
            }
        }
        else{
            ubicacion_msg(map);
        }
    }

    public static void ubicacion_msg(GoogleMap map) {
        if(map!=null) {
            map.clear();

            LatLng posicion = new LatLng(ESTE_DISPOSITIVO.latitud, ESTE_DISPOSITIVO.longitud);
            String MAC_destino = OTRO_DISPOSITIVO.MacAddress;
            if(context != null){
                List<Mensaje> mensajesMac = TB_mensajes.mensajesMac(MAC_destino, context);
                map.addMarker(new MarkerOptions().position(posicion).title("Yo estoy aquí").icon(BitmapDescriptorFactory.fromResource(R.drawable.actual)));
                int numero = 1;
                for (Mensaje mensaje : mensajesMac) {

                    if(comparar_mac(MAC_destino, mensaje.getMacOrigen())){
                        map.addMarker(new MarkerOptions().position(
                                new LatLng(mensaje.getLatitud(), mensaje.getLongitud())
                        ).title("Mensaje No: " + numero));
                    }
                    numero++;
                }
            }
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(posicion, 16.0f));

        }
    }
}
