package com.facci.chatinmediato;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.facci.chatinmediato.Adapters.ChatAdapter;
import com.facci.chatinmediato.AsyncTasks.SendMessageClient;
import com.facci.chatinmediato.AsyncTasks.SendMessageServer;
import com.facci.chatinmediato.DB.DB_SOSCHAT;
import com.facci.chatinmediato.Entities.Image;
import com.facci.chatinmediato.Entities.MediaFile;
import com.facci.chatinmediato.Entities.Mensaje;
import com.facci.chatinmediato.NEGOCIO.ESTE_DISPOSITIVO;
import com.facci.chatinmediato.NEGOCIO.OTRO_DISPOSITIVO;
import com.facci.chatinmediato.Receivers.WifiDirectBroadcastReceiver;
import com.facci.chatinmediato.util.FileUtilities;

import org.apache.commons.lang3.SerializationUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.facci.chatinmediato.NEGOCIO.Mensajes.getMacAddr;
import static com.facci.chatinmediato.NEGOCIO.Mensajes.mostrarMensaje;
import static com.facci.chatinmediato.NEGOCIO.Validaciones.obtenerPeso;

public class ChatOffLineActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    private static final int TAKE_PHOTO = 2;
    private static final int RECORD_AUDIO = 3;
    private static final int RECORD_VIDEO = 4;
    private static final int CHOOSE_FILE = 5;
    private static final int DRAWING = 6;
    private static final int DOWNLOAD_IMAGE = 100;
    private static final int DELETE_MESSAGE = 101;
    private static final int DOWNLOAD_FILE = 102;
    private static final int COPY_TEXT = 103;
    private static final int SHARE_TEXT = 104;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE_CONTENT_RESOLVER = 101;
    private EditText edit;
    private static ListView listView;
    private static List<Mensaje> listMensaje;
    private static ChatAdapter chatAdapter;
    private Uri fileUri;
    private String fileURL;
    private ArrayList<Uri> tmpFilesUri;
    SharedPreferences sharedPref;
    static DB_SOSCHAT db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_off_line);
        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        db= new DB_SOSCHAT(this);

        //Initialize the adapter for the chat
        listView = (ListView) findViewById(R.id.messageList_offLine);
        listMensaje = new ArrayList<Mensaje>();

        listMensaje = db.Mensajes_filtro(getMacAddr(),OTRO_DISPOSITIVO.MacOnclic);

        chatAdapter = new ChatAdapter(this, listMensaje);
        listView.setAdapter(chatAdapter);

        //Initialize the list of temporary files URI
        tmpFilesUri = new ArrayList<Uri>();

        //Send a message
        Button button = (Button) findViewById(R.id.sendMessage_offline);
        edit = (EditText) findViewById(R.id.editMessage_offline);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (!edit.getText().toString().equals(""))
                    sendMessage(Mensaje.TEXT_MESSAGE);
                else
                    Toast.makeText(ChatOffLineActivity.this, R.string.mensaje_vacio, Toast.LENGTH_SHORT).show();
            }
        });
        registerForContextMenu(listView);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
        newDialog.setTitle(R.string.Close_chatroom);
        newDialog.setMessage(R.string.contenido_Close_chatroom_offline);
        newDialog.setPositiveButton(R.string.confirmar, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                clearTmpFiles(getExternalFilesDir(null));
                finish();
            }
        });
        newDialog.setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        newDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        saveStateForeground(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        saveStateForeground(false);
    }


    @SuppressLint("MissingSuperCall")
    @Override
    protected void onDestroy() {
        super.onStop();
        clearTmpFiles(getExternalFilesDir(null));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PICK_IMAGE:
                if (resultCode == RESULT_OK && data.getData() != null) {
                    fileUri = data.getData();
                    sendMessage(Mensaje.IMAGE_MESSAGE);
                }
                break;
            case CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE_CONTENT_RESOLVER:
                if (resultCode == RESULT_OK && data.getData() != null) {
                    fileUri = data.getData();
                    sendMessage(Mensaje.IMAGE_MESSAGE);
                    tmpFilesUri.add(fileUri);
                }
                break;
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK && data.getData() != null) {
                    fileUri = data.getData();
                    sendMessage(Mensaje.IMAGE_MESSAGE);
                    tmpFilesUri.add(fileUri);
                }
                break;
            case RECORD_AUDIO:
                if (resultCode == RESULT_OK) {
                    fileURL = (String) data.getStringExtra("audioPath");
                    sendMessage(Mensaje.AUDIO_MESSAGE);
                }
                break;
            case RECORD_VIDEO:
                if (resultCode == RESULT_OK) {
                    fileUri = data.getData();
                    fileURL = MediaFile.getRealPathFromURI(this, fileUri);
                    sendMessage(Mensaje.VIDEO_MESSAGE);
                }
                break;
            case CHOOSE_FILE:
                if (resultCode == RESULT_OK) {
                    fileURL = (String) data.getStringExtra("filePath");
                    sendMessage(Mensaje.FILE_MESSAGE);
                }
                break;
            case DRAWING:
                if (resultCode == RESULT_OK) {
                    fileURL = (String) data.getStringExtra("drawingPath");
                    sendMessage(Mensaje.DRAWING_MESSAGE);
                }
                break;
        }
    }
    public void sendMessage(int type) {
        long millis = System.currentTimeMillis();
        Mensaje mes = new Mensaje(type, edit.getText().toString(), null, OTRO_DISPOSITIVO.nombreOffline);
        mes.setTiempoEnvio(Math.abs(millis));
        mes.setIdentificacion(true);
        mes.setMacOrigen(getMacAddr());
        mes.setMacDestino(OTRO_DISPOSITIVO.MacOnclic);
        mes.setEmergente("false");
        switch (type) {
            case Mensaje.IMAGE_MESSAGE:
                Image image = new Image(this, fileUri);
                mes.setByteArray(image.bitmapToByteArray(image.getBitmapFromUri()));
                mes.setNombreArchivo(image.getFileName());
                mes.setTamanoArchivo(image.getFileSize());
                break;
            case Mensaje.AUDIO_MESSAGE:
                MediaFile audioFile = new MediaFile(this, fileURL, Mensaje.AUDIO_MESSAGE);
                mes.setByteArray(audioFile.fileToByteArray());
                mes.setNombreArchivo(audioFile.getFileName());
                mes.setPathArchivo(audioFile.getFilePath());
                break;
            case Mensaje.VIDEO_MESSAGE:
                MediaFile videoFile = new MediaFile(this, fileURL, Mensaje.AUDIO_MESSAGE);
                mes.setByteArray(videoFile.fileToByteArray());
                mes.setNombreArchivo(videoFile.getFileName());
                mes.setPathArchivo(videoFile.getFilePath());
                tmpFilesUri.add(fileUri);
                break;
            case Mensaje.FILE_MESSAGE:
                MediaFile file = new MediaFile(this, fileURL, Mensaje.FILE_MESSAGE);
                mes.setByteArray(file.fileToByteArray());
                mes.setNombreArchivo(file.getFileName());
                break;
            case Mensaje.DRAWING_MESSAGE:
                MediaFile drawingFile = new MediaFile(this, fileURL, Mensaje.DRAWING_MESSAGE);
                mes.setByteArray(drawingFile.fileToByteArray());
                mes.setNombreArchivo(drawingFile.getFileName());
                mes.setPathArchivo(drawingFile.getFilePath());
                break;
        }
        refreshList(mes,this);
        db.guardarRegistro(mes);
        edit.setText("");
    }

    public static void refreshList(Mensaje mensaje,Context context) {
        byte[] data = SerializationUtils.serialize(mensaje);
        String peso=obtenerPeso(data.length);
        //mensaje.setTexto(peso);
        listMensaje.add(mensaje);
        int conteo=0, posicion=-1;
        for (Mensaje men_list:listMensaje) {
            if(men_list.getTiempoEnvio() == mensaje.getTiempoEnvio()) conteo++;
            posicion++;
        }
        if(conteo>1)listMensaje.remove(posicion);
        chatAdapter.notifyDataSetChanged();
        listView.setSelection(listMensaje.size() - 1);
    }

    public void saveStateForeground(boolean isForeground) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean("isForeground", isForeground);
        edit.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int idItem = item.getItemId();
        switch (idItem) {
            case R.id.send_image:
                showPopup(edit);
                return true;
            case R.id.send_audio:
                startActivityForResult(new Intent(this, RecordAudioActivity.class), RECORD_AUDIO);
                return true;
            case R.id.send_video:
                Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
                if (takeVideoIntent.resolveActivity(getPackageManager()) != null)
                    startActivityForResult(takeVideoIntent, RECORD_VIDEO);
                return true;
            case R.id.send_file:
                Intent chooseFileIntent = new Intent(this, FilePickerActivity.class);
                startActivityForResult(chooseFileIntent, CHOOSE_FILE);
                return true;
            case R.id.send_drawing:
                Intent drawIntent = new Intent(this, DrawingActivity.class);
                startActivityForResult(drawIntent, DRAWING);
                return true;
            case R.id.vaciar:
                final AlertDialog.Builder alertDialogBuilder;
                alertDialogBuilder=new AlertDialog.Builder(this);

                /**Personaliza el dialogo*/
                alertDialogBuilder.setTitle("¿Borrar base de datos?");
                alertDialogBuilder.setMessage("No se podrá recuperar");

                alertDialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        listMensaje.clear();
                        chatAdapter.notifyDataSetChanged();
                        mostrarMensaje("Listo", "Registro vaciado", ChatOffLineActivity.this);
                    }
                });
                alertDialogBuilder.setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                AlertDialog alertDialog=alertDialogBuilder.create();

                /**Muestra el dialogo*/
                alertDialog.show();
                onDestroy();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private Uri mPhotoUri;

    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.pick_image:
                        Intent intent = new Intent(Intent.ACTION_PICK);
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        if (intent.resolveActivity(getPackageManager()) != null)
                            startActivityForResult(intent, PICK_IMAGE);
                        break;

                    case R.id.take_photo:
                        mPhotoUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                new ContentValues());
                        Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        intent2.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoUri);
                        if (intent2.resolveActivity(getPackageManager()) != null)
                            startActivityForResult(intent2, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE_CONTENT_RESOLVER);
                        break;
                }
                return true;
            }
        });
        popup.inflate(R.menu.send_image);
        popup.show();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle(R.string.opciones);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        Mensaje mes = listMensaje.get((int) info.position);

        menu.add(0, DELETE_MESSAGE, Menu.NONE, R.string.Borrar_mensaje);

        if (!mes.getTexto().equals("")) {
            menu.add(0, COPY_TEXT, Menu.NONE, R.string.copiar_mensaje);
            menu.add(0, SHARE_TEXT, Menu.NONE, R.string.Compartir_mensaje);
        }

        int type = mes.getTipo();
        switch (type) {
            case Mensaje.IMAGE_MESSAGE:
                menu.add(0, DOWNLOAD_IMAGE, Menu.NONE, R.string.Descargar_imagen);
                break;
            case Mensaje.FILE_MESSAGE:
                menu.add(0, DOWNLOAD_FILE, Menu.NONE, R.string.Descargar_archivo);
                break;
            case Mensaje.AUDIO_MESSAGE:
                menu.add(0, DOWNLOAD_FILE, Menu.NONE, R.string.Descargar_audio);
                break;
            case Mensaje.VIDEO_MESSAGE:
                menu.add(0, DOWNLOAD_FILE, Menu.NONE, R.string.Descargar_video);
                break;
            case Mensaje.DRAWING_MESSAGE:
                menu.add(0, DOWNLOAD_FILE, Menu.NONE, R.string.Descargar_dibujo);
                break;
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        switch (item.getItemId()) {
            case DOWNLOAD_IMAGE:
                downloadImage(info.id);
                return true;

            case DELETE_MESSAGE:
                deleteMessage(info.id);
                return true;

            case DOWNLOAD_FILE:
                downloadFile(info.id);
                return true;

            case COPY_TEXT:
                copyTextToClipboard(info.id);
                return true;

            case SHARE_TEXT:
                shareMedia(info.id, Mensaje.TEXT_MESSAGE);
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    public void downloadImage(long id) {
        Mensaje mes = listMensaje.get((int) id);
        Bitmap bm = mes.byteArrayToBitmap(mes.getByteArray());
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        FileUtilities.saveImageFromBitmap(this, bm, path, mes.getNombreArchivo());
        FileUtilities.refreshMediaLibrary(this);
    }

    public void downloadFile(long id) {
        Mensaje mes = listMensaje.get((int) id);
        String sourcePath = mes.getPathArchivo();
        String destinationPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        FileUtilities.copyFile(this, sourcePath, destinationPath, mes.getNombreArchivo());
        FileUtilities.refreshMediaLibrary(this);
    }

    public void deleteMessage(long id) {
        listMensaje.remove((int) id);
        chatAdapter.notifyDataSetChanged();
    }

    private void clearTmpFiles(File dir) {
        File[] childDirs = dir.listFiles();
        for (File child : childDirs) {
            if (child.isDirectory()) clearTmpFiles(child);
            else child.delete();
        }
        for (Uri uri : tmpFilesUri) {
            getContentResolver().delete(uri, null, null);
        }
        FileUtilities.refreshMediaLibrary(this);
    }

    public void talkTo(String destination) {
        edit.setText("@" + destination + " : ");
        edit.setSelection(edit.getText().length());
    }

    private void copyTextToClipboard(long id) {
        Mensaje mes = listMensaje.get((int) id);
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("message", mes.getTexto());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Mensaje copiado en el portapapeles", Toast.LENGTH_SHORT).show();
    }

    private void shareMedia(long id, int type) {
        Mensaje mes = listMensaje.get((int) id);
        switch (type) {
            case Mensaje.TEXT_MESSAGE:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, mes.getTexto());
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
        }
    }
}
