package com.facci.chatinmediato.Fragments;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.facci.chatinmediato.Adapters.AdaptadorDispositivos;
import com.facci.chatinmediato.FuncionActivity;
import com.facci.chatinmediato.InitThreads.ServerInit;
import com.facci.chatinmediato.NEGOCIO.Dispositivo;
import com.facci.chatinmediato.NEGOCIO.ESTE_DISPOSITIVO;
import com.facci.chatinmediato.NEGOCIO.Validaciones;
import com.facci.chatinmediato.R;
import com.facci.chatinmediato.Receivers.WifiDirectBroadcastReceiver;
import com.facci.chatinmediato.Reflexion.ReflectionUtils;
import com.facci.chatinmediato.Servicios.Nerby;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import static com.facci.chatinmediato.NEGOCIO.Dispositivo.requestPermissionFromDevice;


public class FM_encontrados extends Fragment {
    WifiManager wifiManager;
    public static final String DEFAULT_CHAT_NAME = Dispositivo.getDeviceName();
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private WifiDirectBroadcastReceiver mReceiver;
    private IntentFilter mIntentFilter;
    public static String chatName;
    public static ServerInit server;
    RecyclerView rv_participants;
    ProgressDialog pDialog;

    private SearchView searchView = null;
    private boolean searchViewShow = false;
    private SearchView.OnQueryTextListener queryTextListener;
    public ArrayList<String> listado2 = new ArrayList<>();
    public ArrayList<String[]> nueva = new ArrayList<>();
    private String[] caracter;
    private AdaptadorDispositivos adapter;

    SharedPreferences sharedPref;


    WifiP2pManager mWifiP2pManager;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v= inflater.inflate(R.layout.fragment_fm_encontrados, container, false);
        setHasOptionsMenu(true);

        pDialog=new ProgressDialog(getActivity());
        wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if(!wifiManager.isWifiEnabled())
            wifiManager.setWifiEnabled(true);

        mWifiP2pManager = (WifiP2pManager) getActivity().getSystemService(Context.WIFI_P2P_SERVICE);
        if (mWifiP2pManager != null) {
            mChannel = mWifiP2pManager.initialize(getContext(),  getActivity().getMainLooper(), null);
            if (mChannel == null)  mWifiP2pManager = null;
        }

        Dispositivo.requestPermissionFromDevice(getActivity());
        rv_participants=v.findViewById(R.id.participants_rv);
        rv_participants.setLayoutManager(new LinearLayoutManager(getActivity()));

        mManager = (WifiP2pManager) getActivity().getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(getActivity().getApplicationContext(), getActivity().getMainLooper(), null);
        mReceiver = WifiDirectBroadcastReceiver.createInstance();
        mReceiver.setmManager(mManager);
        mReceiver.setmChannel(mChannel);
        mReceiver.setmActivity(getActivity());
        mReceiver.setFragment(this);
        mReceiver.setRecycler(rv_participants);
        mReceiver.setDialogo(pDialog);

        FuncionActivity.server=server;
        FuncionActivity.chatName=Validaciones.loadChatName(getActivity(), "nickname", DEFAULT_CHAT_NAME);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        sharedPref = this.getActivity().getPreferences(Context.MODE_PRIVATE);

        descubrir();

        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragments, menu);
        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

            queryTextListener = new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextChange(String newText) {
                    return true;
                }
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return true;
                }
            };
            searchView.setOnQueryTextListener(queryTextListener);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void descubrir(){
        int numberOfParams = 3;
        Class[] methodParameters = new Class[numberOfParams];
        methodParameters[0] = WifiP2pManager.Channel.class;
        methodParameters[1] = String.class;
        methodParameters[2] = WifiP2pManager.ActionListener.class;

        Object arglist[] = new Object[numberOfParams];
        arglist[0] = mChannel;
        arglist[1] = ESTE_DISPOSITIVO.miNickName;
        arglist[2] = new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int reason) {

            }
        };
        ReflectionUtils.executePrivateMethod(mWifiP2pManager,WifiP2pManager.class,"setDeviceName",methodParameters,arglist);
        try {
            Thread.sleep(300);
            mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(getActivity(), R.string.search_toast, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int reason) {

                }
            });
        } catch (InterruptedException e) {
            Toast.makeText(getActivity(), R.string.no_search, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mReceiver, mIntentFilter);
        descubrir();
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mReceiver);
    }


}