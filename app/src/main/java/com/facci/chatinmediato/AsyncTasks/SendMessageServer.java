package com.facci.chatinmediato.AsyncTasks;

import android.content.Context;
import android.os.AsyncTask;

import com.facci.chatinmediato.ChatActivity;
import com.facci.chatinmediato.Entities.Mensaje;
import com.facci.chatinmediato.InicioActivity;
import com.facci.chatinmediato.InitThreads.ServerInit;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

import static com.facci.chatinmediato.NEGOCIO.Validaciones.isActivityRunning;


public class SendMessageServer extends AsyncTask<Mensaje, Mensaje, Mensaje>{

	private Context mContext;
	private static final int SERVER_PORT = 4446;
	private boolean isMine;
	private boolean diseminado;

	public SendMessageServer(Context context, boolean diseminado){
		mContext = context;
		this.diseminado = diseminado;
	}
	
	@Override
	protected Mensaje doInBackground(Mensaje... msg) {
		publishProgress(msg);
		try {			
			ArrayList<InetAddress> listClients = ServerInit.clients;
			for(InetAddress addr : listClients){

				Socket socket = new Socket();
				socket.setReuseAddress(true);
				socket.bind(null);
				socket.connect(new InetSocketAddress(addr, SERVER_PORT));
				OutputStream outputStream = socket.getOutputStream();
				new ObjectOutputStream(outputStream).writeObject(msg[0]);
			    socket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();

		}
		return msg[0];
	}

	@Override
	protected void onProgressUpdate(Mensaje... values) {
		super.onProgressUpdate(values);
		if(isActivityRunning(InicioActivity.class, mContext))
			ChatActivity.refreshList(values[0], diseminado);
	}

	@Override
	protected void onPostExecute(Mensaje result) {

		super.onPostExecute(result);
	}

}