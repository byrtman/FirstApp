package com.tivo.byrt.firstapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "com.tivo.byrt.firstApp.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void sendMessage(View view) {
        // Finish
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        String message = "BAC";
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }
}
