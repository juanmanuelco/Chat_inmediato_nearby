package com.facci.chatinmediato.DB.Tablas;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.facci.chatinmediato.DB.DB_SOSCHAT;
import com.facci.chatinmediato.Entities.Mensaje;
import com.facci.chatinmediato.Entities.Usuario;
import com.facci.chatinmediato.NEGOCIO.ESTE_DISPOSITIVO;

import java.util.List;

public class TB_usuarios {
    static DB_SOSCHAT db = ESTE_DISPOSITIVO.db; ;
    public static final String NOMBRE   = " USUARIOS ";
    public static String ID             = " ID INTEGER PRIMARY KEY AUTOINCREMENT ";
    public static String USER_MAC       = " USER_MAC TEXT ";
    public static String USER_NAME      = " USER_NAME TEXT ";
    public static String USER_ESTADO    = " USER_ESTADO NUMERIC ";

    public static final String nombre   = " USUARIOS ";
    public static String id_            = " ID ";
    public static String userMac        = " USER_MAC ";
    public static String userName       = " USER_NAME ";
    public static String userEstado     = " USER_ESTADO ";

    public static String CrearTablaUsuario(){
        return  String.format("CREATE TABLE %s (%s,%s,%s,%s)", NOMBRE, ID, USER_MAC, USER_NAME, USER_ESTADO);
    }
    public static String EliminarTabla(){
        return String.format("DROP TABLE IF EXISTS %s",NOMBRE);
    }
    public static String Guardar(Usuario us){
        return "";
    }
    public static List<Usuario> todos(){
        return null;
    }
}


