package com.facci.chatinmediato.Receivers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.facci.chatinmediato.Adapters.AdaptadorDispositivos;
import com.facci.chatinmediato.ChatActivity;
import com.facci.chatinmediato.DB.DB_SOSCHAT;
import com.facci.chatinmediato.Fragments.FM_encontrados;
import com.facci.chatinmediato.FuncionActivity;
import com.facci.chatinmediato.InitThreads.ClientInit;
import com.facci.chatinmediato.InitThreads.ServerInit;
import com.facci.chatinmediato.NEGOCIO.OTRO_DISPOSITIVO;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.facci.chatinmediato.NEGOCIO.Mensajes.cargando;

public class WifiDirectBroadcastReceiver extends BroadcastReceiver{

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private Activity mActivity;
    boolean conteo=true;

    WifiManager wifiManager;
    static DB_SOSCHAT db;

    public static final int IS_OWNER = 1;
    public static final int IS_CLIENT = 2;

    private List<String> peersName = new ArrayList<String>();
    private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    private int isGroupeOwner;
    private InetAddress ownerAddr;

    public ArrayList <String[]> listado;
    public ArrayList<String> listado2;
    WifiP2pDevice[] deviceArray;
    FM_encontrados fm;
    Bundle bun;

    RecyclerView RV;
    ProgressDialog pDialog;

    String MacCalculo;
    StringBuilder MacAddress;

    private static WifiDirectBroadcastReceiver instance;

    private WifiDirectBroadcastReceiver(){
        super();
    }

    public static WifiDirectBroadcastReceiver createInstance(){
        if(instance == null) instance = new WifiDirectBroadcastReceiver();
        return instance;
    }
    public int isGroupeOwner() { return isGroupeOwner; }
    public InetAddress getOwnerAddr() { return ownerAddr; }
    public void setmManager(WifiP2pManager mManager) { this.mManager = mManager; }
    public void setmChannel(WifiP2pManager.Channel mChannel) { this.mChannel = mChannel; }
    public void setmActivity(Activity mActivity) { this.mActivity = mActivity; }
    public void setFragment(FM_encontrados FM){ this.fm= FM;}
    public void setRecycler(RecyclerView  recycler){ this.RV=recycler;}
    public void setDialogo(ProgressDialog dialogo){this.pDialog=dialogo;}
    public void setBun(Bundle bun){this.bun = bun;}

