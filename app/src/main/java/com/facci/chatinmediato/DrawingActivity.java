package com.facci.chatinmediato;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.facci.chatinmediato.CustomViews.DrawingView;
import com.facci.chatinmediato.util.FileUtilities;

import java.io.File;

import yuku.ambilwarna.AmbilWarnaDialog;

public class DrawingActivity extends AppCompatActivity  {
    private static final String TAG = "DrawingActivity";
    private DrawingView drawView;
    private ImageButton currentPaint;
    private float smallBrush, mediumBrush, largeBrush;
    private Button brushButton, eraserButton, newDrawing, saveDrawing;
    LinearLayout mLayout;
    int mDefaulColor;
    Button mButton;
    ImageButton mButton2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing);

        mLayout = (LinearLayout) findViewById(R.id.layout);
        mDefaulColor= ContextCompat.getColor(DrawingActivity.this,R.color.button_background);
        mButton = (Button) findViewById(R.id.button1);
        mButton2 = (ImageButton) findViewById(R.id.button3);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenColor();
            }
        });

        mButton2.setTag("vacio");
        drawView = (DrawingView)findViewById(R.id.drawing);
        LinearLayout layout = (LinearLayout) findViewById(R.id.colorPalette);
        brushButton = (Button) findViewById(R.id.chooseBrush);
        eraserButton = (Button) findViewById(R.id.chooseEraser);
        newDrawing = (Button) findViewById(R.id.newDrawing);
        saveDrawing = (Button) findViewById(R.id.saveDrawing);

        //Retrieve brush sizes
        smallBrush = getResources().getInteger(R.integer.small_size);
        mediumBrush = getResources().getInteger(R.integer.medium_size);
        largeBrush = getResources().getInteger(R.integer.large_size);

        //Default colour is the first color in the color palette
        currentPaint = (ImageButton) layout.getChildAt(0);
        drawView.setColor(currentPaint.getTag().toString());

        //Default brush size is medium
        drawView.setBrushSize(smallBrush);
        drawView.setLastBrushSize(smallBrush);

        brushButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                chooseBrush();
            }
        });

        eraserButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                chooseEraser();
            }
        });

        newDrawing.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                newDrawing();
            }
        });

        saveDrawing.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                saveDrawing();
            }
        });
    }

    public void OpenColor(){
        AmbilWarnaDialog color = new AmbilWarnaDialog(this, mDefaulColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {

            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                mDefaulColor=color;
                mButton2.setBackgroundColor(mDefaulColor);
                mButton2.setTag(String.format("#%X", mDefaulColor ));
                String colors = mButton2.getTag().toString();
                drawView.setColor(colors);
                currentPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint));
                currentPaint=(ImageButton) mButton2;
                currentPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint_selected));
                drawView.setErase(false);
                drawView.setBrushSize(drawView.getLastBrushSize());
                Toast.makeText(DrawingActivity.this, "Formato 1 "+String.format("#%X", mDefaulColor )+" Formato 2 "+"#"+Integer.toHexString(mDefaulColor) , Toast.LENGTH_LONG).show();
            }
        });
        color.show();
    }
    public void paintClicked(View view){
        if(currentPaint != view){
            ImageButton button = (ImageButton) view;
            String color = view.getTag().toString();
            if (color!="vacio") {
                drawView.setColor(color);

                //Change the background of the old color to normal, and change background of the new color to 'pressed'
                currentPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint));
                currentPaint = (ImageButton) button;
                currentPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint_selected));

                drawView.setErase(false);
                drawView.setBrushSize(drawView.getLastBrushSize());
            }
        }
    }

    private void chooseBrush(){
        Log.v(TAG, "Choose brush size");
        final Dialog brushDialog = new Dialog(DrawingActivity.this);
        brushDialog.setTitle(R.string.Brush_size);
        brushDialog.setContentView(R.layout.brush_chooser);
        brushDialog.show();

        ImageView smallBrush = (ImageView) brushDialog.findViewById(R.id.small_brush);
        smallBrush.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                drawView.setBrushSize(DrawingActivity.this.smallBrush);
                drawView.setLastBrushSize(DrawingActivity.this.smallBrush);
                drawView.setErase(false);
                brushDialog.dismiss();
            }
        });

        ImageView mediumBrush = (ImageView) brushDialog.findViewById(R.id.medium_brush);
        mediumBrush.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                drawView.setBrushSize(DrawingActivity.this.mediumBrush);
                drawView.setLastBrushSize(DrawingActivity.this.mediumBrush);
                drawView.setErase(false);
                brushDialog.dismiss();
            }
        });

        ImageView largeBrush = (ImageView) brushDialog.findViewById(R.id.large_brush);
        largeBrush.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                drawView.setBrushSize(DrawingActivity.this.largeBrush);
                drawView.setLastBrushSize(DrawingActivity.this.largeBrush);
                drawView.setErase(false);
                brushDialog.dismiss();
            }
        });
    }

    private void chooseEraser(){
        Log.v(TAG, "Choose eraser size");
        final Dialog eraserDialog = new Dialog(DrawingActivity.this);
        eraserDialog.setTitle(R.string.Eraser_size);
        eraserDialog.setContentView(R.layout.brush_chooser);
        eraserDialog.show();

        ImageView smallBrush = (ImageView) eraserDialog.findViewById(R.id.small_brush);
        smallBrush.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                drawView.setErase(true);
                drawView.setBrushSize(DrawingActivity.this.smallBrush);
                eraserDialog.dismiss();
            }
        });

        ImageView mediumBrush = (ImageView) eraserDialog.findViewById(R.id.medium_brush);
        mediumBrush.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                drawView.setErase(true);
                drawView.setBrushSize(DrawingActivity.this.mediumBrush);
                eraserDialog.dismiss();
            }
        });

        ImageView largeBrush = (ImageView) eraserDialog.findViewById(R.id.large_brush);
        largeBrush.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                drawView.setErase(true);
                drawView.setBrushSize(DrawingActivity.this.largeBrush);
                eraserDialog.dismiss();
            }
        });
    }

    private void newDrawing(){
        AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
        newDialog.setTitle(R.string.new_drawing);
        newDialog.setMessage(R.string.contenido_new_drawing);

        newDialog.setPositiveButton(R.string.confirmar, new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {
                drawView.startNewDrawing();
                mButton2.setBackgroundColor(Color.WHITE);
                mButton2.setTag("vacio");
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

    private void saveDrawing(){
        AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
        newDialog.setTitle(R.string.titulo_enviar_dibujo);
        newDialog.setMessage(R.string.contenido_enivar_dibujo);

        newDialog.setPositiveButton(R.string.confirmar, new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {
                drawView.setDrawingCacheEnabled(true);
                String path = DrawingActivity.this.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
                String fileName = FileUtilities.fileName() + ".jpg";
                FileUtilities.saveImageFromBitmap(DrawingActivity.this, drawView.getDrawingCache(), path, fileName);
                drawView.destroyDrawingCache();

                Intent intent = getIntent();
                intent.putExtra("drawingPath", path + File.separator + fileName);
                setResult(RESULT_OK, intent);
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
}
