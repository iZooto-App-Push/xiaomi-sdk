package com.app.izoototest;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import com.izooto.Lg;
import com.izooto.NotificationHelperListener;
import com.izooto.NotificationWebViewListener;
import com.izooto.Payload;
import com.izooto.PushTemplate;
import com.izooto.TokenReceivedListener;
import com.izooto.iZooto;

public class AppController extends Application implements TokenReceivedListener, NotificationWebViewListener, NotificationHelperListener

{

    @Override
    public void onCreate() {
        super.onCreate();
        iZooto.initialize(this)
                .setTokenReceivedListener(this)
                .setNotificationReceiveListener(this)
                .setLandingURLListener(this)
                .build();
        iZooto.setDefaultTemplate(PushTemplate.DEFAULT);
        iZooto.setDefaultNotificationBanner(R.drawable.ic_baseline_share_24);

    }

   @Override
    public void onTokenReceived(String token) {
        Lg.i("Device token", token + "");


    }


    @Override
    public void onNotificationReceived(Payload payload) {
        Log.e("PayloadData",payload.getTitle());
    }

    @Override
    public void onNotificationOpened(String data) {
     Log.e("AdditionalData",data);
    }

    @Override
    public void onWebView(String landingUrl) {
     Log.e("LandingURL",landingUrl);
    }
}