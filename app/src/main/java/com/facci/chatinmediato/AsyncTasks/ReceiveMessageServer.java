package com.facci.chatinmediato.AsyncTasks;

import android.content.Context;

import com.facci.chatinmediato.DB.DB_SOSCHAT;
import com.facci.chatinmediato.Entities.Mensaje;
import com.facci.chatinmediato.NEGOCIO.OTRO_DISPOSITIVO;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import static com.facci.chatinmediato.NEGOCIO.Mensajes.getMacAddr;


public class ReceiveMessageServer extends AbstractReceiver {
	private static final int SERVER_PORT = 4445;
	private Context mContext;
	private ServerSocket serverSocket;
	DB_SOSCHAT db;

	public boolean seDisemina;


	public ReceiveMessageServer(Context context){
		mContext = context;
		this.db = new DB_SOSCHAT(context);
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		try {
			serverSocket = new ServerSocket(SERVER_PORT);
			while(true){
				Socket clientSocket = serverSocket.accept();				
				
				InputStream inputStream = clientSocket.getInputStream();				
				ObjectInputStream objectIS = new ObjectInputStream(inputStream);
				Mensaje mensaje = (Mensaje) objectIS.readObject();
				InetAddress senderAddr = clientSocket.getInetAddress();
				mensaje.setAddress(senderAddr.toString());

				seDisemina = mensaje.diseminacion();

				if(mensaje.getTiempoRecibo()==0)
					mensaje.setTiempoRecibo(System.currentTimeMillis());
				if(mensaje.getMacDestino().equals(""))
					mensaje.setMacDestino(getMacAddr());
				if(db.validarRegistro(mensaje))db.guardarRegistro(mensaje);
				else db.actualizarDestino(mensaje.getTiempoEnvio(), OTRO_DISPOSITIVO.MacAddress, mensaje.getTiempoRecibo());

				clientSocket.close();
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
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		super.onCancelled();
	}

	@Override
	protected void onProgressUpdate(Mensaje... values) {
		super.onProgressUpdate(values);
		playNotification(mContext, values[0]);
		
		//If the message contains a video or an audio, we saved this file to the external storage
		int type = values[0].getTipo();
		if(type==Mensaje.AUDIO_MESSAGE || type==Mensaje.VIDEO_MESSAGE || type==Mensaje.FILE_MESSAGE || type==Mensaje.DRAWING_MESSAGE){
			values[0].saveByteArrayToFile(mContext);
		}
		
		new SendMessageServer(mContext, seDisemina).executeOnExecutor(THREAD_POOL_EXECUTOR, values);
	}
	
}
