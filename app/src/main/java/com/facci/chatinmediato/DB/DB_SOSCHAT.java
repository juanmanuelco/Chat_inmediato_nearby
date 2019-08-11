package com.facci.chatinmediato.DB;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.facci.chatinmediato.DB.Tablas.TB_mensajes;
import com.facci.chatinmediato.DB.Tablas.TB_usuarios;
import com.facci.chatinmediato.Entities.Mensaje;
import com.facci.chatinmediato.Entities.Usuario;
import com.facci.chatinmediato.NEGOCIO.OTRO_DISPOSITIVO;
import com.facci.chatinmediato.R;

import java.util.ArrayList;
import java.util.List;

import static com.facci.chatinmediato.NEGOCIO.Mensajes.getMacAddr;

public class DB_SOSCHAT extends SQLiteOpenHelper {

    public static final String DB_NOMBRE = "DB_SOSCHAT.db";
    Context ct;
    public DB_SOSCHAT(Context context) {
        super(context, DB_NOMBRE, null, 1);
        SQLiteDatabase db = this.getWritableDatabase();
        this.ct = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TB_mensajes.CrearTablaMensaje());
        db.execSQL(TB_usuarios.CrearTablaUsuario());
        db.execSQL(String.format("INSERT INTO USUARIOS VALUES ( NULL,'3d:12:12:12:12','Dispositivo1', 'true')"));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(TB_mensajes.EliminarTabla());
        db.execSQL(TB_usuarios.EliminarTabla());
        onCreate(db);
    }

    public void guardarRegistro(Object obj) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "";
        if (obj.getClass() == Mensaje.class) query = TB_mensajes.Guardar((Mensaje) obj,ct);
        if (obj.getClass() == Usuario.class) query = TB_usuarios.Guardar((Usuario) obj);
        if(!query.equals("0")){db.execSQL(query); }
    }

    public Boolean validarRegistro(Mensaje mes) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor registro = db.rawQuery("SELECT * FROM MENSAJES_SOSCHAT", null);
        return TB_mensajes.mensaje_guardado(registro, mes);
    }

    public void actualizarDestino(long id, String mac, long ti_recibo){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE MENSAJES_SOSCHAT  SET MAC_DESTINO = '"+mac+"' WHERE TIEMPO_ENVIO = "+ id + " AND MAC_DESTINO = "+ "''");
        db.execSQL("UPDATE MENSAJES_SOSCHAT SET TIEMPO_RECIBO = "+ti_recibo+" WHERE TIEMPO_ENVIO = "+ id + " AND TIEMPO_RECIBO = 0");
    }

    public List<Mensaje> todos_mensajes(){
        List<Mensaje> respuesta = new ArrayList<Mensaje>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM MENSAJES_SOSCHAT", null);
        return TB_mensajes.todos(cursor);
    }

    public List<Mensaje> Mensajes_filtro(String mac_origen, String mac_destino){
        String mac_destino_recortada = mac_destino.substring(3);
        String mac_origen_recortada = mac_origen.substring(3);
        Log.i("Macs","Origen: "+mac_origen_recortada+" Destino: "+mac_destino_recortada);
        List<Mensaje> respuesta = new ArrayList<Mensaje>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor= db.rawQuery("select * from MENSAJES_SOSCHAT where (MAC_ORIGEN like '%"+mac_origen_recortada+"' AND MAC_DESTINO like '%"+mac_destino_recortada+"')OR(MAC_DESTINO like '%"+mac_origen_recortada+"' AND MAC_ORIGEN like '%"+mac_destino_recortada+"')",null);
        return TB_mensajes.todos(cursor);
    }

    public ArrayList<String> buscador(){
        ArrayList<String> respuesta= new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            Cursor res = db.rawQuery(String.format("select * from USUARIOS"),null);
            while (res.moveToNext()) {
                if (Boolean.parseBoolean(res.getString(3)))
                    respuesta.add(res.getString(2)+","+res.getString(1));
            }
        }catch (SQLException e){
            Log.i("error",e.toString());
        }
        return respuesta;
    }

    public ArrayList<String[]> listaEncontrados(){
        ArrayList<String[]> respuesta= new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            Cursor res = db.rawQuery(String.format("SELECT * FROM USUARIOS"),null);
            while (res.moveToNext()) {
                if (Boolean.parseBoolean(res.getString(3)))
                    respuesta.add(new String[]{res.getString(2), res.getString(1)});
            }
        }catch (SQLException e){
            Log.i("error",e.toString());
        }
        return respuesta;
    }

    public Boolean validarAgregado(String mac){
        Boolean respuesta= false;
        SQLiteDatabase db = this.getWritableDatabase();
        SQLiteStatement s = db.compileStatement( "SELECT (USER_ESTADO) from USUARIOS WHERE USER_MAC = '"+mac+"'" );
        try{
            Boolean data=Boolean.parseBoolean(s.simpleQueryForString());
            if(data) respuesta=true;
        } catch(Exception exp){
            Log.i("ValidarAgregado", "error "+exp);
        }
        return respuesta;
    }

    public void insertarUsuario(String Mac, String Nombre){
        SQLiteDatabase db = this.getWritableDatabase();
        SQLiteStatement s = db.compileStatement( "SELECT COUNT (USER_MAC) from USUARIOS WHERE USER_MAC = '"+Mac+"'" );
        long count = s.simpleQueryForLong();
        if(count <1) db.execSQL(String.format("INSERT INTO USUARIOS VALUES ( NULL,'%s', '%s', 'false')", Mac, Nombre));
    }

    public int ActualizarUsuario(String Mac, Boolean estado){
        int respuesta=0;
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(String.format("UPDATE USUARIOS SET USER_ESTADO = '%s' WHERE USER_MAC= '%s'", estado, Mac ));
        if(estado){ respuesta=R.string.ADD;}
        else{ respuesta=R.string.NADD;}
        return respuesta;
    }

    public ArrayList<String[]> mensajesRecibidos(){
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<String[]> respuesta= new ArrayList<>();
        String mac_recortada;
        String mac = getMacAddr();
        mac_recortada = "%"+mac.substring(3);
        Cursor obtenidos =db.rawQuery(String.format(
                "SELECT * FROM %s WHERE (%s IN (SELECT MAX (%s) FROM %s GROUP BY %s ) ) AND (%s like '%s') ORDER BY %s DESC",
                TB_mensajes.nombre,
                TB_mensajes.tiempoEnvio,
                TB_mensajes.tiempoEnvio,
                TB_mensajes.nombre,
                TB_mensajes.macOrigen,
                TB_mensajes.macDestino,
                mac_recortada,
                TB_mensajes.tiempoEnvio), null);
        while (obtenidos.moveToNext()){
            respuesta.add(new String[]{ obtenidos.getString(3),obtenidos.getString(2), obtenidos.getLong(11)+"",});
        }
        return respuesta;
    }

    public void RetornarMacMensaje(String Texto, String Nombre){
        Log.i("RetornarMac",Texto +" "+Nombre);
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<String[]> respuesta= new ArrayList<>();
        Cursor cursor = db.rawQuery(String.format("select MAC_ORIGEN, MAC_DESTINO from MENSAJES_SOSCHAT where TEXTO='"+Texto+"' AND CHATNAME='"+Nombre+"'"),null);
        while (cursor.moveToNext()){
            if (!getMacAddr().equals(cursor.getString(0))){
                OTRO_DISPOSITIVO.MacOnclic= cursor.getString(0);
                Log.i("Retorno",OTRO_DISPOSITIVO.MacOnclic);
            }else if (!getMacAddr().equals(cursor.getString(1))){
                OTRO_DISPOSITIVO.MacOnclic= cursor.getString(1);
                Log.i("Retorno",OTRO_DISPOSITIVO.MacOnclic);
            }
        }
    }

    public ArrayList<String> buscador_mensaje(){
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<String> respuesta= new ArrayList<>();
        try {
            Cursor res =db.rawQuery(String.format(
                    "SELECT * FROM %s WHERE (%s IN (SELECT MAX (%s) FROM %s GROUP BY %s ) ) AND (%s = '%s') ORDER BY %s DESC",
                    TB_mensajes.nombre,
                    TB_mensajes.tiempoEnvio,
                    TB_mensajes.tiempoEnvio,
                    TB_mensajes.nombre,
                    TB_mensajes.macOrigen,
                    TB_mensajes.macDestino,
                    getMacAddr(),
                    TB_mensajes.tiempoEnvio), null);
            while (res.moveToNext()) {
                respuesta.add(res.getString(3)+","+res.getString(2)+","+res.getString(11));
            }
        }catch (SQLException e){
            Log.i("error",e.toString());
        }
        return respuesta;
    }
}