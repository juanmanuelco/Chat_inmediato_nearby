package com.facci.chatinmediato.AsyncTasks;

import android.content.Context;

import com.facci.chatinmediato.ChatActivity;
import com.facci.chatinmediato.DB.DB_SOSCHAT;
import com.facci.chatinmediato.Entities.Mensaje;
import com.facci.chatinmediato.InicioActivity;
import com.facci.chatinmediato.NEGOCIO.OTRO_DISPOSITIVO;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import static com.facci.chatinmediato.NEGOCIO.Mensajes.getMacAddr;
import static com.facci.chatinmediato.NEGOCIO.Validaciones.isActivityRunning;

public class ReceiveMessageClient extends AbstractReceiver {
	private static final int SERVER_PORT = 4446;
	private Context mContext;
	private ServerSocket socket;
	DB_SOSCHAT db;

	public boolean seDisemina;

	public ReceiveMessageClient(Context context){
		mContext = context;
		this.db = new DB_SOSCHAT(context);
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		try {
			socket = new ServerSocket(SERVER_PORT);
			while(true){
				Socket destinationSocket = socket.accept();
				InputStream inputStream = destinationSocket.getInputStream();
				BufferedInputStream buffer = new BufferedInputStream(inputStream);
				ObjectInputStream objectIS = new ObjectInputStream(buffer);
				Mensaje mensaje = (Mensaje) objectIS.readObject();

				seDisemina = mensaje.diseminacion();

				if(mensaje.getTiempoRecibo()==0)
					mensaje.setTiempoRecibo(System.currentTimeMillis());
				if(mensaje.getMacDestino().equals(""))
					mensaje.setMacDestino(getMacAddr());

				if(db.validarRegistro(mensaje))db.guardarRegistro(mensaje);
				else db.actualizarDestino(mensaje.getTiempoEnvio(), OTRO_DISPOSITIVO.MacAddress, mensaje.getTiempoRecibo());


				destinationSocket.close();
				publishProgress(mensaje);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
        
		return null;
	}

	@Override
	protected void onCancelled() {
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		super.onCancelled();
	}

	@Override
	protected void onProgressUpdate(Mensaje... values) {
		super.onProgressUpdate(values);
		playNotification(mContext, values[0]);
		int type = values[0].getTipo();
		if(type==Mensaje.AUDIO_MESSAGE || type==Mensaje.VIDEO_MESSAGE || type==Mensaje.FILE_MESSAGE || type==Mensaje.DRAWING_MESSAGE){
			values[0].saveByteArrayToFile(mContext);
		}
		if(isActivityRunning(InicioActivity.class,mContext))
			ChatActivity.refreshList(values[0], seDisemina);
	}


}
