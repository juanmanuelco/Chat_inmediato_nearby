package com.facci.chatinmediato.Adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.facci.chatinmediato.NEGOCIO.Mensajes;
import com.facci.chatinmediato.R;

import java.util.ArrayList;

public class AdaptadorMensajes extends RecyclerView.Adapter<AdaptadorMensajes.ViewHolderDatos> implements View.OnClickListener {
    Context context;
    View view;
    ArrayList<String[]> listado;

    private View.OnClickListener listener;

    public AdaptadorMensajes(ArrayList<String[]> listado, Context c) {
        this.listado = listado;
        this.context = c;
    }


    @NonNull
    @Override
    public AdaptadorMensajes.ViewHolderDatos onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_msg, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(lp);
        view.setOnClickListener(this);
        return new ViewHolderDatos(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdaptadorMensajes.ViewHolderDatos viewHolderDatos, final int i) {
        String nombre = Character.toUpperCase(listado.get(i)[0].charAt(0)) + listado.get(i)[0].substring(1, listado.get(i)[0].length());
        viewHolderDatos.txtNombre.setText(nombre);
        viewHolderDatos.msg_tv.setText(listado.get(i)[1]);
        viewHolderDatos.tiempo_tv.setText(Mensajes.contadorTiempo(Long.parseLong(listado.get(i)[2])));
    }

    @Override
    public int getItemCount() {
        return listado.size();
    }

    public void setOnClickListener(View.OnClickListener listen) {
        this.listener = listen;
    }

    @Override
    public void onClick(View v) {
        if (listener != null) listener.onClick(v);
    }

    public class ViewHolderDatos extends RecyclerView.ViewHolder {
        TextView txtNombre, msg_tv, tiempo_tv;

        public ViewHolderDatos(@NonNull View itemView) {
            super(itemView);
            txtNombre = itemView.findViewById(R.id.name_tv);
            msg_tv = itemView.findViewById(R.id.msg_tv);
            tiempo_tv = itemView.findViewById(R.id.tiempo_tv);
        }
    }
}