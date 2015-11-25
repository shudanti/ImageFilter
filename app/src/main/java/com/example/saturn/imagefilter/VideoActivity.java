package com.example.saturn.imagefilter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.FileOutputStream;

public class VideoActivity extends Activity {

    private VideoView mVideoView ;
    private static final int SELECT_PHOTO = 100;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_VIDEO_CAPTURE = 2;
    private Bitmap src;
    String effectName = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        mVideoView = (VideoView) findViewById(R.id.video_main);
    }
/*
    public void saveBitmap(View v){
        final EditText txtUrl = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle("Message")
                .setMessage("Input file name!")
                .setView(txtUrl)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String str = txtUrl.getText().toString();
                        try {
                            Toast.makeText(VideoActivity.this,"Processing...", Toast.LENGTH_SHORT).show();
                            Bitmap bmp = ((BitmapDrawable)mVideoView.getDrawable()).getBitmap();
                            String fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + str +".png";
                            File f = new File(fileName);
                            FileOutputStream fos = new FileOutputStream(f);
                            bmp.compress(Bitmap.CompressFormat.PNG, 90, fos);
                            Toast.makeText(VideoActivity.this,"Save file at "+ fileName, Toast.LENGTH_LONG).show();
                        }
                        catch(Exception ex){
                            Toast.makeText(VideoActivity.this,"Save file fail", Toast.LENGTH_SHORT).show();
                            ex.printStackTrace();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .show();

    }

 */   @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case REQUEST_VIDEO_CAPTURE:
                if(resultCode == RESULT_OK){
                    Uri videoUri = data.getData();
                    mVideoView.setVideoURI(videoUri);
                    mVideoView.start();
                }
        }
    }

    public void btTakeVideo(View v) {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
