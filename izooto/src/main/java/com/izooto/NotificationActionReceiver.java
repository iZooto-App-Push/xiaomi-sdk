package com.izooto;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;

public class NotificationActionReceiver extends BroadcastReceiver {

    private String mUrl;
    private int inApp;
    private String rid;
    private  String cid;
    private int btncount;
    private String api_url;
    private String additionalData;
    private String phoneNumber;
    private String act1ID;
    private String act2ID;
    private String langingURL;
    private String act2URL;
    private String act1URL;
    private String btn1Title;
    private String btn2Title;
    private String clickIndex = "0";
    private String lastClickIndex = "0";


    @Override
    public void onReceive(Context context, Intent intent) {
        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);

        context.sendBroadcast(it);
        getBundleData(context, intent);
        String appVersion = Util.getSDKVersion();
        mUrl.replace(AppConstant.BROWSERKEYID, PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.FCM_DEVICE_TOKEN));
        getBundleData(context, intent);
        try {
            final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(iZooto.appContext);

            if (btncount!=0) {
                api_url = AppConstant.API_PID + preferenceUtil.getiZootoID(AppConstant.APPPID)+ AppConstant.VER_ + appVersion +
                        AppConstant.CID_ + cid + AppConstant.ANDROID_ID + Util.getAndroidId(iZooto.appContext) + AppConstant.RID_ + rid + AppConstant.NOTIFICATION_OP + "click&btn=" + btncount;
}
            else
            {
                api_url = AppConstant.API_PID +preferenceUtil.getiZootoID(AppConstant.APPPID) + AppConstant.VER_ + appVersion +
                        AppConstant.CID_  + cid + AppConstant.ANDROID_ID + Util.getAndroidId(iZooto.appContext) + AppConstant.RID_ + rid + AppConstant.NOTIFICATION_OP + "click";
 }

            if(clickIndex.equalsIgnoreCase("1")) {
                RestClient.postRequest(RestClient.NOTIFICATIONCLICK + api_url, new RestClient.ResponseHandler() {


                    @Override
                    void onFailure(int statusCode, String response, Throwable throwable) {
                        super.onFailure(statusCode, response, throwable);
                    }

                    @Override
                    void onSuccess(String response) {
                        super.onSuccess(response);
                        // Log.e("Click","call");
                    }
                });
            }
            if (lastClickIndex.equalsIgnoreCase("1")){
                String encodeData = "";
                try {
                    HashMap<String, Object> data = new HashMap<>();
                    data.put(AppConstant.LAST_NOTIFICAION_CLICKED, true);
                    JSONObject jsonObject = new JSONObject(data);
                    encodeData = URLEncoder.encode(jsonObject.toString(), AppConstant.UTF);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                String lastClickAPIUrl = AppConstant.API_PID +preferenceUtil.getiZootoID(AppConstant.APPPID) + AppConstant.VER_ + appVersion +
                        AppConstant.ANDROID_ID + Util.getAndroidId(iZooto.appContext) + AppConstant.VAL + encodeData + AppConstant.ACT + "add" + AppConstant.ISID_ + "1" + AppConstant.ET_ + "userp";
                RestClient.postRequest(RestClient.LASTNOTIFICATIONCLICKURL + lastClickAPIUrl, new RestClient.ResponseHandler() {


                    @Override
                    void onFailure(int statusCode, String response, Throwable throwable) {
                        super.onFailure(statusCode, response, throwable);
                    }

                    @Override
                    void onSuccess(String response) {
                        super.onSuccess(response);
                        // Log.e("Click","call");
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (additionalData.equalsIgnoreCase(""))
        {
            additionalData = "1";
        }


        if (!additionalData.equalsIgnoreCase("1") && inApp >=0)
        {

            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put(AppConstant.BUTTON_ID_1, act1ID);
            hashMap.put(AppConstant.BUTTON_TITLE_1,btn1Title);
            hashMap.put(AppConstant.BUTTON_URL_1, act1URL);
            hashMap.put(AppConstant.ADDITIONAL_DATA, additionalData);
            hashMap.put(AppConstant.LANDING_URL, langingURL);
            hashMap.put(AppConstant.BUTTON_ID_2, act2ID);
            hashMap.put(AppConstant.BUTTON_TITLE_2,btn2Title);
            hashMap.put(AppConstant.BUTTON_URL_2,act2URL);
            hashMap.put(AppConstant.ACTION_TYPE, String.valueOf(btncount));
            JSONObject jsonObject = new JSONObject(hashMap);
            iZooto.notificationActionHandler(jsonObject.toString());
        }
        else
        {
            if (inApp == 1 && phoneNumber.equalsIgnoreCase(AppConstant.NO)) {
                if (iZooto.mBuilder!=null && iZooto.mBuilder.mWebViewListener!=null){
                    iZooto.notificationInAppAction(mUrl);
                }else
                    iZootoWebViewActivity.startActivity(context, mUrl);
            }else if (inApp == 2 && phoneNumber.equalsIgnoreCase(AppConstant.NO)){
                PackageManager pm = context.getPackageManager();
                Intent LaunchIntent = null;
                String name = "";
                try {
                    if (pm != null && !Util.isAppInForeground(iZooto.appContext)) {
                        ApplicationInfo app = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
                        name = (String) pm.getApplicationLabel(app);
                        LaunchIntent = pm.getLaunchIntentForPackage(context.getPackageName());
                        Intent intentAppLaunch = LaunchIntent; // new Intent();
                        intentAppLaunch.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        context.startActivity(intentAppLaunch);
                    }
                    Log.d(AppConstant.APP_NAME_TAG + "Found it:",name);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
            else {
                try {
                    if (phoneNumber.equalsIgnoreCase(AppConstant.NO)) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mUrl));
                        browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        context.startActivity(browserIntent);
                    } else {
                        Intent browserIntent = new Intent(Intent.ACTION_DIAL, Uri.parse(phoneNumber));
                        browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        context.startActivity(browserIntent);
                    }

                } catch (Exception ex) {
                    Log.e("ex", ex.toString());
                }
            }
        }

    }

    private void getBundleData(Context context, Intent intent) {
        Bundle tempBundle = intent.getExtras();
        if (tempBundle != null) {
            if (tempBundle.containsKey(AppConstant.KEY_WEB_URL))
                mUrl = tempBundle.getString(AppConstant.KEY_WEB_URL);
            if (tempBundle.containsKey(AppConstant.KEY_IN_APP))
                inApp = tempBundle.getInt(AppConstant.KEY_IN_APP);
            if (tempBundle.containsKey(AppConstant.KEY_IN_RID))
                rid = tempBundle.getString(AppConstant.KEY_IN_RID);
            if (tempBundle.containsKey(AppConstant.KEY_IN_CID))
                cid = tempBundle.getString(AppConstant.KEY_IN_CID);
            if(tempBundle.containsKey(AppConstant.KEY_IN_BUTOON))
                btncount = tempBundle.getInt(AppConstant.KEY_IN_BUTOON);
            if(tempBundle.containsKey(AppConstant.KEY_IN_ADDITIONALDATA))
                additionalData = tempBundle.getString(AppConstant.KEY_IN_ADDITIONALDATA);
            if(tempBundle.containsKey(AppConstant.KEY_IN_PHONE))
                phoneNumber=tempBundle.getString(AppConstant.KEY_IN_PHONE);
            if(tempBundle.containsKey(AppConstant.KEY_IN_ACT1ID))
                act1ID=tempBundle.getString(AppConstant.KEY_IN_ACT1ID);
            if(tempBundle.containsKey(AppConstant.KEY_IN_ACT2ID))
                act2ID=tempBundle.getString(AppConstant.KEY_IN_ACT2ID);
            if(tempBundle.containsKey(AppConstant.LANDINGURL))
                langingURL=tempBundle.getString(AppConstant.LANDINGURL);
            if(tempBundle.containsKey(AppConstant.ACT1URL))
                act1URL=tempBundle.getString(AppConstant.ACT1URL);
            if(tempBundle.containsKey(AppConstant.ACT2URL))
                act2URL=tempBundle.getString(AppConstant.ACT2URL);
            if(tempBundle.containsKey(AppConstant.ACT1TITLE))
                btn1Title=tempBundle.getString(AppConstant.ACT1TITLE);
            if(tempBundle.containsKey(AppConstant.ACT2TITLE))
                btn2Title=tempBundle.getString(AppConstant.ACT2TITLE);
            if(tempBundle.containsKey(AppConstant.CLICKINDEX))
                clickIndex=tempBundle.getString(AppConstant.CLICKINDEX);
            if(tempBundle.containsKey(AppConstant.LASTCLICKINDEX))
                lastClickIndex=tempBundle.getString(AppConstant.LASTCLICKINDEX);



            if (tempBundle.containsKey(AppConstant.KEY_NOTIFICITON_ID)) {
                NotificationManager notificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(tempBundle.getInt(AppConstant.KEY_NOTIFICITON_ID));
            }
        }
    }
}