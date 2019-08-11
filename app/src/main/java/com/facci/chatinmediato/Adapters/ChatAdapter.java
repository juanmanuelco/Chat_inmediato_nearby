package com.facci.chatinmediato.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.ThumbnailUtils;
import android.provider.MediaStore.Images.Thumbnails;
import android.text.util.Linkify;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facci.chatinmediato.Cache.CacheView;
import com.facci.chatinmediato.ChatActivity;
import com.facci.chatinmediato.Entities.Mensaje;
import com.facci.chatinmediato.NEGOCIO.ESTE_DISPOSITIVO;
import com.facci.chatinmediato.NEGOCIO.OTRO_DISPOSITIVO;
import com.facci.chatinmediato.PlayVideoActivity;
import com.facci.chatinmediato.R;
import com.facci.chatinmediato.ViewImageActivity;
import com.facci.chatinmediato.util.FileUtilities;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static com.facci.chatinmediato.NEGOCIO.Mensajes.getMacAddr;

public class ChatAdapter extends BaseAdapter {
	private Activity activity;
	private List<Mensaje> listMensaje;
	private LayoutInflater inflater;
	public static Bitmap bitmap;
	private Context mContext;
	private HashMap<String,Bitmap> mapThumb;





	public ChatAdapter(Context context, List<Mensaje> listMensaje){
		this.listMensaje = listMensaje;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mContext = context;
		mapThumb = new HashMap<String, Bitmap>();
	}
	
