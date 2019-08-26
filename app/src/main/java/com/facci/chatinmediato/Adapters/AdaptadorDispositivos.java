package com.facci.chatinmediato.Adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.facci.chatinmediato.DB.DB_SOSCHAT;
import com.facci.chatinmediato.NEGOCIO.OTRO_DISPOSITIVO;
import com.facci.chatinmediato.R;
import com.facci.chatinmediato.VerMapaActivity;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;



public class AdaptadorDispositivos extends RecyclerView.Adapter<AdaptadorDispositivos.ViewHolderDatos> implements View.OnClickListener, Filterable {

    ArrayList<String[]> listado;
    ArrayList<String[]>primerListado;
    listaFilter filter;
    Context context;
    DB_SOSCHAT db;
    View view;
    Bundle bund;
    private MapView   mMapView;
    GoogleMap map;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    private View.OnClickListener listener;

    public AdaptadorDispositivos(ArrayList<String[]> listado, Context c) {
        this.listado = listado;
        this.primerListado = listado;
        this.context=c;
        //this.bund = bn;
        getFilter();
    }

    @NonNull
    @Override
    public AdaptadorDispositivos.ViewHolderDatos onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_participants, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(lp);
        view.setOnClickListener(this);
        db= new DB_SOSCHAT(this.context);

        return new ViewHolderDatos(view);}

    @Override
    public void onBindViewHolder(@NonNull AdaptadorDispositivos.ViewHolderDatos viewHolderDatos, final int i) {
        try{
            String nombre= Character.toUpperCase(listado.get(i)[0].charAt(0)) + listado.get(i)[0].substring(1,listado.get(i)[0].length());
            viewHolderDatos.txtNombre.setText(nombre);
            viewHolderDatos.txtMAC.setText(listado.get(i)[1]);
            if(db.validarAgregado(listado.get(i)[1])){
                viewHolderDatos.sw_agregado.setChecked(true);
            }else{
                viewHolderDatos.sw_agregado.setChecked(false);
            }
            viewHolderDatos.sw_agregado.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int respuesta=0;
                    if(isChecked){ respuesta=db.ActualizarUsuario(listado.get(i)[1], true);}
                    else{ respuesta=db.ActualizarUsuario(listado.get(i)[1], false);}
                    Snackbar.make(view, context.getString(respuesta), Snackbar.LENGTH_SHORT).show();
                }
            });
            viewHolderDatos.btn_mapa.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    OTRO_DISPOSITIVO.MacAddress = listado.get(i)[1];
                    Intent verMapa = new Intent(context, VerMapaActivity.class);
                    context.startActivity(verMapa);
                }
            });
        }catch (Exception e){

        }

    }

    @Override
    public int getItemCount() {
        return listado.size();
    }

    public void setOnClickListener(View.OnClickListener listen){
        this.listener=listen;
    }


    @Override
    public void onClick(View v) {
        if(listener!=null)
            listener.onClick(v);
    }

    @Override
    public Filter getFilter() {
        if (filter==null)  filter = new  listaFilter();
        return filter;
    }


    public class ViewHolderDatos extends RecyclerView.ViewHolder {
        TextView txtNombre, txtMAC;
        Switch sw_agregado;
        ImageButton btn_mapa;

        public ViewHolderDatos(@NonNull View itemView) {
            super(itemView);
            txtNombre = itemView.findViewById(R.id.name_tv);
            txtMAC = itemView.findViewById(R.id.ip_tv);
            sw_agregado =(Switch) itemView.findViewById(R.id.sw_agregado);
            btn_mapa = (ImageButton) itemView.findViewById(R.id.img_btn_mapa);
        }
    }

    private class listaFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if (constraint!=null && constraint.length()>0){
                constraint=constraint.toString().toUpperCase();
                ArrayList<String[]> Filtrado = new ArrayList<>();
                for (int i=0; i<listado.size();i++ ){
                    if (listado.get(i).toString().toUpperCase().contains(constraint)){
                        Filtrado.add(listado.get(i));
                    }
                }
                results.count=Filtrado.size();
                results.values= Filtrado;
            }else{
                results.count=listado.size();
                results.values= listado;
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            listado = (ArrayList<String[]>) results.values;
            notifyDataSetChanged();
        }
    }
}