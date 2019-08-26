package com.facci.chatinmediato;

import androidx.fragment.app.FragmentActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import com.facci.chatinmediato.DB.Tablas.TB_mensajes;
import com.facci.chatinmediato.Entities.Mensaje;
import com.facci.chatinmediato.NEGOCIO.ESTE_DISPOSITIVO;
import com.facci.chatinmediato.NEGOCIO.OTRO_DISPOSITIVO;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

import static com.facci.chatinmediato.NEGOCIO.Validaciones.comparar_mac;

public class VerMapaActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_mapa);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap map) {
        if(map!=null) {
            map.clear();

            LatLng posicion = new LatLng(ESTE_DISPOSITIVO.latitud, ESTE_DISPOSITIVO.longitud);
            String MAC_destino = OTRO_DISPOSITIVO.MacAddress;
            List<Mensaje> mensajesMac = TB_mensajes.mensajesMac(MAC_destino, this);
            map.addMarker(new MarkerOptions().position(posicion).title("Yo estoy aquí").icon(BitmapDescriptorFactory.fromResource(R.drawable.actual)));
            int numero = 1;
            PolylineOptions opciones = new PolylineOptions();
            for (Mensaje mensaje : mensajesMac) {

                if(comparar_mac(MAC_destino, mensaje.getMacOrigen())){
                    map.addMarker(new MarkerOptions().position(
                            new LatLng(mensaje.getLatitud(), mensaje.getLongitud())
                    ).title("Mensaje No: " + numero));
                    Toast.makeText(this,mensaje.getLatitud() +"---" +mensaje.getLongitud(), Toast.LENGTH_SHORT).show();

                    opciones.add(new LatLng(mensaje.getLatitud(), mensaje.getLongitud())).width(5).color(Color.RED);

                }
                numero++;
            }
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(posicion, 16.0f));

            Polyline line = map.addPolyline(opciones);
        }

    }
}
