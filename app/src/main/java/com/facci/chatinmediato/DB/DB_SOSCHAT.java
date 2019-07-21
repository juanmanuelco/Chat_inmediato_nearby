package com.facci.chatinmediato.DB;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import com.facci.chatinmediato.DB.Tablas.TB_mensajes;
import com.facci.chatinmediato.DB.Tablas.TB_usuarios;
import com.facci.chatinmediato.Entities.Mensaje;
import com.facci.chatinmediato.Entities.Usuario;
import com.facci.chatinmediato.R;

import java.util.ArrayList;
import java.util.List;

public class DB_SOSCHAT extends SQLiteOpenHelper {

    public static final String DB_NOMBRE = "DB_SOSCHAT_V2.db";

    public DB_SOSCHAT(Context context) {
        super(context, DB_NOMBRE, null, 1);
        SQLiteDatabase db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TB_mensajes.CrearTablaMensaje());
        db.execSQL(TB_usuarios.CrearTablaUsuario());
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
        if (obj.getClass() == Mensaje.class) query = TB_mensajes.Guardar((Mensaje) obj);
        if (obj.getClass() == Usuario.class) query = TB_usuarios.Guardar((Usuario) obj);
        if(!query.equals("0")){db.execSQL(query);}
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
}