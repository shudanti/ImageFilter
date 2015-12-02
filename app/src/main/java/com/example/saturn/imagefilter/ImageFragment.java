package com.example.saturn.imagefilter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.sileria.android.view.HorzListView;


public class ImageFragment extends Fragment {
    GLSurfaceView mPreview;
    private static final int SELECT_PHOTO = 101;
    private static final int REQUEST_IMAGE_CAPTURE = 102;
    Button start, stop, capture;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_image,container,false);


        LinearLayout LPreview = (LinearLayout)v.findViewById(R.id.preview);
        mPreview = new GLSurfaceView(getActivity());
        mPreview.setEGLContextClientVersion(2);
        mPreview.setRenderer(new GLLayer(getActivity()));
        LPreview.addView(mPreview);

        // listview Item la anh
        HorzListView listviewImg = (HorzListView) v.findViewById(R.id.horizontal_lv);
        int[] arrImg = { R.drawable.effect_black, R.drawable.effect_boost_1, R.drawable.effect_boost_2,
                R.drawable.effect_boost_3, R.drawable.effect_brightness, R.drawable.effect_brightness,
                R.drawable.effect_color_red,
                R.drawable.effect_color_green,
                R.drawable.effect_color_blue,
                R.drawable.effect_color_depth_64,
                R.drawable.effect_color_depth_32};
        ListFilterAdapter adapterImg = new ListFilterAdapter(
                getActivity(), arrImg);
        listviewImg.setAdapter(adapterImg);
        listviewImg.setOnItemClickListener(onFilterClickListener);

        FloatingActionButton btTake = (FloatingActionButton)v.findViewById(R.id.fab_take);
        btTake.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //mCamera.takePicture(ImageFragment.this, null, null, ImageFragment.this);
            }
        });
        FloatingActionButton btSave = (FloatingActionButton)v.findViewById(R.id.fab_save);
        //btSave.setOnClickListener(onSaveClick);
        FloatingActionButton btAdd = (FloatingActionButton)v.findViewById(R.id.fab_add);
        btAdd.setOnClickListener(onAddClick);
        return v;
    }
    @Override
    public void onPause() {
        super.onPause();
        mPreview.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /*View.OnClickListener onSaveClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            final EditText txtUrl = new EditText(getActivity());
            new AlertDialog.Builder(getActivity())
                    .setTitle("Message")
                    .setMessage("Input file name!")
                    .setView(txtUrl)
                    .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            String str = txtUrl.getText().toString();
                            try {
                                Toast.makeText(getActivity(),"Processing...", Toast.LENGTH_SHORT).show();
                                Bitmap bmp = BitmapFactory.decodeByteArray(cur_img_data, 0, cur_img_data.length);
                                String fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + str +".png";
                                File f = new File(fileName);
                                FileOutputStream fos = new FileOutputStream(f);
                                bmp.compress(Bitmap.CompressFormat.PNG, 90, fos);
                                Toast.makeText(getActivity(),"Save file at "+ fileName, Toast.LENGTH_LONG).show();
                            }
                            catch(Exception ex){
                                Toast.makeText(getActivity(),"Save file fail", Toast.LENGTH_SHORT).show();
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
    };*/
    boolean isFilterChange = false;
    int filterIndex = 0;

    AdapterView.OnItemClickListener onFilterClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            filterIndex = i;
            if (filterIndex == 0){
                GLLayer.shader_selection = 0;
            }
            if (filterIndex == 1){
                GLLayer.shader_selection = GLLayer.BLUR;
            }
            if	(filterIndex == 2){
                GLLayer.shader_selection = GLLayer.EDGE;
            }
            if	(filterIndex == 3){
                GLLayer.shader_selection = GLLayer.EMBOSS;
            }
        }
    };
    View.OnClickListener onAddClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(photoPickerIntent, SELECT_PHOTO);
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