	@Override
	public int getCount() {
		return listMensaje.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		int layoutResource = 0; // determined by view type
		Mensaje mes = listMensaje.get(position);
		int type = mes.getTipo();
		if(view == null){
			CacheView cache = new CacheView();
			view = inflater.inflate(R.layout.chat_row,parent, false);
			cache.chatName = (TextView) view.findViewById(R.id.chatName);
            cache.text = (TextView) view.findViewById(R.id.text);
            cache.image = (ImageView) view.findViewById(R.id.image);
            cache.relativeLayout = (RelativeLayout) view.findViewById(R.id.relativeLayout);
            cache.audioPlayer = (ImageView) view.findViewById(R.id.playAudio);
            cache.videoPlayer = (ImageView) view.findViewById(R.id.playVideo);
            cache.fileSaved = (TextView) view.findViewById(R.id.fileSaved);
            cache.videoPlayerButton = (ImageView) view.findViewById(R.id.buttonPlayVideo);
            cache.fileSavedIcon = (ImageView) view.findViewById(R.id.file_attached_icon);

			view.setTag(cache);
		}

		//Retrive the items from cache
        CacheView cache = (CacheView) view.getTag();
        cache.chatName.setText(listMensaje.get(position).getChatName());
        cache.chatName.setTag(cache);
        cache.chatName.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				CacheView cache = (CacheView) v.getTag();
				((ChatActivity)mContext).talkTo((String) cache.chatName.getText());
				return true;
			}
		});
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)cache.relativeLayout.getLayoutParams();
        String mensaje=mes.getTexto();

		if(mes.getMacOrigen().equals(getMacAddr()) || mes.getMacOrigen().equals(ESTE_DISPOSITIVO.miMacAddress)){
        	params.removeRule(RelativeLayout.ALIGN_PARENT_LEFT);
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			cache.relativeLayout.setBackground(view.getResources().getDrawable(R.drawable.chat_bubble_mine));
			cache.chatName.setTextColor(Color.BLACK);
			cache.text.setTextColor(Color.BLACK);
			cache.chatName.setText("Yo");
			if(mes.getIdentificacion()) mes.setMacDestino(OTRO_DISPOSITIVO.MacAddress);
		}
        else{
			params.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			cache.relativeLayout.setBackground(view.getResources().getDrawable(R.drawable.chat_bubble));
			cache.chatName.setTextColor(Color.WHITE);
			cache.text.setTextColor(Color.WHITE);
			OTRO_DISPOSITIVO.MacAddress=mes.getMacOrigen();
        }

        //We disable all the views and enable certain views depending on the message's type
        disableAllMediaViews(cache);



        /***********************************************
          				Text Mensaje
         ***********************************************/

        if(type == Mensaje.TEXT_MESSAGE){
        	enableTextView(cache, mensaje);
		}

        /***********************************************
			            Image Mensaje
         ***********************************************/

		else if(type == Mensaje.IMAGE_MESSAGE){
			enableTextView(cache, mensaje);
			cache.image.setVisibility(View.VISIBLE);

			if(!mapThumb.containsKey(mes.getNombreArchivo())){
				Bitmap thumb = mes.byteArrayToBitmap(mes.getByteArray());
				mapThumb.put(mes.getNombreArchivo(), thumb);
			}
			cache.image.setImageBitmap(mapThumb.get(mes.getNombreArchivo()));
			cache.image.setTag(position);

			cache.image.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Mensaje mes = listMensaje.get((Integer) v.getTag());
					bitmap = mes.byteArrayToBitmap(mes.getByteArray());

					Intent intent = new Intent(mContext, ViewImageActivity.class);
					String fileName = mes.getNombreArchivo();
					intent.putExtra("fileName", fileName);

					mContext.startActivity(intent);
				}
			});
		}

        /***********************************************
        				Audio Mensaje
         ***********************************************/
		else if(type == Mensaje.AUDIO_MESSAGE){
			enableTextView(cache, mensaje);
			cache.audioPlayer.setVisibility(View.VISIBLE);
			cache.audioPlayer.setTag(position);
			cache.audioPlayer.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(final View v) {
					MediaPlayer mPlayer = new MediaPlayer();
					Mensaje mes = listMensaje.get((Integer) v.getTag());
			        try {
			            mPlayer.setDataSource(mes.getPathArchivo());
			            mPlayer.prepare();
			            mPlayer.start();

			            //Disable the button when the audio is playing
			            v.setEnabled(false);
			            ((ImageView)v).setImageDrawable(mContext.getResources().getDrawable(R.drawable.play_audio_in_progress));

			            mPlayer.setOnCompletionListener(new OnCompletionListener() {

							@Override
							public void onCompletion(MediaPlayer mp) {
								//Re-enable the button when the audio has finished playing
								v.setEnabled(true);
								((ImageView)v).setImageDrawable(mContext.getResources().getDrawable(R.drawable.play_audio));
							}
						});
			        } catch (IOException e) {
			            e.printStackTrace();
			        }

				}
			});
		}

        /***********************************************
        				Video Mensaje
         ***********************************************/
		else if(type == Mensaje.VIDEO_MESSAGE){
			enableTextView(cache, mensaje);
			cache.videoPlayer.setVisibility(View.VISIBLE);
			cache.videoPlayerButton.setVisibility(View.VISIBLE);

			if(!mapThumb.containsKey(mes.getPathArchivo())){
				Bitmap thumb = ThumbnailUtils.createVideoThumbnail(mes.getPathArchivo(), Thumbnails.MINI_KIND);
				mapThumb.put(mes.getPathArchivo(), thumb);
			}
			cache.videoPlayer.setImageBitmap(mapThumb.get(mes.getPathArchivo()));

			cache.videoPlayerButton.setTag(position);
			cache.videoPlayerButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Mensaje mes = listMensaje.get((Integer) v.getTag());
					Intent intent = new Intent(mContext, PlayVideoActivity.class);
					intent.putExtra("filePath", mes.getPathArchivo());
					mContext.startActivity(intent);
				}
			});
		}

        /***********************************************
						File Mensaje
         ***********************************************/
		else if(type == Mensaje.FILE_MESSAGE){
			enableTextView(cache, mensaje);
			cache.fileSavedIcon.setVisibility(View.VISIBLE);
			cache.fileSaved.setVisibility(View.VISIBLE);
			cache.fileSaved.setText(mes.getNombreArchivo());
		}

        /***********************************************
					Drawing Mensaje
		***********************************************/
		else if(type == Mensaje.DRAWING_MESSAGE){
			enableTextView(cache, mensaje);
			cache.image.setVisibility(View.VISIBLE);

			if(!mapThumb.containsKey(mes.getNombreArchivo())){
				Bitmap thumb = FileUtilities.getBitmapFromFile(mes.getPathArchivo());
				mapThumb.put(mes.getNombreArchivo(), thumb);
			}
			cache.image.setImageBitmap(mapThumb.get(mes.getNombreArchivo()));
			cache.image.setTag(position);

			cache.image.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Mensaje mes = listMensaje.get((Integer) v.getTag());
					bitmap = mes.byteArrayToBitmap(mes.getByteArray());

					Intent intent = new Intent(mContext, ViewImageActivity.class);
					String fileName = mes.getNombreArchivo();
					intent.putExtra("fileName", fileName);

					mContext.startActivity(intent);
				}
			});
		}
		return view;
	}
	
	private void disableAllMediaViews(CacheView cache){
		cache.text.setVisibility(View.GONE);
		cache.image.setVisibility(View.GONE);
		cache.audioPlayer.setVisibility(View.GONE);
		cache.videoPlayer.setVisibility(View.GONE);
		cache.fileSaved.setVisibility(View.GONE);
		cache.videoPlayerButton.setVisibility(View.GONE);
		cache.fileSavedIcon.setVisibility(View.GONE);
	}
	
	private void enableTextView(CacheView cache, String text){
		if(!text.equals("")){
			cache.text.setVisibility(View.VISIBLE);
			cache.text.setText(text);
			Linkify.addLinks(cache.text, Linkify.PHONE_NUMBERS);
			Linkify.addLinks(cache.text, Patterns.WEB_URL, "myweburl:");
		}		
	}

}
