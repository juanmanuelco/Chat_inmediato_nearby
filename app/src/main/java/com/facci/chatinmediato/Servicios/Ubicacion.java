package com.facci.chatinmediato.Servicios;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.os.IBinder;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.facci.chatinmediato.NEGOCIO.ESTE_DISPOSITIVO;
import com.facci.chatinmediato.R;

import static com.facci.chatinmediato.NEGOCIO.ESTE_DISPOSITIVO.checkLocation;

public class Ubicacion extends Service {
    LocationManager locationManager;
    SharedPreferences sharedPref;
    Intent            nerby_service;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        nerby_service = new Intent(this, Nerby.class);
        nerby_service.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        sharedPref =  PreferenceManager.getDefaultSharedPreferences(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!checkLocation(locationManager)){
            Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(myIntent);
        }else{
            locationManager.removeUpdates(locationListenerGPS);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            }
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2 * 20 * 1000, 10, locationListenerGPS);
            locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 2 * 20 * 1000, 10, locationListenerGPS);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2 * 20 * 1000, 10, locationListenerGPS);

        }
        return super.onStartCommand(intent, flags, startId);
    }
    private final LocationListener locationListenerGPS = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            ESTE_DISPOSITIVO.longitud = location.getLongitude();
            ESTE_DISPOSITIVO.latitud = location.getLatitude();
            ESTE_DISPOSITIVO.ubicacion(ESTE_DISPOSITIVO.map);
            boolean nearby = sharedPref.getBoolean("difusion", true);
            if(nearby) startService(nerby_service); else stopService(nerby_service);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            Toast.makeText(Ubicacion.this, "Status cambiado", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderEnabled(String s) {
            Toast.makeText(Ubicacion.this, "Proveedor activado", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderDisabled(String s) {
            Toast.makeText(Ubicacion.this, "Proveedor deshabilitado", Toast.LENGTH_SHORT).show();
        }
    };
}
