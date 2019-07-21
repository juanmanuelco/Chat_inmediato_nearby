package com.facci.chatinmediato.Servicios;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import com.facci.chatinmediato.AsyncTasks.ReceiveMessageClient;
import com.facci.chatinmediato.AsyncTasks.ReceiveMessageServer;
import com.facci.chatinmediato.Receivers.WifiDirectBroadcastReceiver;


public class MessageService extends Service {
	private static final String TAG = "MessageService";
	WifiManager wifiManager;
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		WifiDirectBroadcastReceiver mReceiver = WifiDirectBroadcastReceiver.createInstance();
		wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

		if(!wifiManager.isWifiEnabled())
			wifiManager.setWifiEnabled(true);

		//Start the AsyncTask for the server to receive messages
        if(mReceiver.isGroupeOwner() == WifiDirectBroadcastReceiver.IS_OWNER){
        	Log.v(TAG, "Start the AsyncTask for the server to receive messages");
        	new ReceiveMessageServer(getApplicationContext()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[])null);
        }
        else if(mReceiver.isGroupeOwner() == WifiDirectBroadcastReceiver.IS_CLIENT){
        	Log.v(TAG, "Start the AsyncTask for the client to receive messages");
        	new ReceiveMessageClient(getApplicationContext()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[])null);
        }
		return START_STICKY;
	}
}