    @Override
    public void onReceive(final Context context, Intent intent) {

        String action = intent.getAction();
        listado= new ArrayList<String[]>();
        listado2= new ArrayList<>();
        wifiManager = (WifiManager) mActivity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        db = new DB_SOSCHAT(context);

        if(action.equals(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)){
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if(state != WifiP2pManager.WIFI_P2P_STATE_ENABLED)
                if(!wifiManager.isWifiEnabled()) wifiManager.setWifiEnabled(true);
            return;
        }

        if(action.equals(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)){
            if(mManager!=null){
                mManager.requestPeers(mChannel, new WifiP2pManager.PeerListListener() {
                    @Override
                    public void onPeersAvailable(WifiP2pDeviceList peerList) {
                        if(!peerList.getDeviceList().equals(peers)){
                            peers.clear();
                            listado.clear();
                            peers.addAll(peerList.getDeviceList());
                            deviceArray= new WifiP2pDevice[peerList.getDeviceList().size()];
                            int index=0;
                            for(WifiP2pDevice device : peerList.getDeviceList()){

                                MacCalculo = restarHexadecimal(device.deviceAddress.substring(0,2));
                                Log.i("RestaMac", " Mac calculada: "+MacCalculo+" Mac normal: "+device.deviceAddress);
                                MacAddress = new StringBuilder(device.deviceAddress);
                                MacAddress.setCharAt(0,MacCalculo.charAt(0));
                                MacAddress.setCharAt(1,MacCalculo.charAt(1));


                                listado.add(new String[]{device.deviceName, MacAddress.toString().toLowerCase()});
                                listado2.add(device.deviceName+","+device.deviceAddress);

                                db.insertarUsuario(MacAddress.toString().toLowerCase(), device.deviceName);

                                deviceArray[index]= device;
                                index++;
                            }

                            AdaptadorDispositivos adapter= new AdaptadorDispositivos(listado, context);
                            adapter.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    final WifiP2pDevice device = deviceArray[RV.getChildAdapterPosition(v)];

                                    WifiP2pConfig config =  new WifiP2pConfig();
                                    config.deviceAddress=device.deviceAddress;
                                    OTRO_DISPOSITIVO.MacAddress=device.deviceAddress;
                                    mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                                        @Override
                                        public void onSuccess() {
                                            Toast.makeText(context, "Conectando con: "+ device.deviceName, Toast.LENGTH_SHORT).show();
                                        }
                                        @Override
                                        public void onFailure(int reason) {
                                            Toast.makeText(context, "Error al conectarse con "+ device.deviceName, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            });
                            RV.setAdapter(adapter);
                        }
                    }
                });
            }
            return;
        }
        if(action.equals(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)){
            return;
        }

        if(action.equals(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)){
            if(mManager == null) return;
            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if(networkInfo.isConnected() && conteo){
                mManager.requestConnectionInfo(mChannel, new WifiP2pManager.ConnectionInfoListener() {

                    @Override
                    public void onConnectionInfoAvailable(WifiP2pInfo info) {
                        InetAddress groupOwnerAddress = info.groupOwnerAddress;
                        ownerAddr= groupOwnerAddress;
                        if (info.groupFormed && info.isGroupOwner) {
                            isGroupeOwner = IS_OWNER;
                            fm.server=new ServerInit();
                            fm.server.start();
                            FuncionActivity.server=fm.server;
                            mManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener() {
                                @Override
                                public void onGroupInfoAvailable(WifiP2pGroup group) {
                                    Collection<WifiP2pDevice> clientes = group.getClientList();
                                    ArrayList<WifiP2pDevice> lista_cliente = new ArrayList<>();
                                    for (WifiP2pDevice cliente : clientes ) {
                                        lista_cliente.add(cliente);
                                    }
                                    if(lista_cliente.size() >0){
                                        OTRO_DISPOSITIVO.MacAddress = lista_cliente.get(0).deviceAddress;
                                        OTRO_DISPOSITIVO.MacOnclic = lista_cliente.get(0).deviceAddress;
                                    }
                                }
                            });
                        }
                        else if (info.groupFormed) {
                            isGroupeOwner = IS_CLIENT;
                            ClientInit client = new ClientInit(getOwnerAddr());
                            client.start();
                        }
                        Intent intent = new Intent(mActivity.getApplicationContext(), ChatActivity.class);
                        mActivity.startActivity(intent);
                        conteo=false;
                    }
                });
            }
        }
    }
    public ArrayList<String> retornar(){
        return listado2;
    }
    private static String restarHexadecimal(String primervalor) {
        String primerOperando = convertirHexadecimalADecimal(primervalor);
        String segundoOperando = convertirHexadecimalADecimal("02");

        int primero = Integer.parseInt(primerOperando);
        int segundo = Integer.parseInt(segundoOperando);
        Log.i("RestaHex","Primero: "+primero+" Segundo: "+segundo);
        int operacion = primero - segundo;

        String resultado = "" + operacion;
        return convertirDecimalAhexadecimal(resultado);
    }

    public static String convertirHexadecimalADecimal(String base){
        String digitos = "0123456789ABCDEF";
        String decimal = "";
        base = base.toUpperCase();
        int decimalInt = 0;
        for (int i = 0; i < base.length(); i++)
        {
            char c = base.charAt(i);
            int d = digitos.indexOf(c);
            decimalInt = 16 * decimalInt + d;
        }
        decimal += decimalInt;
        return decimal;
    }

    public static String convertirDecimalAhexadecimal(String bas){
        int decimal = Integer.parseInt(bas);
        String digits = "0123456789ABCDEF";
        if (decimal == 0)
            return "00";
        String hexde = "";
        while (decimal > 0) {
            int mod = decimal % 16; // DÃƒÂ­gito de la derecha
            hexde = digits.charAt(mod) + hexde; // ConcatenaciÃƒÂ³n de cadenas
            decimal = decimal / 16;
        }
        if(hexde.length() == 1){
            hexde = "0"+hexde;
        }
        return hexde;
    }
}