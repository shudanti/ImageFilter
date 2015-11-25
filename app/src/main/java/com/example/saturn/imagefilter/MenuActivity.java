package com.example.saturn.imagefilter;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
    }
    public void OpenImageActivity(View v)
    {
        Intent intent = new Intent(this, ImageActivity.class);
        startActivity(intent);
    }
    public void OpenVideoActivity(View v)
    {
        Intent intent = new Intent(this, VideoActivity.class);
        startActivity(intent);
    }
}
