package com.facci.chatinmediato.DB.Tablas;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.facci.chatinmediato.DB.DB_SOSCHAT;
import com.facci.chatinmediato.Entities.Mensaje;
import com.facci.chatinmediato.NEGOCIO.ESTE_DISPOSITIVO;
import com.facci.chatinmediato.NEGOCIO.Listeners;
import com.facci.chatinmediato.NEGOCIO.Validaciones;

import java.util.ArrayList;
import java.util.List;

import static com.facci.chatinmediato.NEGOCIO.Mensajes.getMacAddr;
import static com.facci.chatinmediato.NEGOCIO.Validaciones.Formateados;
import static com.facci.chatinmediato.NEGOCIO.Validaciones.FormateadosR;
import static com.facci.chatinmediato.NEGOCIO.Validaciones.obtenerInetAddress;

public class TB_mensajes {
    static DB_SOSCHAT db = ESTE_DISPOSITIVO.db; ;
    public static final String NOMBRE   = " MENSAJES_SOSCHAT ";
    public static String ID             = " ID INTEGER PRIMARY KEY AUTOINCREMENT ";
    public static String TIPO           = " TIPO INTEGER ";
    public static String TEXTO          = " TEXTO TEXT ";
    public static String CHATNAME       = " CHATNAME TEXT ";
    public static String BYTEARRAY      = " BYTEARRAY BLOB ";
    public static String DIRECCION      = " DIRECCION TEXT ";
    public static String NOMBRE_ARCHIVO = " NOMBRE_ARCHIVO TEXT ";
    public static String TAMANO_ARCHIVO = " TAMANO_ARCHIVO NUMERIC";
    public static String PATH_ARCHIVO   = " PATH_ARCHIVO TEXT";
    public static String MAC_ORIGEN     = " MAC_ORIGEN TEXT";
    public static String MAC_DESTINO    = " MAC_DESTINO TEXT";
    public static String TIEMPO_ENVIO   = " TIEMPO_ENVIO NUMERIC";
    public static String TIEMPO_RECIBO  = " TIEMPO_RECIBO NUMERIC";
    public static String LATITUD        = " LATITUD NUMERIC";
    public static String LONGITUD       = " LONGITUD NUMERIC";


    public static final String nombre   = " MENSAJES_SOSCHAT ";
    public static String id_             = " ID  ";
    public static String tipo           = " TIPO  ";
    public static String texto          = " TEXTO  ";
    public static String chatname       = " CHATNAME  ";
    public static String byteArray      = " BYTEARRAY  ";
    public static String direccion      = " DIRECCION  ";
    public static String nombreArchivo  = " NOMBRE_ARCHIVO  ";
    public static String tamanoArchivo  = " TAMANO_ARCHIVO ";
    public static String pathArchivo    = " PATH_ARCHIVO ";
    public static String macOrigen      = " MAC_ORIGEN ";
    public static String macDestino     = " MAC_DESTINO ";
    public static String tiempoEnvio    = " TIEMPO_ENVIO ";
    public static String tiempoRecibo   = " TIEMPO_RECIBO ";
    public static String latitud        = " LATITUD ";
    public static String longitud       = " LONGITUD ";

    public static String CrearTablaMensaje(){
        return String.format("CREATE TABLE %s ("+Formateados(15)+")",
                NOMBRE, ID, TIPO, TEXTO, CHATNAME, BYTEARRAY, DIRECCION, NOMBRE_ARCHIVO,
                TAMANO_ARCHIVO, PATH_ARCHIVO, MAC_ORIGEN, MAC_DESTINO, TIEMPO_ENVIO, TIEMPO_RECIBO, LATITUD, LONGITUD);
    }
    public static String EliminarTabla(){
        return String.format("DROP TABLE IF EXISTS %s",NOMBRE);
    }

    public static String Guardar(Mensaje m, Context context){
        try{
            db = new DB_SOSCHAT(context);
            if(db.validarRegistro(m)){
                return String.format("INSERT INTO %s VALUES (NULL, "+FormateadosR(14)+")",
                        NOMBRE,
                        m.getTipo(),
                        m.getTexto(),
                        m.getChatName(),
                        m.getByteArray(),
                        m.getAddress(),
                        m.getNombreArchivo(),
                        m.getTamanoArchivo(),
                        m.getPathArchivo(),
                        m.getMacOrigen(),
                        m.getMacDestino(),
                        m.getTiempoEnvio(),
                        m.getTiempoRecibo(),
                        m.getLatitud(),
                        m.getLongitud()
                );
            }
            else{
                return "0";
            }
        }catch (Exception e){
            return "Errores "+e;
        }
    }
    public static boolean mensaje_guardado(Cursor registro, Mensaje mes){
        Boolean respuesta = true;
        ArrayList<Long> keys = new ArrayList<Long>();
        while (registro.moveToNext()) {
            keys.add(registro.getLong(11));
        }
        if (keys.contains(mes.getTiempoEnvio())) respuesta = false;
        return respuesta;
    }

    public static List<Mensaje> todos(Cursor obtenidos) {
        List<Mensaje> respuesta = new ArrayList<Mensaje>();
        while (obtenidos.moveToNext()) {
            Mensaje mensaje= new Mensaje(
                    obtenidos.getInt(1),
                    obtenidos.getString(2),
                    obtenidos.getString(5),
                    obtenidos.getString(3)
            );
            mensaje.setByteArray(obtenidos.getBlob(4));
            mensaje.setNombreArchivo(obtenidos.getString(6));
            mensaje.setTamanoArchivo(obtenidos.getLong(7));
            mensaje.setPathArchivo(obtenidos.getString(8));
            mensaje.setMacOrigen(obtenidos.getString(9));
            mensaje.setMacDestino(obtenidos.getString(10));
            mensaje.setTiempoEnvio(obtenidos.getLong(11));
            mensaje.setTiempoRecibo(obtenidos.getLong(12));
            mensaje.setLatitud(obtenidos.getLong(13));
            mensaje.setLongitud(obtenidos.getLong(13));
            respuesta.add(mensaje);
        }
        return  respuesta;
    }
}
