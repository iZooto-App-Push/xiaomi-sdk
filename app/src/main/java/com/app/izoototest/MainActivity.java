package com.app.izoototest;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import com.izooto.NotificationHelperListener;
import com.izooto.Payload;
import com.izooto.TokenReceivedListener;
import com.izooto.iZooto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements NotificationHelperListener, TokenReceivedListener
{

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        HashMap<String,Object> data= new HashMap<>();
        data.put("Language","Bangali");
      //  iZooto.addEvent("Xiaomi",data);
       iZooto.addUserProperty(data);
//        List<String> topicNale=new ArrayList<>();
//        topicNale.add("DOB");
//        iZooto.addTag(topicNale);
        Log.e("Language", Locale.getDefault().toLanguageTag());
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onNotificationReceived(Payload payload) {
        Log.e("Received",payload.getTitle());

    }


    @Override
    public void onNotificationOpened(String data) {
        Log.e("NotificationClicked",data);
       // startActivity(new Intent(MainActivity.this,MainActivity.class));


    }


    @Override
    public void onTokenReceived(String token) {
        Log.e("Token",token);
    }
}
