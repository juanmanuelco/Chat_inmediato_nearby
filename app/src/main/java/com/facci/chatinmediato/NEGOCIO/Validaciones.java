package com.facci.chatinmediato.NEGOCIO;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.EditText;

import com.facci.chatinmediato.Entities.Mensaje;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Validaciones {
    public static boolean vacio(EditText[] campos){
        int visor=0;
        /**Obtiene una lista del valor de cada caja de texto*/
        ArrayList list = new ArrayList();
        for (int i = 0; i < campos.length; i++){
            list.add(campos[i].getText().toString().trim());
        }

        /**Usa un patron Iterador para recorrer la lista*/
        Iterator e = list.iterator();
        while (e.hasNext()){
            Object obj = e.next();
            if (!obj.toString().equals(""))
                visor+=1;
        }
        /**Si el tamaño de la lista es igual al tamaño del array entonces no hay espacios en blanco*/
        if(visor==campos.length)
            return true;
        else
            return false;
    }
    public static String loadChatName(Context context, String key, String defaultText) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(key, defaultText);
    }

    public static byte[] obtenerInet(InetAddress direccion){
        byte[] resultado=null;
        try{ resultado=direccion.getAddress(); }catch (Exception e){ }
        return resultado;
    }

    public static InetAddress obtenerInetAddress(byte[] valor){
        InetAddress respuesta= null;
        try {respuesta=InetAddress.getByAddress(valor);
        } catch (UnknownHostException e) { e.printStackTrace();}
        return respuesta;
    }

    public static String Formateados(int tope){
        String respueta ="";
        for (int i=0; i < tope; i++ ){
            respueta += "%s, ";
        }
        respueta = respueta.substring(0, respueta.length()-2);
        return respueta;
    }

    public static String FormateadosR(int tope){
        String respueta ="";
        for (int i=0; i < tope; i++ ){
            respueta += "'%s', ";
        }
        respueta = respueta.substring(0, respueta.length()-2);
        return respueta;
    }

    public static byte[] NearbySerialize(Mensaje object) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        // transform object to stream and then to a byte array
        objectOutputStream.writeObject(object);
        objectOutputStream.flush();
        objectOutputStream.close();
        return byteArrayOutputStream.toByteArray();
    }

    public static Object NearbyDeserialize(byte[] bytes) throws IOException, ClassNotFoundException{
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        return objectInputStream.readObject();
    }



    public static Boolean isActivityRunning(Class activityClass, Context mContext) {
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

        for (ActivityManager.RunningTaskInfo task : tasks) {
            if (activityClass.getCanonicalName().equalsIgnoreCase(task.baseActivity.getClassName()))
                return true;
        }
        return false;
    }

    public static String  obtenerPeso(int peso){
        String mensaje= "";
        if(peso>1024 && peso < 1048576) mensaje= Math.round(peso/1024)+" KB";
        else mensaje= Math.round(peso/1048576)+" MB";
        if(peso<1024) mensaje= peso +" Bytes";
        return mensaje;
    }

    public static boolean mensaje_para_mi(Mensaje mensaje){
        boolean respuesta = false;
        boolean mio_origen = mensaje.getMacOrigen().equals(ESTE_DISPOSITIVO.miMacAddress);
        boolean otro_destino =  mensaje.getMacDestino().equals(OTRO_DISPOSITIVO.MacAddress);

        boolean mio_destino = mensaje.getMacDestino().equals(ESTE_DISPOSITIVO.miMacAddress);
        boolean otro_origen =  mensaje.getMacOrigen().equals(OTRO_DISPOSITIVO.MacAddress);

        boolean origen_vacio = mensaje.getMacOrigen().equals("");
        boolean destino_vacio = mensaje.getMacDestino().equals("");

        if((mio_origen && otro_destino) || (mio_destino && otro_origen) || (origen_vacio && mio_destino) || (destino_vacio && mio_origen)){
            respuesta = true;
        }

        return respuesta;
    }
}
