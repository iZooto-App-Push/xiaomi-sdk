package com.app.izoototest;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.izooto.AppConstant;
import com.izooto.Lg;
import com.izooto.NotificationHelperListener;
import com.izooto.NotificationWebViewListener;
import com.izooto.Payload;
import com.izooto.PushTemplate;
import com.izooto.TokenReceivedListener;
import com.izooto.iZooto;

import org.json.JSONObject;

public class AppController extends Application implements TokenReceivedListener, NotificationWebViewListener, NotificationHelperListener

{

    @Override
    public void onCreate() {
        super.onCreate();
//        iZooto.initialize(this)
//                .setTokenReceivedListener(this)
//                .setNotificationReceiveListener(this)
//               // .setLandingURLListener(this)
//                .build();
//        iZooto.setDefaultTemplate(PushTemplate.DEFAULT);
//        iZooto.setDefaultNotificationBanner(R.drawable.ic_baseline_share_24);
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                try {
                    if (!task.isSuccessful()) {
                        Log.w("TAG", "Fetching FCM registration token failed", task.getException());
                        return;
                    }


                    // Get new FCM registration token
                    String token = task.getResult();
                    Log.e("TAG", "AppController onComplete:token -- "+token );
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(AppConstant.FCM_TOKEN_FROM_JSON,token);
                    iZooto.initialize(AppController.this, jsonObject.toString())
                            .setTokenReceivedListener(AppController.this)
                            .setLandingURLListener(AppController.this)
                            .setNotificationReceiveListener(AppController.this)
                            .build();
                } catch (Exception e) {
                    Log.e("TAG", "onComplete: AppController" );

                }
            }
        });


    }

   @Override
    public void onTokenReceived(String token) {
        Lg.i(" Token JSON String ", token + "");


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