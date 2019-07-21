package com.facci.chatinmediato.Entities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.net.InetAddress;

@SuppressWarnings("serial")
public class Mensaje implements Serializable{
	/**Tipos de envío*/
	public static final int TEXT_MESSAGE    = 1;
	public static final int IMAGE_MESSAGE   = 2;
	public static final int VIDEO_MESSAGE   = 3;
	public static final int AUDIO_MESSAGE   = 4;
	public static final int FILE_MESSAGE    = 5;
	public static final int DRAWING_MESSAGE = 6;
	public static final int FOTO_MESSAGE = 7;

	/**Variables del sistema*/
	private int mType;
	private String mText;
	private String chatName;
	private byte[] byteArray;
	private String senderAddress;
	private String fileName;
	private long fileSize;
	private String filePath;
	private String macOrigen="";
	private String macDestino="";
	private long tiempoEnvio=0;
	private long tiempoRecibo=0;
	private boolean identificacion=true;
	private long latitud =0;
	private long longitud =0;




	/**Obtener elementos del sistema*/
	public int getTipo(){return mType;}
	public String getTexto(){return mText;}
	public String getChatName(){return chatName;}
	public String getMacOrigen(){return macOrigen;}
	public byte[] getByteArray(){return byteArray;}
	public long getTamanoArchivo(){return fileSize;}
	public String getPathArchivo(){return filePath;}
	public String getMacDestino(){return macDestino;}
	public long getTiempoEnvio(){return tiempoEnvio;}
	public String getNombreArchivo(){return fileName;}
	public long getTiempoRecibo(){return tiempoRecibo;}
	public String getAddress(){return senderAddress;}
	public Boolean getIdentificacion(){return identificacion;}
	public long getLatitud(){return latitud;}
	public long getLongitud(){return longitud;}

	/**Sitiar elemtos del sistema*/
	public void setTipo(int tipo){this.mType=tipo;}
	public void setTexto(String texto){this.mText=texto;}
	public void setMacOrigen(String mac){this.macOrigen=mac;}
	public void setMacDestino(String mac){this.macDestino=mac;}
	public void setTamanoArchivo(long tam ){this.fileSize=tam;}
	public void setPathArchivo(String path){this.filePath=path;}
	public void setNombreArchivo(String nombe){this.fileName=nombe;}
	public void setChatName(String chatName){this.chatName=chatName;}
	public void setTiempoEnvio(long tiempo){this.tiempoEnvio=tiempo;}
	public void setTiempoRecibo(long tiempo){this.tiempoRecibo=tiempo;}
	public void setByteArray(byte[] byteArray){this.byteArray=byteArray;}
	public void setAddress(String direccion){this.senderAddress=direccion;}
    public void setIdentificacion(Boolean identificacion){this.identificacion=identificacion;}
    public void setLatitud(long latitud){this.latitud = latitud;}
    public void setLongitud(long longitud){this.longitud = longitud;}


	public Mensaje(int type, String text, String sender, String name){
		mType = type;
		mText = text;
		senderAddress = sender;
		chatName = name;
	}

	public Bitmap byteArrayToBitmap(byte[] b){
		return BitmapFactory.decodeByteArray(b, 0, b.length);
	}
	
	public void saveByteArrayToFile(Context context){
		switch(mType){
			case Mensaje.AUDIO_MESSAGE:
				filePath = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC).getAbsolutePath()+"/"+fileName;
				break;
			case Mensaje.VIDEO_MESSAGE:
				filePath = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES).getAbsolutePath()+"/"+fileName;
				break;
			case Mensaje.FILE_MESSAGE:
				filePath = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()+"/"+fileName;
				break;
			case Mensaje.DRAWING_MESSAGE:
				filePath = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath()+"/"+fileName;
				break;
		}
		File file = new File(filePath);

		if (file.exists()) file.delete();
		try {
			FileOutputStream fos=new FileOutputStream(file.getPath());
			fos.write(byteArray);
			fos.close();
		}
		catch (java.io.IOException e) {
			e.printStackTrace();
		}
	}
}
