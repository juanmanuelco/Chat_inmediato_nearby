package com.facci.chatinmediato.Fragments;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facci.chatinmediato.Adapters.AdaptadorDispositivos;
import com.facci.chatinmediato.ChatActivity;
import com.facci.chatinmediato.ChatOffLineActivity;
import com.facci.chatinmediato.DB.DB_SOSCHAT;
import com.facci.chatinmediato.NEGOCIO.OTRO_DISPOSITIVO;
import com.facci.chatinmediato.R;

import java.util.ArrayList;

public class FM_historico extends Fragment {

    private SearchView searchView = null;
    private boolean searchViewShow = false;
    private SearchView.OnQueryTextListener queryTextListener;

    RecyclerView rv_participants;
    AdaptadorDispositivos adaptadorDispositivos;
    ArrayList<String[]> encontrados, EncontradosBusqueda;
    ArrayList<String> searches;
    DB_SOSCHAT db;
    TextView Sin_historicos;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v= inflater.inflate(R.layout.fragment_fm_historico, container, false);
        setHasOptionsMenu(true);
        // ---------------------------------------------------------------------------
        db= new DB_SOSCHAT(getActivity());
        encontrados= new ArrayList<>();
        searches = new ArrayList<>();
        searches=db.buscador();
        encontrados= db.listaEncontrados();
        Sin_historicos= v.findViewById(R.id.informacion_historico);
        rv_participants=v.findViewById(R.id.participants_rv);
        adaptadorDispositivos= new AdaptadorDispositivos(encontrados, getActivity());
        // --------------------------------------------------------------------------
        if(encontrados.size()<1){
            Sin_historicos.setText(R.string.NOADD);
        }else{
            Sin_historicos.setText("");
        }

        rv_participants.setLayoutManager(new LinearLayoutManager(getActivity()));

        adaptadorDispositivos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OTRO_DISPOSITIVO.MacOnclic= encontrados.get(rv_participants.getChildAdapterPosition(v))[1];
                OTRO_DISPOSITIVO.MacAddress= OTRO_DISPOSITIVO.MacOnclic;
                OTRO_DISPOSITIVO.nombreOffline= encontrados.get(rv_participants.getChildAdapterPosition(v))[0];
                Intent intent = new Intent(getActivity().getApplicationContext(), ChatOffLineActivity.class);
                getActivity().startActivity(intent);
            }
        });
        rv_participants.setAdapter(adaptadorDispositivos);
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
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
                    if (newText != null && !newText.isEmpty()) {
                        String palabra = newText.toLowerCase();
                        EncontradosBusqueda= new ArrayList<>();
                        for (int i=0; i<encontrados.size();i++ ){
                            String[] partes = searches.get(i).split(",");
                            String nombre = partes[0];
                            String mac = partes[1];
                            if (nombre.toLowerCase().contains(palabra)){
                                EncontradosBusqueda.add(new String[]{nombre,mac});
                                //Log.i("encontrado",nombre +" "+ mac);
                            }
                        }
                        adaptadorDispositivos= new AdaptadorDispositivos(EncontradosBusqueda, getActivity());
                        rv_participants.setAdapter(adaptadorDispositivos);
                        return true;
                    }else{
                        adaptadorDispositivos= new AdaptadorDispositivos(encontrados, getActivity());
                        rv_participants.setAdapter(adaptadorDispositivos);
                        return true;
                    }
                }
                @Override
                public boolean onQueryTextSubmit(String query) {
                    Log.i("onQueryTextSubmit", query);

                    return true;
                }
            };
            searchView.setOnQueryTextListener(queryTextListener);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Aqui tendran los eventos los iconos en el toolbar
        switch (item.getItemId()) {
            case R.id.configuracion:
                Toast.makeText(getContext(),"Configuración",Toast.LENGTH_LONG).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
