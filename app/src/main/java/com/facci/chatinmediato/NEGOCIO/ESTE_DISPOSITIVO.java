package com.facci.chatinmediato.NEGOCIO;

import android.content.Context;
import android.location.LocationManager;

import com.facci.chatinmediato.DB.DB_SOSCHAT;
import com.facci.chatinmediato.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class ESTE_DISPOSITIVO {
    public static double longitud=0;
    public static double latitud=0;

    public static String miNickName;
    public static String miMacAddress;

    public static Context context;

    public static DB_SOSCHAT db;

    public static GoogleMap map;


    public static boolean checkLocation(LocationManager locationManager) {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public static void ubicacion(GoogleMap map){
        if(map!=null){
            LatLng posicion = new LatLng(latitud, longitud);
            map.clear();
            map.addMarker(new MarkerOptions()
                    .position(posicion)
                    .title("Mi ubicación"));
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(posicion, 16.0f));
        }
    }

}
