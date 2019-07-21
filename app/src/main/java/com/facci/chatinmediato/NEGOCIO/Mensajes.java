package com.facci.chatinmediato.NEGOCIO;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;

import com.facci.chatinmediato.R;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;



public class Mensajes {
    public static void mostrarMensaje(String t, String m, Context c){
        /**Crea el dialogo para mostrarlo*/
        AlertDialog.Builder alertDialogBuilder;
        alertDialogBuilder=new AlertDialog.Builder(c);

        /**Personaliza el dialogo*/
        alertDialogBuilder.setTitle(t);
        alertDialogBuilder.setMessage(m);
        alertDialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog=alertDialogBuilder.create();



        /**Muestra el dialogo*/
        alertDialog.show();
    }

    public static void mostrarMensaje(int t, int m, Context c){
        /**Crea el dialogo para mostrarlo*/
        AlertDialog.Builder alertDialogBuilder;
        alertDialogBuilder=new AlertDialog.Builder(c);

        /**Personaliza el dialogo*/
        alertDialogBuilder.setTitle(c.getResources().getString(t));
        alertDialogBuilder.setMessage(c.getResources().getString(m));
        alertDialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog=alertDialogBuilder.create();

        /**Muestra el dialogo*/
        alertDialog.show();
    }
    public static void cargando(String m, ProgressDialog p){
        /**Muestra un mensaje de carga*/
        p.setMessage(m);
        p.show();
    }
    public static void cargando(int m, ProgressDialog p, Context C){
        /**Muestra un mensaje de carga*/
        p.setMessage(C.getResources().getString(m));
        p.show();
    }
    public static String datosSenal(int velocidad, int frecuencia, int fuerza){
        String respuesta= "Velocidad: " + velocidad +" Mbps, ";
        respuesta=respuesta.concat("Frecuencia: " + frecuencia+" Mhz, ");
        respuesta=respuesta.concat("Fuerza de señal: " + fuerza);
        return respuesta;
    }
    public static String getMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(Integer.toHexString(b & 0xFF) + ":");
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
        }
        return "02:00:00:00:00:00";
    }
    public static String  contadorTiempo(long tiempo){
        String respuesta="";
        long tiempoActual= System.currentTimeMillis();
        long resta = Math.abs(tiempoActual-tiempo);
        if(resta < 1000) respuesta = "Justo ahora";
        else if(resta < 60000) respuesta = "Hace menos de un minuto";
        else if (resta < 3600000 ) respuesta = "Hace "+ Math.round(resta/60000) +" minutos";
        else if (resta < 86400000) respuesta = "Hace "+ Math.round(resta/3600000) + " horas";
        return respuesta;
    }
}