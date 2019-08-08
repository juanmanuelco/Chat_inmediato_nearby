package com.facci.chatinmediato;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;

import com.facci.chatinmediato.DB.DB_SOSCHAT;
import com.facci.chatinmediato.Entities.Mensaje;
import com.facci.chatinmediato.NEGOCIO.Dispositivo;
import com.facci.chatinmediato.NEGOCIO.ESTE_DISPOSITIVO;
import com.facci.chatinmediato.NEGOCIO.Mensajes;
import com.facci.chatinmediato.NEGOCIO.OTRO_DISPOSITIVO;
import com.facci.chatinmediato.NEGOCIO.Validaciones;
import com.facci.chatinmediato.Servicios.Nerby;
import com.facci.chatinmediato.Servicios.Ubicacion;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.MessagesClient;
import com.google.android.gms.nearby.messages.MessagesOptions;
import com.google.android.gms.nearby.messages.NearbyPermissions;

import static com.facci.chatinmediato.NEGOCIO.Dispositivo.GuardarPreferencia;
import static com.facci.chatinmediato.NEGOCIO.Dispositivo.REQUEST_CODE_REQUIRED_PERMISSIONS;
import static com.facci.chatinmediato.NEGOCIO.Dispositivo.REQUIRED_PERMISSIONS;
import static com.facci.chatinmediato.NEGOCIO.Dispositivo.hasPermissions;
import static com.facci.chatinmediato.NEGOCIO.ESTE_DISPOSITIVO.db;
import static com.facci.chatinmediato.NEGOCIO.Mensajes.getMacAddr;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class InicioActivity extends AppCompatActivity implements OnMapReadyCallback{
    Context           context;
    WifiManager       wifiManager;
    Intent            nerby_service, ubicacion;
    private MapView   mMapView;
    GoogleMap         map;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";


    EditText          editText_Nickname;
    ProgressDialog    pDialog;
    SharedPreferences sharedPref;
    Activity activity;
    private LinearLayout view_popup;
    static DB_SOSCHAT db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ESTE_DISPOSITIVO.context = this;
        setContentView(R.layout.activity_inicio);
        view_popup=findViewById(R.id.InicioActivityLayout);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if(!wifiManager.isWifiEnabled()) wifiManager.setWifiEnabled(true);
        Dispositivo.requestPermissionFromDevice(this);
        context = getApplicationContext();
        nerby_service = new Intent(this, Nerby.class);
        nerby_service.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ubicacion = new Intent(this, Ubicacion.class);
        ubicacion.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        mMapView = (MapView) findViewById(R.id.map);
        mMapView.onCreate(mapViewBundle);
        mMapView.getMapAsync(this);
        pDialog=new ProgressDialog(this);
        editText_Nickname= findViewById(R.id.ET_Main_Nickname);
        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        editText_Nickname.setText(sharedPref.getString("nickname", Dispositivo.getDeviceName()));
        int vida= sharedPref.getInt("TTLV", 24 );
        activity = this;
        db = new DB_SOSCHAT(this);
        ESTE_DISPOSITIVO.db=db;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            MessagesClient mMessagesClient = Nearby.getMessagesClient(this, new MessagesOptions.Builder()
                    .setPermissions(NearbyPermissions.BLE)
                    .build());
        }

    }

    public void wifi(View v){
        Mensajes.cargando(R.string.VERIFY, pDialog, this);
        String nickname= editText_Nickname.getText().toString();
        ESTE_DISPOSITIVO.miNickName = nickname;
        ESTE_DISPOSITIVO.miMacAddress = getMacAddr();
        if(Validaciones.vacio(new EditText[]{editText_Nickname})){
            GuardarPreferencia("nickname",nickname, 0, sharedPref, this);
            Intent act_chat= new Intent(InicioActivity.this, FuncionActivity.class);
            startActivity(act_chat);
            return;
        }
        Mensajes.mostrarMensaje(R.string.ERROR, R.string.NONAME, this);
    }

    @Override
    protected void onStart() {
        if (!hasPermissions(this, REQUIRED_PERMISSIONS) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_REQUIRED_PERMISSIONS);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            startService(ubicacion);
        } else {
            // Show rationale and request permission.
        }

        super.onStart();
        mMapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
        if (pDialog != null) pDialog.dismiss();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMapView.onStop();
        if (pDialog != null) pDialog.dismiss();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activitidad_inicio, menu);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (permissions.length == 1 &&
                permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        } else {
            // Permission was denied. Display an error message.
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Aqui tendran los eventos los iconos en el toolbar
        switch (item.getItemId()) {
            case R.id.emergencia:
                View MenuEmergente = findViewById(R.id.emergencia);
                showPopup(MenuEmergente);
                return true;
            case R.id.configuracion:
                final View mView= getLayoutInflater().inflate(R.layout.dialogo_configuracion, null);
                final NumberPicker NP= mView.findViewById(R.id.NP_hora);
                NP.setMinValue(1);
                NP.setMaxValue(24);
                NP.setValue(sharedPref.getInt("TTLV", 24 ));
                final AlertDialog.Builder BuilDialogo=new AlertDialog.Builder(InicioActivity.this)
                    .setPositiveButton(R.string.SAVE, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            GuardarPreferencia("TTLV", NP.getValue()+"", 1, sharedPref, activity);
                            Toast.makeText(InicioActivity.this, R.string.GUARD, Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton(R.string.CANC, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                BuilDialogo.setView(mView);
                final AlertDialog dialogo= BuilDialogo.create();
                dialogo.show();
                return true;
            case R.id.SERV_BUS:
                GuardarPreferencia("difusion", "true", 2, sharedPref, activity);
                startService(nerby_service);
                Toast.makeText(context, R.string.SERV_INI, Toast.LENGTH_SHORT).show();
                return true;
            case R.id.SERV_CANC:
                GuardarPreferencia("difusion", "false", 2, sharedPref, activity);
                stopService(nerby_service);
                Toast.makeText(context, R.string.SERV_STOP, Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;
        ESTE_DISPOSITIVO.map = map;
        ESTE_DISPOSITIVO.ubicacion(map);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }
        mMapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
        if (pDialog != null) pDialog.dismiss();
    }

    @Override
    protected void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
        if (pDialog != null) pDialog.dismiss();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
        if (pDialog != null) pDialog.dismiss();
    }

    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.envio_emergente:
                        ShowPopupWindows();
                        Toast.makeText(InicioActivity.this,"Emergencia",Toast.LENGTH_LONG).show();
                        break;
                }
                return true;
            }
        });
        //popup.inflate(R.menu.seleccionar_archivo);
        /*  The below code in try catch is responsible to display icons*/
        try {
            Field[] fields = popup.getClass().getDeclaredFields();
            for (Field field : fields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popup);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        popup.getMenuInflater().inflate(R.menu.envio_emergencia, popup.getMenu());
        popup.show();
    }

    public void ShowPopupWindows(){
        View popupView = getLayoutInflater().inflate(R.layout.boton_panico_mensaje, null);

        final PopupWindow popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);

        popupWindow.setTouchable(true);
        popupWindow.setFocusable(true);

        popupWindow.showAtLocation(view_popup, Gravity.CENTER, 0, 0);

        View vista = popupWindow.getContentView();

        final EditText sms = (EditText) vista.findViewById(R.id.edt_msm_emergencia);
        Button  btn_enviar_sms = (Button) vista.findViewById(R.id.btn_enviar_msm_emergencia);

        sms.setText("Estoy en una emergencia, necesito ayuda mis coordenadas son, LATITUD:"+ ESTE_DISPOSITIVO.latitud+" LONGITUD:"+ESTE_DISPOSITIVO.longitud);

        btn_enviar_sms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long millis = System.currentTimeMillis();
                Mensaje mes = new Mensaje(1, sms.getText().toString(), null, ESTE_DISPOSITIVO.miNickName);
                mes.setTiempoEnvio(Math.abs(millis));
                mes.setIdentificacion(true);
                mes.setMacOrigen(getMacAddr());
                mes.setMacDestino(OTRO_DISPOSITIVO.MacAddress);
                mes.setEmergente("true");

                db.guardarRegistro(mes);

            }
        });
    }

}
