/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.izooto;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import org.json.JSONObject;
import java.util.Map;

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
public class iZootoMessagingService extends FirebaseMessagingService {
    private  Payload payload = null;
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        try {
            if (remoteMessage.getData().size() > 0) {
                Log.v("Push Type","FCM");
                Map<String, String> data = remoteMessage.getData();
                handleNow(data);
            }
            if (remoteMessage.getNotification() != null) {
                sendNotification(remoteMessage);
                Log.d(AppConstant.APP_NAME_TAG, AppConstant.PAYLOAD + remoteMessage.getNotification().getBody());
            }
        }
        catch (Exception ex)
        {
            Log.e(AppConstant.FIREBASEEXCEPTION,ex.toString());
        }


    }
    private void sendNotification(RemoteMessage remoteMessage) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                        .setContentTitle(remoteMessage.getNotification().getTitle())
                        .setContentText(remoteMessage.getNotification().getBody())
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    AppConstant.CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }


    public   void handleNow(final Map<String, String> data) {
        Log.d(AppConstant.APP_NAME_TAG, AppConstant.NOTIFICATIONRECEIVED);
        PreferenceUtil preferenceUtil =PreferenceUtil.getInstance(this);
        try {
            if(data.get(AppConstant.AD_NETWORK) !=null && data.get(AppConstant.GLOBAL)!=null)
            {
                AdMediation.getAdJsonData(this,data);
                preferenceUtil.setBooleanData(AppConstant.MEDIATION,true);
            }
            else {
                preferenceUtil.setBooleanData(AppConstant.MEDIATION, false);
                JSONObject payloadObj = new JSONObject(data);
                if (payloadObj.optLong(ShortpayloadConstant.CREATEDON) > PreferenceUtil.getInstance(this).getLongValue(AppConstant.DEVICE_REGISTRATION_TIMESTAMP)) {
                    payload = new Payload();
                    payload.setCreated_Time(payloadObj.optString(ShortpayloadConstant.CREATEDON));
                    payload.setFetchURL(payloadObj.optString(ShortpayloadConstant.FETCHURL));
                    payload.setKey(payloadObj.optString(ShortpayloadConstant.KEY));
                    payload.setId(payloadObj.optString(ShortpayloadConstant.ID));
                    payload.setRid(payloadObj.optString(ShortpayloadConstant.RID));
                    payload.setLink(payloadObj.optString(ShortpayloadConstant.LINK));
                    payload.setTitle(payloadObj.optString(ShortpayloadConstant.TITLE));
                    payload.setMessage(payloadObj.optString(ShortpayloadConstant.NMESSAGE));
                    payload.setIcon(payloadObj.optString(ShortpayloadConstant.ICON));
                    payload.setReqInt(payloadObj.optInt(ShortpayloadConstant.REQINT));
                    payload.setTag(payloadObj.optString(ShortpayloadConstant.TAG));
                    payload.setBanner(payloadObj.optString(ShortpayloadConstant.BANNER));
                    payload.setAct_num(payloadObj.optInt(ShortpayloadConstant.ACTNUM));
                    payload.setBadgeicon(payloadObj.optString(ShortpayloadConstant.BADGE_ICON));
                    payload.setBadgecolor(payloadObj.optString(ShortpayloadConstant.BADGE_COLOR));
                    payload.setSubTitle(payloadObj.optString(ShortpayloadConstant.SUBTITLE));
                    payload.setGroup(payloadObj.optInt(ShortpayloadConstant.GROUP));
                    payload.setBadgeCount(payloadObj.optInt(ShortpayloadConstant.BADGE_COUNT));
                    // Button 2
                    payload.setAct1name(payloadObj.optString(ShortpayloadConstant.ACT1NAME));
                    payload.setAct1link(payloadObj.optString(ShortpayloadConstant.ACT1LINK));
                    payload.setAct1icon(payloadObj.optString(ShortpayloadConstant.ACT1ICON));
                    payload.setAct1ID(payloadObj.optString(ShortpayloadConstant.ACT1ID));
                    // Button 2
                    payload.setAct2name(payloadObj.optString(ShortpayloadConstant.ACT2NAME));
                    payload.setAct2link(payloadObj.optString(ShortpayloadConstant.ACT2LINK));
                    payload.setAct2icon(payloadObj.optString(ShortpayloadConstant.ACT2ICON));
                    payload.setAct2ID(payloadObj.optString(ShortpayloadConstant.ACT2ID));

                    payload.setInapp(payloadObj.optInt(ShortpayloadConstant.INAPP));
                    payload.setTrayicon(payloadObj.optString(ShortpayloadConstant.TARYICON));
                    payload.setSmallIconAccentColor(payloadObj.optString(ShortpayloadConstant.ICONCOLOR));
                    payload.setSound(payloadObj.optString(ShortpayloadConstant.SOUND));
                    payload.setLedColor(payloadObj.optString(ShortpayloadConstant.LEDCOLOR));
                    payload.setLockScreenVisibility(payloadObj.optInt(ShortpayloadConstant.VISIBILITY));
                    payload.setGroupKey(payloadObj.optString(ShortpayloadConstant.GKEY));
                    payload.setGroupMessage(payloadObj.optString(ShortpayloadConstant.GMESSAGE));
                    payload.setFromProjectNumber(payloadObj.optString(ShortpayloadConstant.PROJECTNUMBER));
                    payload.setCollapseId(payloadObj.optString(ShortpayloadConstant.COLLAPSEID));
                    payload.setPriority(payloadObj.optInt(ShortpayloadConstant.PRIORITY));
                    payload.setRawPayload(payloadObj.optString(ShortpayloadConstant.RAWDATA));
                    payload.setAp(payloadObj.optString(ShortpayloadConstant.ADDITIONALPARAM));
                    payload.setCfg(payloadObj.optInt(ShortpayloadConstant.CFG));
                    payload.setTime_to_live(payloadObj.optString(ShortpayloadConstant.TIME_TO_LIVE));
                    payload.setPush_type(AppConstant.PUSH_FCM);
                    payload.setSound(payloadObj.optString(ShortpayloadConstant.NOTIFICATION_SOUND));
                    payload.setMaxNotification(payloadObj.optInt(ShortpayloadConstant.MAX_NOTIFICATION));
                    payload.setCustomNotification(payloadObj.optInt(ShortpayloadConstant.CUSTOM_NOTIFICATION));



                } else
                    return;
            }

        } catch (Exception e) {

            e.printStackTrace();
            Lg.d(AppConstant.APP_NAME_TAG, e.toString());
        }
        if (iZooto.appContext == null)
            iZooto.appContext = this;
        Handler mainHandler = new Handler(Looper.getMainLooper());
        Runnable myRunnable = new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                iZooto.processNotificationReceived(payload);

            } // This is your code
        };
        mainHandler.post(myRunnable);



    }



}