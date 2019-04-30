package com.tivo.byrt.firstapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.lang.reflect.Array;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "com.tivo.byrt.firstApp.MESSAGE";
    private static final String[] tickers = {"BAC","HASI","IBM","AAPL","TIVO","FB","GOOGL"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void sendMessage(View view) {
        // Finish
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        int i = (int) Math.round(Math.random() * tickers.length);
        String message = tickers[i];
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    public void displayToast(String message) {
        Toast.makeText(getApplicationContext(), message,
                Toast.LENGTH_SHORT).show();
    }

    public void onImageClick(View view) {
        displayToast("Ouch!");
    }

    public void onClickFAB(View view) {
        Intent intent = new Intent(MainActivity.this, OrderActivity.class);
        startActivity(intent);
    }


}
