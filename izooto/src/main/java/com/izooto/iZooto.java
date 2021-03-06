package com.izooto;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.izooto.shortcutbadger.ShortcutBadger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.izooto.AppConstant.FCM_TOKEN_FROM_JSON;
import static com.izooto.AppConstant.HUAWEI_TOKEN_FROM_JSON;
import static com.izooto.AppConstant.TAG;
import static com.izooto.AppConstant.XIAOMI_TOKEN_FROM_JSON;

public class iZooto {
    static Context appContext;
    private static String senderId;
    public static String mIzooToAppId;
    public static Builder mBuilder;
    public static int icon;
    private static Payload payload;
    public static boolean mUnsubscribeWhenNotificationsAreDisabled;
    protected static Listener mListener;
    protected static Handler mHandler;
    private static FirebaseAnalyticsTrack firebaseAnalyticsTrack;
    public static String inAppOption;
    @SuppressLint("StaticFieldLeak")
    static Activity curActivity;
    public static String SDKDEF ="power-push";
    public static String soundID;
    public static int bannerImage;


    public static boolean isHybrid = false; //check for SDK(Flutter,React native)

    private static String advertisementID;
    public static void setSenderId(String senderId) {
        iZooto.senderId = senderId;
    }
    private static void setActivity(Activity activity){
        curActivity = activity;
    }
    public static void setIzooToAppId(String izooToAppId) {
        mIzooToAppId = izooToAppId;
    }
    public static iZooto.Builder initialize(Context context) {
        return new iZooto.Builder(context);
    }
    public enum OSInAppDisplayOption {
        None, InAppAlert, Notification
    }
    private static void init(Builder builder) {
        final Context context = builder.mContext;
        appContext = context.getApplicationContext();
        mBuilder = builder;
        builder.mContext = null;
        final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = appInfo.metaData;
            if (bundle != null) {
                if (bundle.containsKey(AppConstant.IZOOTO_APP_ID)) {
                    mIzooToAppId = bundle.getString(AppConstant.IZOOTO_APP_ID);
                    preferenceUtil.setStringData(AppConstant.ENCRYPTED_PID,mIzooToAppId);
                }
                if (mIzooToAppId =="") {
                    Lg.e(AppConstant.APP_NAME_TAG, AppConstant.MISSINGID);
                }
                else {
                    Lg.i(AppConstant.APP_NAME_TAG, mIzooToAppId + "");
                    RestClient.get(AppConstant.GOOGLE_JSON_URL + mIzooToAppId +".dat", new RestClient.ResponseHandler() {
                        @Override
                        void onFailure(int statusCode, String response, Throwable throwable) {
                            super.onFailure(statusCode, response, throwable);
                        }

                        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                        @Override
                        void onSuccess(String response) {
                            super.onSuccess(response);
                            if (!response.isEmpty() && response.length() > 20 && response != null) {
                                try {
                                    final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
                                    JSONObject jsonObject = new JSONObject(Objects.requireNonNull(Util.decrypt(AppConstant.SECRETKEY, response)));
                                    senderId = jsonObject.getString(AppConstant.SENDERID);
                                    String appId = jsonObject.optString(AppConstant.APPID);
                                    String apiKey = jsonObject.optString(AppConstant.APIKEY);
                                    String mKey =jsonObject.optString(AppConstant.MIAPIKEY);
                                    String mId =jsonObject.optString(AppConstant.MIAPPID);
                                    String hms_appId =jsonObject.optString(AppConstant.HMS_APP_ID);
                                    mIzooToAppId = jsonObject.optString(AppConstant.APPPID);
                                    preferenceUtil.setiZootoID(AppConstant.APPPID, mIzooToAppId);
                                    trackAdvertisingId();
                                    if(!mKey.isEmpty() && !mId.isEmpty() && Build.MANUFACTURER.equalsIgnoreCase("Xiaomi") && !preferenceUtil.getBoolean(AppConstant.CAN_GENERATE_XIAOMI_TOKEN)){
                                        XiaomiSDKHandler xiaomiSDKHandler = new XiaomiSDKHandler(iZooto.appContext, mId, mKey);
                                        xiaomiSDKHandler.onMIToken();
                                    }
                                    if (!hms_appId.isEmpty() && Build.MANUFACTURER.equalsIgnoreCase("Huawei")  && !preferenceUtil.getBoolean(AppConstant.CAN_GENERATE_HUAWEI_TOKEN)) {
                                        initHmsService(appContext);
                                    }
                                    if (senderId != null && !senderId.isEmpty()) {
                                        init(context, apiKey, appId);
                                    } else {
                                        Lg.e(AppConstant.APP_NAME_TAG, appContext.getString(R.string.something_wrong_fcm_sender_id));
                                    }
                                    if (!preferenceUtil.getStringData(AppConstant.USER_LOCAL_DATA).isEmpty()) {
                                        JSONObject json  = new JSONObject(preferenceUtil.getStringData(AppConstant.USER_LOCAL_DATA));
                                        addUserProperty(Util.toMap(json));
                                    }
                                    if (!preferenceUtil.getStringData(AppConstant.EVENT_LOCAL_DATA_EN).isEmpty() && !preferenceUtil.getStringData(AppConstant.EVENT_LOCAL_DATA_EV).isEmpty()) {
                                        JSONObject json  = new JSONObject(preferenceUtil.getStringData(AppConstant.EVENT_LOCAL_DATA_EV));
                                        addEvent(preferenceUtil.getStringData(AppConstant.EVENT_LOCAL_DATA_EN), Util.toMap(json));
                                    }
                                    if (preferenceUtil.getBoolean(AppConstant.IS_SET_SUBSCRIPTION_METHOD))
                                        iZooto.setSubscription(preferenceUtil.getBoolean(AppConstant.SET_SUBSCRITION_LOCAL_DATA));
                                } catch (JSONException e) {
                                    Lg.e(AppConstant.APP_NAME_TAG, e.toString());
                                }
                            }
                            else
                            {
                              Log.e(AppConstant.APP_NAME_TAG,"Account id is not sync properly on panel");
                            }
                        }


                    });
                }
            } else {
                Lg.e(AppConstant.APP_NAME_TAG, AppConstant.MESSAGE);
            }


        } catch (Throwable t) {
            t.printStackTrace();
        }

    }
    private static void initHmsService(final Context context){
        if (context == null)
            return;

        HMSTokenGenerator hmsTokenGenerator = new HMSTokenGenerator();
        hmsTokenGenerator.getHMSToken(context, new HMSTokenListener.HMSTokenGeneratorHandler() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void complete(String id) {
                Log.i(AppConstant.APP_NAME_TAG, "HMS Token - " + id);
                PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
                if (id != null && !id.isEmpty()) {
                    if (!preferenceUtil.getBoolean(AppConstant.IS_UPDATED_HMS_TOKEN)) {
                        preferenceUtil.setBooleanData(AppConstant.IS_UPDATED_HMS_TOKEN, true);
                        preferenceUtil.setBooleanData(AppConstant.IS_TOKEN_UPDATED, false);
                        iZooto.registerToken();
                    }

                }
            }

            @Override
            public void failure(String errorMessage) {
                Lg.v(AppConstant.APP_NAME_TAG, errorMessage);
            }
        });
    }
    private static void init(final Context context, String apiKey, String appId) {
        FCMTokenGenerator fcmTokenGenerator = new FCMTokenGenerator();
        fcmTokenGenerator.getToken(context, senderId, apiKey, appId, new TokenGenerator.TokenGenerationHandler() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void complete(String id) {
                Util util = new Util();
                final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
                if (util.isInitializationValid()) {
                    Lg.i(AppConstant.APP_NAME_TAG, AppConstant.DEVICETOKEN  + preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN));
                    registerToken();
                    if (iZooto.isHybrid)
                        preferenceUtil.setBooleanData(AppConstant.IS_HYBRID_SDK, iZooto.isHybrid);
                    ActivityLifecycleListener.registerActivity((Application)appContext);
                    setCurActivity(context);
                    areNotificationsEnabledForSubscribedState(appContext);
                    if (FirebaseAnalyticsTrack.canFirebaseAnalyticsTrack())
                        firebaseAnalyticsTrack = new FirebaseAnalyticsTrack(appContext);

                }
            }

            @Override
            public void failure(String errorMsg) {
                Lg.e(AppConstant.APP_NAME_TAG, errorMsg);
            }
        });

    }

    private static void trackAdvertisingId(){
        if(appContext!=null) {
            final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
            AdvertisingIdClient.getAdvertisingId(appContext, new AdvertisingIdClient.Listener() {
                @Override
                public void onAdvertisingIdClientFinish(AdvertisingIdClient.AdInfo adInfo) {
                    advertisementID = adInfo.getId();
                    preferenceUtil.setStringData(AppConstant.ADVERTISING_ID, advertisementID);
                    invokeFinish(advertisementID, preferenceUtil.getStringData(AppConstant.ENCRYPTED_PID));

                }

                @Override
                public void onAdvertisingIdClientFail(Exception exception) {
                    invokeFail(new Exception(TAG + " - Error: context null"));
                }
            });
        }
    }

    public static synchronized void idsAvailable(Context context, Listener listener) {
        new iZooto().start(context, listener);
    }

    protected void start(final Context context, final Listener listener) {
        if (listener == null) {
            Log.v(AppConstant.APP_NAME_TAG, "getAdvertisingId - Error: null listener, dropping call");
        } else {
            mHandler = new Handler(Looper.getMainLooper());
            mListener = listener;
            if (context == null) {
                invokeFail(new Exception(TAG + " - Error: context null"));
            } else {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
                        invokeFinish(preferenceUtil.getStringData(AppConstant.ADVERTISING_ID),preferenceUtil.getStringData(AppConstant.ENCRYPTED_PID));
                    }
                }).start();
            }
        }
    }


    public interface Listener {

        void idsAvailable(String adverID,String registrationID);

        void onAdvertisingIdClientFail(Exception exception);
    }

    protected static void invokeFinish(final String adverID, final String registrationID) {
        mHandler = new Handler(Looper.getMainLooper());
        mHandler.post(new Runnable() {
            @Override
            public void run() {

                if (mListener != null) {
                    mListener.idsAvailable(adverID,registrationID);
                }
            }
        });
    }

    protected static void invokeFail(final Exception exception) {
        mHandler = new Handler(Looper.getMainLooper());
        mHandler.post(new Runnable() {
            @Override
            public void run() {

                if (mListener != null) {
                    mListener.onAdvertisingIdClientFail(exception);
                }
            }
        });
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
 static void registerToken() {
    if(appContext!=null) {
        final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
        if (!preferenceUtil.getBoolean(AppConstant.IS_TOKEN_UPDATED)) {
            if (!preferenceUtil.getStringData(AppConstant.HMS_TOKEN).isEmpty() && !preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN).isEmpty() && !preferenceUtil.getStringData(AppConstant.XiaomiToken).isEmpty()) {
                preferenceUtil.setIntData(AppConstant.CLOUD_PUSH, 3);
            } else if (!preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN).isEmpty() && !preferenceUtil.getStringData(AppConstant.XiaomiToken).isEmpty()) {
                preferenceUtil.setIntData(AppConstant.CLOUD_PUSH, 2);
            } else if (!preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN).isEmpty() && !preferenceUtil.getStringData(AppConstant.HMS_TOKEN).isEmpty()) {
                preferenceUtil.setIntData(AppConstant.CLOUD_PUSH, 2);
            }
            else {
                preferenceUtil.setIntData(AppConstant.CLOUD_PUSH, 1);
            }
            try {
                if (!preferenceUtil.getStringData(AppConstant.HMS_TOKEN).isEmpty())
                    preferenceUtil.setBooleanData(AppConstant.IS_UPDATED_HMS_TOKEN, true);
                if (!preferenceUtil.getStringData(AppConstant.XiaomiToken).isEmpty())
                    preferenceUtil.setBooleanData(AppConstant.IS_UPDATED_XIAOMI_TOKEN, true);
                Map<String, String> mapData = new HashMap<>();
                mapData.put(AppConstant.ADDURL, "" + AppConstant.STYPE);
                mapData.put(AppConstant.PID, mIzooToAppId);
                mapData.put(AppConstant.BTYPE_, "" + AppConstant.BTYPE);
                mapData.put(AppConstant.DTYPE_, "" + AppConstant.DTYPE);
                mapData.put(AppConstant.TIMEZONE, "" + System.currentTimeMillis());
                mapData.put(AppConstant.APPVERSION, "" + Util.getSDKVersion(iZooto.appContext));
                mapData.put(AppConstant.OS, "" + AppConstant.SDKOS);
                mapData.put(AppConstant.ALLOWED_, "" + AppConstant.ALLOWED);
                mapData.put(AppConstant.ANDROID_ID, "" + Util.getAndroidId(appContext));
                mapData.put(AppConstant.CHECKSDKVERSION, "" + Util.getSDKVersion(appContext));
                mapData.put(AppConstant.LANGUAGE, "" + Util.getDeviceLanguage());
                mapData.put(AppConstant.QSDK_VERSION, "" + AppConstant.SDKVERSION);
                mapData.put(AppConstant.TOKEN, "" + preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN));
                mapData.put(AppConstant.ADVERTISEMENTID, "" + preferenceUtil.getStringData(AppConstant.ADVERTISING_ID));
                mapData.put(AppConstant.XIAOMITOKEN, "" + preferenceUtil.getStringData(AppConstant.XiaomiToken));
                mapData.put(AppConstant.PACKAGE_NAME, "" + appContext.getPackageName());
                mapData.put(AppConstant.SDKTYPE, "" + SDKDEF);
                mapData.put(AppConstant.KEY_HMS, "" + preferenceUtil.getStringData(AppConstant.HMS_TOKEN));
                mapData.put(AppConstant.ANDROIDVERSION, "" + Build.VERSION.RELEASE);
                mapData.put(AppConstant.DEVICENAME, "" + Util.getDeviceName());

                RestClient.newPostRequest(RestClient.BASE_URL, mapData, new RestClient.ResponseHandler() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    void onSuccess(final String response) {
                        super.onSuccess(response);
                        lastVisitApi(appContext);
                        if (mBuilder != null && mBuilder.mTokenReceivedListener != null) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        JSONObject jsonObject = new JSONObject();
                                        jsonObject.put(FCM_TOKEN_FROM_JSON, preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN));
                                        jsonObject.put(XIAOMI_TOKEN_FROM_JSON, preferenceUtil.getStringData(AppConstant.XiaomiToken));
                                        jsonObject.put(HUAWEI_TOKEN_FROM_JSON, preferenceUtil.getStringData(AppConstant.HMS_TOKEN));
                                        mBuilder.mTokenReceivedListener.onTokenReceived(jsonObject.toString());
                                    }
                                    catch (Exception ex)
                                    {
                                     Log.e("Exception","Invalid JSON");
                                    }

                                }
                            });
                        }
                        preferenceUtil.setBooleanData(AppConstant.IS_TOKEN_UPDATED, true);
                        preferenceUtil.setLongData(AppConstant.DEVICE_REGISTRATION_TIMESTAMP, System.currentTimeMillis());

                        try {
                            if (!preferenceUtil.getStringData(AppConstant.USER_LOCAL_DATA).isEmpty()) {
                                Util.sleepTime(5000);
                                JSONObject json  = new JSONObject(preferenceUtil.getStringData(AppConstant.USER_LOCAL_DATA));
                                addUserProperty(Util.toMap(json));
                            }
                            if (!preferenceUtil.getStringData(AppConstant.EVENT_LOCAL_DATA_EN).isEmpty() && !preferenceUtil.getStringData(AppConstant.EVENT_LOCAL_DATA_EV).isEmpty()) {
                                JSONObject json  = new JSONObject(preferenceUtil.getStringData(AppConstant.EVENT_LOCAL_DATA_EV));
                                addEvent(preferenceUtil.getStringData(AppConstant.EVENT_LOCAL_DATA_EN), Util.toMap(json));
                            }
                            if (preferenceUtil.getBoolean(AppConstant.IS_SET_SUBSCRIPTION_METHOD))
                                iZooto.setSubscription(preferenceUtil.getBoolean(AppConstant.SET_SUBSCRITION_LOCAL_DATA));

                        } catch (Exception e) {
                            Util.setException(appContext, e.toString(), "registerToken", AppConstant.APP_NAME_TAG);
                        }
                    }

                    @Override
                    void onFailure(int statusCode, String response, Throwable throwable) {
                        super.onFailure(statusCode, response, throwable);
                    }
                });

            } catch (Exception exception) {
                Util.setException(appContext, exception.toString(), AppConstant.APP_NAME_TAG, "registerToken");
            }
        } else {
            if (mBuilder != null && mBuilder.mTokenReceivedListener != null) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(FCM_TOKEN_FROM_JSON, preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN));
                    jsonObject.put(XIAOMI_TOKEN_FROM_JSON, preferenceUtil.getStringData(AppConstant.XiaomiToken));
                    jsonObject.put(HUAWEI_TOKEN_FROM_JSON, preferenceUtil.getStringData(AppConstant.HMS_TOKEN));
                    mBuilder.mTokenReceivedListener.onTokenReceived(jsonObject.toString());
                }
                catch (Exception ex)
                {
                    Log.e("Exception","Invalid JSON");
                }

            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                lastVisitApi(appContext);
            }

        }
    }

}

    static void onActivityResumed(Activity activity){
        final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
        setActivity(activity);
        if (!preferenceUtil.getBoolean(AppConstant.IS_NOTIFICATION_ID_UPDATED)) {
            if (firebaseAnalyticsTrack != null && preferenceUtil.getBoolean(AppConstant.FIREBASE_ANALYTICS_TRACK)) {
                firebaseAnalyticsTrack.influenceOpenTrack();
                preferenceUtil.setBooleanData(AppConstant.IS_NOTIFICATION_ID_UPDATED,true);
            }
        }

        try {
            ShortcutBadger.applyCountOrThrow(appContext, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static void setCurActivity(Context context) {
        boolean foreground = isContextActivity(context);
        if (foreground) {
            iZooto.curActivity = (Activity) context;
        }
    }

    private static boolean isContextActivity(Context context) {
        return context instanceof Activity;
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void processNotificationReceived(Payload payload) {
        if(payload!=null) {
            NotificationEventManager.manageNotification(payload);
        }

    }

    public static void notificationView(Payload payload)
    {
        final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
        if(payload!=null)
        {
            if(mBuilder!=null && mBuilder.mNotificationHelper!=null)
            {
                mBuilder.mNotificationHelper.onNotificationReceived(payload);
            }
            if (firebaseAnalyticsTrack != null && preferenceUtil.getBoolean(AppConstant.FIREBASE_ANALYTICS_TRACK))
            {
                firebaseAnalyticsTrack.receivedEventTrack(payload);
            }

            if (payload.getId() != null && !payload.getId().isEmpty()) {
                if (!payload.getId().equals(preferenceUtil.getStringData(AppConstant.TRACK_NOTIFICATION_ID))) {
                    preferenceUtil.setBooleanData(AppConstant.IS_NOTIFICATION_ID_UPDATED, false);
                }
                preferenceUtil.setStringData(AppConstant.TRACK_NOTIFICATION_ID, payload.getId());
            }

        }
    }
    public static void notificationActionHandler(String data)
    {
        final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
        if(mBuilder!=null && mBuilder.mNotificationHelper!=null)
        {
            mBuilder.mNotificationHelper.onNotificationOpened(data);
        }
        if (firebaseAnalyticsTrack != null && preferenceUtil.getBoolean(AppConstant.FIREBASE_ANALYTICS_TRACK)) {
            firebaseAnalyticsTrack.openedEventTrack();
        }
        try {
            preferenceUtil.setIntData(AppConstant.NOTIFICATION_COUNT,preferenceUtil.getIntData(AppConstant.NOTIFICATION_COUNT)-1);
            ShortcutBadger.applyCountOrThrow(appContext, preferenceUtil.getIntData(AppConstant.NOTIFICATION_COUNT));
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
    public static void notificationInAppAction(String url){
        if (mBuilder!=null && mBuilder.mWebViewListener!=null)
            mBuilder.mWebViewListener.onWebView(url);
    }

    /*
        Handle the Hybrid Web_View Listener
       */
    public static void notificationWebView(NotificationWebViewListener notificationWebViewListener){
        mBuilder.mWebViewListener = notificationWebViewListener;
        if(mBuilder.mWebViewListener!=null)
        {
            runNotificationWebViewCallback();
        }
    }

    private static void runNotificationWebViewCallback() {
        runOnMainUIThread(new Runnable() {
            public void run() {
                if (!NotificationActionReceiver.WebViewClick.isEmpty()) {
                    iZooto.mBuilder.mWebViewListener.onWebView(NotificationActionReceiver.WebViewClick);
                    NotificationActionReceiver.WebViewClick = "";
                }
            }
        });
    }

    /*
     Handle the Hybrid DeepLink  Listener
    */
    public static void notificationClick(NotificationHelperListener notificationOpenedListener)
    {
        mBuilder.mNotificationHelper = notificationOpenedListener;
        final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
        if(mBuilder.mNotificationHelper!=null)
        {
            runNotificationOpenedCallback();
        }
        if (firebaseAnalyticsTrack != null && preferenceUtil.getBoolean(AppConstant.FIREBASE_ANALYTICS_TRACK)) {
            firebaseAnalyticsTrack.openedEventTrack();
        }
        try {
            preferenceUtil.setIntData(AppConstant.NOTIFICATION_COUNT,preferenceUtil.getIntData(AppConstant.NOTIFICATION_COUNT)-1);
            ShortcutBadger.applyCountOrThrow(appContext, preferenceUtil.getIntData(AppConstant.NOTIFICATION_COUNT));
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private static void runNotificationOpenedCallback() {
        runOnMainUIThread(new Runnable() {
            public void run() {
                if (!NotificationActionReceiver.notificationClick.isEmpty()) {
                    iZooto.mBuilder.mNotificationHelper.onNotificationOpened(NotificationActionReceiver.notificationClick);
                    NotificationActionReceiver.notificationClick = "";
                }
            }
        });
    }

    // handle the execution
    static void runOnMainUIThread(Runnable runnable) {
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            runnable.run();
        } else {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(runnable);
        }

    }
    public static void notificationViewHybrid(String payloadList, Payload payload)
    {
        final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
        if(payload!=null)
        {
            if(mBuilder!=null && mBuilder.mNotificationReceivedHybridlistener !=null)
            {
                mBuilder.mNotificationReceivedHybridlistener.onNotificationReceivedHybrid(payloadList);
            }
            if (firebaseAnalyticsTrack != null && preferenceUtil.getBoolean(AppConstant.FIREBASE_ANALYTICS_TRACK)) {
                firebaseAnalyticsTrack.receivedEventTrack(payload);
            }else {
                if (FirebaseAnalyticsTrack.canFirebaseAnalyticsTrack() && preferenceUtil.getBoolean(AppConstant.FIREBASE_ANALYTICS_TRACK)) {
                    firebaseAnalyticsTrack = new FirebaseAnalyticsTrack(appContext);
                    firebaseAnalyticsTrack.receivedEventTrack(payload);
                }
            }
            if (payload.getId() != null && !payload.getId().isEmpty()) {
                if (!payload.getId().equals(preferenceUtil.getStringData(AppConstant.TRACK_NOTIFICATION_ID))) {
                    preferenceUtil.setBooleanData(AppConstant.IS_NOTIFICATION_ID_UPDATED, false);
                }
                preferenceUtil.setStringData(AppConstant.TRACK_NOTIFICATION_ID, payload.getId());
            }
        }
    }
    public static void notificationReceivedCallback(NotificationReceiveHybridListener notificationReceivedHybridListener){
        mBuilder.mNotificationReceivedHybridlistener = notificationReceivedHybridListener;
        if(mBuilder.mNotificationReceivedHybridlistener != null) {
            runNotificationReceivedCallback();
        }
    }
    private static void runNotificationReceivedCallback() {
        runOnMainUIThread(new Runnable() {
            public void run() {
                final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
                if (NotificationEventManager.iZootoReceivedPayload != null) {
                    iZooto.mBuilder.mNotificationReceivedHybridlistener.onNotificationReceivedHybrid(NotificationEventManager.iZootoReceivedPayload);
//                    NotificationEventManager.iZootoReceivedPayload;
                }
                if (!preferenceUtil.getBoolean(AppConstant.IS_NOTIFICATION_ID_UPDATED)) {
                    if (FirebaseAnalyticsTrack.canFirebaseAnalyticsTrack() && preferenceUtil.getBoolean(AppConstant.FIREBASE_ANALYTICS_TRACK)) {
                        firebaseAnalyticsTrack = new FirebaseAnalyticsTrack(appContext);
                        firebaseAnalyticsTrack.influenceOpenTrack();
                        preferenceUtil.setBooleanData(AppConstant.IS_NOTIFICATION_ID_UPDATED, true);
                    }
                }
            }
        });
    }
    public static class Builder {
        Context mContext;
        private TokenReceivedListener mTokenReceivedListener;
        private NotificationHelperListener mNotificationHelper;
        public NotificationWebViewListener mWebViewListener;
        public NotificationReceiveHybridListener mNotificationReceivedHybridlistener;

        OSInAppDisplayOption mDisplayOption;
        private Builder(Context context) {
            mContext = context;
        }

        public Builder setTokenReceivedListener(TokenReceivedListener listener) {
            mTokenReceivedListener = listener;
            return this;
        }
        public Builder setNotificationReceiveListener(NotificationHelperListener notificationHelper) {
            mNotificationHelper = notificationHelper;
            return this;
        }

        public Builder inAppNotificationBehaviour(OSInAppDisplayOption displayOption) {
            mDisplayOption = displayOption;
            inAppOption = displayOption.toString();
            return this;
        }
        public Builder setLandingURLListener(NotificationWebViewListener mNotificationWebViewListener){
            mWebViewListener = mNotificationWebViewListener;
            return this;

        }
        public Builder setNotificationReceiveHybridListener(NotificationReceiveHybridListener notificationReceivedHybrid) {
            mNotificationReceivedHybridlistener = notificationReceivedHybrid;
            return this;
        }



        public Builder unsubscribeWhenNotificationsAreDisabled(boolean set){
            mUnsubscribeWhenNotificationsAreDisabled = set;
            /*if (set){
                areNotificationsEnabledForSubscribedState(mContext);
            }*/
            return this;
        }


        public void build() {
            iZooto.init(this);
        }

    }

    private static void areNotificationsEnabledForSubscribedState(Context context){
        final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
        if (context!=null) {
            int value = 0;
            if (mUnsubscribeWhenNotificationsAreDisabled) {
                boolean isChecked = Util.isNotificationEnabled(context);
                if (!isChecked) {
                    value = 2;
                }
            }
            if (value==0 && preferenceUtil.getIntData(AppConstant.GET_NOTIFICATION_ENABLED)==0){
                preferenceUtil.setIntData(AppConstant.GET_NOTIFICATION_ENABLED,1);
                preferenceUtil.setIntData(AppConstant.GET_NOTIFICATION_DISABLED,0);
                getNotificationAPI(context,value);

            }else if (value==2 && preferenceUtil.getIntData(AppConstant.GET_NOTIFICATION_DISABLED)==0) {
                preferenceUtil.setIntData(AppConstant.GET_NOTIFICATION_DISABLED, 1);
                preferenceUtil.setIntData(AppConstant.GET_NOTIFICATION_ENABLED, 0);
                getNotificationAPI(context, value);

            }
        }

    }

    private static void getNotificationAPI(Context context,int value){

        if(context!=null) {
            final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
            try {
                if (!preferenceUtil.getiZootoID(AppConstant.APPPID).isEmpty() && Util.isNetworkAvailable(context)) {
                    if (!preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN).isEmpty() || !preferenceUtil.getStringData(AppConstant.HMS_TOKEN).isEmpty() || !preferenceUtil.getStringData(AppConstant.XiaomiToken).isEmpty()) {
                        Map<String, String> mapData = new HashMap<>();
                        mapData.put(AppConstant.PID, preferenceUtil.getiZootoID(AppConstant.APPPID));
                        mapData.put(AppConstant.ANDROID_ID, "" + Util.getAndroidId(context));
                        mapData.put(AppConstant.BTYPE_, "" + AppConstant.BTYPE);
                        mapData.put(AppConstant.DTYPE_, "" + AppConstant.DTYPE);
                        mapData.put(AppConstant.APPVERSION, "" + AppConstant.SDKVERSION);
                        mapData.put(AppConstant.PTE_, "" + AppConstant.PTE);
                        mapData.put(AppConstant.OS, "" + AppConstant.SDKOS);
                        mapData.put(AppConstant.PT_, "" + AppConstant.PT);
                        mapData.put(AppConstant.GE_, "" + AppConstant.GE);
                        mapData.put(AppConstant.ACTION, "" + value);

                        RestClient.newPostRequest(RestClient.SUBSCRIPTION_API, mapData, new RestClient.ResponseHandler() {
                            @Override
                            void onSuccess(final String response) {
                                super.onSuccess(response);
                            }

                            @Override
                            void onFailure(int statusCode, String response, Throwable throwable) {
                                super.onFailure(statusCode, response, throwable);
                            }
                        });
                    }
                }
            } catch (Exception ex) {
                Util.setException(iZooto.appContext, ex.toString(), AppConstant.APP_NAME_TAG, "getNotificationAPI");
            }
        }

    }


    public static void setInAppNotificationBehaviour(OSInAppDisplayOption displayOption) {
        inAppOption = displayOption.toString();
    }





    // send events  with event name and event data
    public static void addEvent(String eventName, HashMap<String,Object> data) {
        if (data != null && eventName != null&&eventName.length()>0&&data.size()>0) {
            eventName = eventName.substring(0, Math.min(eventName.length(), 32)).replace(" ","_");
            HashMap<String, Object>  newListEvent= new HashMap<String, Object>();
            for (Map.Entry<String,Object> refineEntry : data.entrySet()) {
                if (refineEntry.getKey()!=null&&!refineEntry.getKey().isEmpty()){
                    String newKey = refineEntry.getKey().toLowerCase();
                    newListEvent.put(newKey,refineEntry.getValue());
                }
            }
            if (newListEvent.size()>0)
                addEventAPI(eventName,newListEvent);
        }
    }
    private static void addEventAPI(String eventName,HashMap<String,Object> data){
        if(appContext!=null) {
            final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
            HashMap<String, Object> filterEventData = checkValidationEvent(data, 1);
            if (filterEventData.size() > 0) {
                try {
                    JSONObject jsonObject = new JSONObject(filterEventData);
                    if (!preferenceUtil.getiZootoID(AppConstant.APPPID).isEmpty() && Util.isNetworkAvailable(appContext)) {
                        if (!preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN).isEmpty() || !preferenceUtil.getStringData(AppConstant.HMS_TOKEN).isEmpty() || !preferenceUtil.getStringData(AppConstant.XiaomiToken).isEmpty()) {
                            Map<String, String> mapData = new HashMap<>();
                            mapData.put(AppConstant.PID, preferenceUtil.getiZootoID(AppConstant.APPPID));
                            mapData.put(AppConstant.ACT, eventName);
                            mapData.put(AppConstant.ET_, "evt");
                            mapData.put(AppConstant.ANDROID_ID, "" + Util.getAndroidId(appContext));
                            mapData.put(AppConstant.VAL, "" + jsonObject.toString());
                            RestClient.newPostRequest(RestClient.EVENT_URL, mapData, new RestClient.ResponseHandler() {
                                @Override
                                void onSuccess(final String response) {
                                    super.onSuccess(response);
                                    preferenceUtil.setStringData(AppConstant.EVENT_LOCAL_DATA_EN, null);
                                    preferenceUtil.setStringData(AppConstant.EVENT_LOCAL_DATA_EV, null);
                                }

                                @Override
                                void onFailure(int statusCode, String response, Throwable throwable) {
                                    super.onFailure(statusCode, response, throwable);
                                }
                            });
                        } else {
                            JSONObject jsonObjectLocal = new JSONObject(data);
                            preferenceUtil.setStringData(AppConstant.EVENT_LOCAL_DATA_EN, eventName);
                            preferenceUtil.setStringData(AppConstant.EVENT_LOCAL_DATA_EV, jsonObjectLocal.toString());
                        }
                    } else {
                        JSONObject jsonObjectLocal = new JSONObject(data);
                        preferenceUtil.setStringData(AppConstant.EVENT_LOCAL_DATA_EN, eventName);
                        preferenceUtil.setStringData(AppConstant.EVENT_LOCAL_DATA_EV, jsonObjectLocal.toString());
                    }
                }
                catch (Exception ex)
                {

                }
            }  else {
                Log.v(AppConstant.APP_NAME_TAG, "Event length more than 32...");
            }
        }
    }

    private static HashMap<String, Object> checkValidationEvent(HashMap<String, Object> data,int index){
        HashMap<String, Object>  newList= new HashMap<String, Object>();
        for (HashMap.Entry<String,Object> array:data.entrySet()) {
            if (index<=16){
                String newKey = array.getKey().substring(0, Math.min(array.getKey().length(), 32));
                if (array.getValue() instanceof String){
                    if (array.getValue().toString().length()>0) {
                        String newValue = array.getValue().toString().substring(0, Math.min(array.getValue().toString().length(), 64));
                        newList.put(newKey, newValue);
                        index++;
                    }
                } else if (!(array.getValue() instanceof String)&&array.getValue()!=null){
                    newList.put(newKey, ( array.getValue()));
                    index ++;
                }
            }
        }
        return newList;
    }
    public static void addUserProperty(HashMap<String, Object> object) {
        if (appContext == null)
            return;

        final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);

        try {
            if (object != null && object.size()>0) {
                HashMap<String, Object>  newListUserProfile = new HashMap<String, Object>();
                for (Map.Entry<String,Object> refineEntry : object.entrySet()) {
                    if (refineEntry.getKey()!=null&&!refineEntry.getKey().isEmpty()){
                        String newKey = refineEntry.getKey().toLowerCase();
                        newListUserProfile.put(newKey,refineEntry.getValue());
                    }
                }
                if (newListUserProfile.size()>0) {
                    HashMap<String, Object> filterUserPropertyData = checkValidationUserProfile(newListUserProfile, 1);
                    if (filterUserPropertyData.size() > 0) {
                        JSONObject jsonObject = new JSONObject(filterUserPropertyData);

                        if (!preferenceUtil.getiZootoID(AppConstant.APPPID).isEmpty() && Util.isNetworkAvailable(appContext)) {
                            if (!preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN).isEmpty() || !preferenceUtil.getStringData(AppConstant.HMS_TOKEN).isEmpty() || !preferenceUtil.getStringData(AppConstant.XiaomiToken).isEmpty()) {
                                Map<String, String> mapData = new HashMap<>();
                                mapData.put(AppConstant.PID, preferenceUtil.getiZootoID(AppConstant.APPPID));
                                mapData.put(AppConstant.ACT, "add");
                                mapData.put(AppConstant.ET_, "" + AppConstant.USERP_);
                                mapData.put(AppConstant.ANDROID_ID, "" + Util.getAndroidId(appContext));
                                mapData.put(AppConstant.VAL, "" + jsonObject.toString());

                                RestClient.newPostRequest(RestClient.PROPERTIES_URL, mapData, new RestClient.ResponseHandler() {
                                    @Override
                                    void onSuccess(final String response) {
                                        super.onSuccess(response);
                                        preferenceUtil.setStringData(AppConstant.USER_LOCAL_DATA, null);
                                    }

                                    @Override
                                    void onFailure(int statusCode, String response, Throwable throwable) {
                                        super.onFailure(statusCode, response, throwable);
                                    }
                                });
                            } else {
                                JSONObject jsonObjectLocal = new JSONObject(object);
                                preferenceUtil.setStringData(AppConstant.USER_LOCAL_DATA, jsonObjectLocal.toString());
                            }
                        } else {
                            JSONObject jsonObjectLocal = new JSONObject(object);
                            preferenceUtil.setStringData(AppConstant.USER_LOCAL_DATA, jsonObjectLocal.toString());
                        }
                    }

                }
            }
        } catch (Exception e) {
            Util.setException(appContext, e.toString(), "addUserProperty", AppConstant.APP_NAME_TAG);
        }
    }
    public static void  setNotificationSound(String soundName)
    {
        soundID = soundName;
    }
    private static HashMap<String, Object> checkValidationUserProfile(HashMap<String, Object> data,int index){
        HashMap<String, Object>  newList= new HashMap<String, Object>();
        int indexForValue = 1;
        for (HashMap.Entry<String,Object> array:data.entrySet()) {
            if (index<=64){
                String newKey = array.getKey().substring(0, Math.min(array.getKey().length(), 32));
                if (array.getValue() instanceof String){
                    if (array.getValue().toString().length()>0) {
                        String newValue = array.getValue().toString().substring(0, Math.min(array.getValue().toString().length(), 64));
                        newList.put(newKey, newValue);
                        index++;
                    }
                } else if (array.getValue() instanceof List) {
                    List<Object> newvalueListDta = (List<Object>) array.getValue();
                    List<Object> newvalueList = new ArrayList<Object>();
                    for(Object obj: newvalueListDta) {
                        if (indexForValue<=64){
                            if (obj instanceof String){
                                String ListData = obj.toString();
                                if (indexForValue<=64&&ListData.length()>0){
                                    String newListValue = ListData.substring(0, Math.min(ListData.length(), 64));
                                    newvalueList.add(newListValue);
                                    indexForValue ++;
                                }
                            }else if (!(obj instanceof String)&&obj!=null){
                                newvalueList.add(obj);
                                indexForValue ++;
                            }
                        }
                    }
                    newList.put(newKey, newvalueList);
                    index ++;
                }else if (!(array.getValue() instanceof String)&&!(array.getValue() instanceof List)&&array.getValue()!=null){
                    newList.put(newKey, ( array.getValue()));
                    index ++;
                }
            }
        }
        return newList;
    }
    public static void setIcon(int icon1)
    {
        icon=icon1;
    }

    public static void setSubscription(Boolean enable) {
        if (appContext == null)
            return;

        final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);

        try {
            int value = 2;
            if (enable != null) {
                if (enable) {
                    value = 0;
                }

                if (!preferenceUtil.getiZootoID(AppConstant.APPPID).isEmpty() && Util.isNetworkAvailable(appContext)) {
                    if (!preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN).isEmpty() || !preferenceUtil.getStringData(AppConstant.HMS_TOKEN).isEmpty() || !preferenceUtil.getStringData(AppConstant.XiaomiToken).isEmpty()) {
                        Map<String, String> mapData = new HashMap<>();
                        mapData.put(AppConstant.PID, preferenceUtil.getiZootoID(AppConstant.APPPID));
                        mapData.put(AppConstant.ANDROID_ID, "" + Util.getAndroidId(appContext));
                        mapData.put(AppConstant.BTYPE_, "" + AppConstant.BTYPE);
                        mapData.put(AppConstant.DTYPE_, "" + AppConstant.DTYPE);
                        mapData.put(AppConstant.APPVERSION, "" + Util.getSDKVersion(iZooto.appContext));
                        mapData.put(AppConstant.PTE_, "" + AppConstant.PTE);
                        mapData.put(AppConstant.OS, "" + AppConstant.SDKOS);
                        mapData.put(AppConstant.PT_, "" + AppConstant.PT);
                        mapData.put(AppConstant.GE_, "" + AppConstant.GE);
                        mapData.put(AppConstant.ACTION, "" + value);

                        RestClient.newPostRequest(RestClient.SUBSCRIPTION_API, mapData, new RestClient.ResponseHandler() {
                            @Override
                            void onSuccess(final String response) {
                                super.onSuccess(response);
                                preferenceUtil.setBooleanData(AppConstant.IS_SET_SUBSCRIPTION_METHOD, false);
                            }

                            @Override
                            void onFailure(int statusCode, String response, Throwable throwable) {
                                super.onFailure(statusCode, response, throwable);
                            }
                        });
                    } else {
                        preferenceUtil.setBooleanData(AppConstant.IS_SET_SUBSCRIPTION_METHOD, true);
                        preferenceUtil.setBooleanData(AppConstant.SET_SUBSCRITION_LOCAL_DATA, enable);
                    }
                } else {
                    preferenceUtil.setBooleanData(AppConstant.IS_SET_SUBSCRIPTION_METHOD, true);
                    preferenceUtil.setBooleanData(AppConstant.SET_SUBSCRITION_LOCAL_DATA, enable);
                }

            }
        }catch (Exception e) {
            Util.setException(appContext, e.toString(), "setSubscription", AppConstant.APP_NAME_TAG);
        }

    }

    public static void setFirebaseAnalytics(boolean isSet){
        final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
        preferenceUtil.setBooleanData(AppConstant.FIREBASE_ANALYTICS_TRACK,isSet);
    }

    public static void iZootoHandleNotification(Context context,final Map<String,String> data)
    {
        Log.d(AppConstant.APP_NAME_TAG, AppConstant.NOTIFICATIONRECEIVED);
        try {
            final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);

            if(data.get(AppConstant.AD_NETWORK) !=null && data.get(AppConstant.GLOBAL)!=null)
            {
                AdMediation.getAdJsonData(context,data);
                preferenceUtil.setBooleanData(AppConstant.MEDIATION,true);

            }
            else {
                preferenceUtil.setBooleanData(AppConstant.MEDIATION, false);
                JSONObject payloadObj = new JSONObject(data);
                if (payloadObj.optLong(ShortpayloadConstant.CREATEDON) > PreferenceUtil.getInstance(iZooto.appContext).getLongValue(AppConstant.DEVICE_REGISTRATION_TIMESTAMP)) {
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

            // return;
        } catch (Exception e) {

            e.printStackTrace();
            Lg.d(TAG, e.toString());
        }


        if (iZooto.appContext == null)
            iZooto.appContext = context;
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

    public static void addTag(final List<String> topicName){
        if (topicName != null && !topicName.isEmpty()) {
            final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
            if (preferenceUtil.getStringData(AppConstant.SENDERID) != null) {
                FirebaseOptions firebaseOptions =
                        new FirebaseOptions.Builder()
                                .setGcmSenderId(preferenceUtil.getStringData(AppConstant.SENDERID)) //senderID
                                .setApplicationId(get_App_ID()) //application ID
                                .setApiKey(getAPI_KEY()) //Application Key
                                .setProjectId(get_Project_ID()) //Project ID
                                .build();
                try {
                    FirebaseApp firebaseApp = FirebaseApp.getInstance(AppConstant.FCMDEFAULT);
                    if (firebaseApp == null) {
                        FirebaseApp.initializeApp(appContext, firebaseOptions, AppConstant.FCMDEFAULT);
                    }
                } catch (IllegalStateException ex) {
                    FirebaseApp.initializeApp(appContext, firebaseOptions, AppConstant.FCMDEFAULT);
                }
                List<String> topicList = new ArrayList<String>();
                for (final String filterTopicName : topicName) {
                    if (filterTopicName != null && !filterTopicName.isEmpty()) {
                        if (Util.isMatchedString(filterTopicName)){
                            try {
                                FirebaseMessaging.getInstance().subscribeToTopic(filterTopicName);
                                preferenceUtil.setStringData(AppConstant.GET_TOPIC_NAME, filterTopicName);
                                topicList.add(filterTopicName);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                topicApi(AppConstant.ADD_TOPIC, topicList);
            }
        }
    }
    public static void removeTag(final List<String> topicName){
        if (topicName != null && !topicName.isEmpty()) {
            final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
            if (preferenceUtil.getStringData(AppConstant.SENDERID) != null ) {

                FirebaseOptions firebaseOptions =
                        new FirebaseOptions.Builder()
                                .setGcmSenderId(preferenceUtil.getStringData(AppConstant.SENDERID)) //senderID
                                .setApplicationId(get_App_ID()) //application ID
                                .setApiKey(getAPI_KEY()) //Application Key
                                .setProjectId(get_Project_ID()) //Project ID
                                .build();
                try {
                    FirebaseApp firebaseApp = FirebaseApp.getInstance(AppConstant.FCMDEFAULT);
                    if (firebaseApp == null) {
                        FirebaseApp.initializeApp(appContext, firebaseOptions, AppConstant.FCMDEFAULT);
                    }
                } catch (IllegalStateException ex) {
                    FirebaseApp.initializeApp(appContext, firebaseOptions, AppConstant.FCMDEFAULT);
                }
                List<String> topicList = new ArrayList<String>();
                for (final String filterTopicName : topicName) {
                    if (filterTopicName != null && !filterTopicName.isEmpty()) {
                        if (Util.isMatchedString(filterTopicName)) {
                            try {
                                FirebaseMessaging.getInstance().unsubscribeFromTopic(filterTopicName);
                                preferenceUtil.setStringData(AppConstant.REMOVE_TOPIC_NAME, filterTopicName);
                                topicList.add(filterTopicName);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                topicApi(AppConstant.REMOVE_TOPIC, topicList);
            }
        }
    }
    private static void topicApi(String action, List<String> topic){
        if(appContext!=null) {
            if (topic.size() > 0) {
                final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
                if (!preferenceUtil.getiZootoID(AppConstant.APPPID).isEmpty() && !preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN).isEmpty()) {



                    try {
                        HashMap<String, List<String>> data = new HashMap<>();
                        data.put(AppConstant.TOPIC, topic);
                        JSONObject jsonObject = new JSONObject(data);
                        Map<String, String> mapData = new HashMap<>();
                        mapData.put(AppConstant.PID, preferenceUtil.getiZootoID(AppConstant.APPPID));
                        mapData.put(AppConstant.ACT, action);
                        mapData.put(AppConstant.ET_, "" + AppConstant.USERP_);
                        mapData.put(AppConstant.ANDROID_ID, "" + Util.getAndroidId(appContext));
                        mapData.put(AppConstant.VAL, "" + jsonObject.toString());
                        mapData.put(AppConstant.TOKEN, "" + preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN));
                        mapData.put(AppConstant.BTYPE_, "" + AppConstant.BTYPE);
                        RestClient.newPostRequest(RestClient.PROPERTIES_URL, mapData, new RestClient.ResponseHandler() {
                            @Override
                            void onSuccess(final String response) {
                                super.onSuccess(response);
                            }

                            @Override
                            void onFailure(int statusCode, String response, Throwable throwable) {
                                super.onFailure(statusCode, response, throwable);
                            }
                        });
                    } catch (Exception ex) {
                        Util.setException(iZooto.appContext, ex.toString(), AppConstant.APP_NAME_TAG, "topicAPI");
                    }
                }
            }
        }
    }
    private static String  getAPI_KEY()
    {
        try {
            String apiKey = FirebaseOptions.fromResource(iZooto.appContext).getApiKey();
            if (apiKey != null)
                return apiKey;
        }
        catch (Exception e)
        {
            return "";//new String(Base64.decode(FCM_DEFAULT_API_KEY_BASE64, Base64.DEFAULT));

        }
        return "";


    }
    private  static String get_App_ID() {
        try {
            String application_id = FirebaseOptions.fromResource(iZooto.appContext).getApplicationId();
            if (application_id!=null)
                return application_id;
        }
        catch (Exception ex)
        {
            return "";//FCM_DEFAULT_APP_ID;

        }
        return "";

    }
    private  static String get_Project_ID()
    {
        try {
            String project_id = FirebaseOptions.fromResource(iZooto.appContext).getProjectId();
            if(project_id!=null)
                return project_id;
        }
        catch (Exception exception)
        {
            return "";

        }
        return "";

    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    static void lastVisitApi(Context context){
        if(context!=null) {
            final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
            String time = preferenceUtil.getStringData(AppConstant.CURRENT_DATE);
            if (!time.equalsIgnoreCase(getTime())) {
                preferenceUtil.setStringData(AppConstant.CURRENT_DATE, getTime());
                try {
                    HashMap<String, Object> data = new HashMap<>();
                    data.put(AppConstant.LAST_WEBSITE_VISIT, true);
                    data.put(AppConstant.LANG_, Util.getDeviceLanguageTag());
                    JSONObject jsonObject = new JSONObject(data);
                    Map<String, String> mapData = new HashMap<>();
                    mapData.put(AppConstant.PID, preferenceUtil.getiZootoID(AppConstant.APPPID));
                    mapData.put(AppConstant.ANDROID_ID, "" + Util.getAndroidId(appContext));
                    mapData.put(AppConstant.VAL, "" + jsonObject.toString());
                    mapData.put(AppConstant.ACT, "add");
                    mapData.put(AppConstant.ISID_, "1");
                    mapData.put(AppConstant.ET_, "" + AppConstant.USERP_);
                    RestClient.newPostRequest(RestClient.LASTVISITURL, mapData, new RestClient.ResponseHandler() {
                        @Override
                        void onSuccess(final String response) {
                            super.onSuccess(response);
                        }

                        @Override
                        void onFailure(int statusCode, String response, Throwable throwable) {
                            super.onFailure(statusCode, response, throwable);
                        }
                    });
                } catch (Exception ex) {
                    Util.setException(context, ex.toString(), AppConstant.APP_NAME_TAG, "lastVisitAPI");


                }
            }
        }
    }
    private static String getTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
        String currentDate = sdf.format(new Date());
        return currentDate;
    }
    public static void setDefaultTemplate(int templateID){
        if(appContext!=null) {
            final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(appContext);
            preferenceUtil.setIntData(AppConstant.NOTIFICATION_PREVIEW, templateID);
        }
        else
        {
            Log.e(AppConstant.APP_NAME_TAG,"Current object is not pass here");
        }
    }

    public static void setDefaultNotificationBanner(int setBanner){
        bannerImage = setBanner;
    }
    public static iZooto.Builder initialize(Context context, String tokenJson) {
        if (context == null)
            return null;

        try {
            if (tokenJson !=null && !tokenJson.isEmpty()) {
                if (isJSONValid(tokenJson)) {

                    PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
                    JSONObject data = new JSONObject(tokenJson);

                    String fcmToken = data.optString(AppConstant.FCM_TOKEN_FROM_JSON);
                    String xiaomiToken = data.optString(AppConstant.XIAOMI_TOKEN_FROM_JSON);
                    String huaweiToken = data.optString(AppConstant.HUAWEI_TOKEN_FROM_JSON);

                    if (data.has(AppConstant.HUAWEI_TOKEN_FROM_JSON)) {
                        if (!huaweiToken.isEmpty()) {
                            if (Build.MANUFACTURER.equalsIgnoreCase("Huawei")) {
                                if (!huaweiToken.equals(preferenceUtil.getStringData(AppConstant.HMS_TOKEN))) {
                                    preferenceUtil.setBooleanData(AppConstant.CAN_GENERATE_HUAWEI_TOKEN, true);
                                    preferenceUtil.setBooleanData(AppConstant.IS_TOKEN_UPDATED, false);
                                    preferenceUtil.setBooleanData(AppConstant.IS_UPDATED_HMS_TOKEN, true);
                                    preferenceUtil.setStringData(AppConstant.HMS_TOKEN, huaweiToken);
                                }
                            }
                        } else {
                            Util.setException(context, "Please put huawei token...", "initialize", AppConstant.APP_NAME_TAG);
                        }
                    }
                    preferenceUtil.setBooleanData(AppConstant.CAN_GENERATE_FCM_TOKEN, true);
                    if (data.has(AppConstant.FCM_TOKEN_FROM_JSON)) {
                        if (!fcmToken.isEmpty()) {
                            if (!fcmToken.equals(preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN))) {
                                preferenceUtil.setBooleanData(AppConstant.IS_TOKEN_UPDATED, false);
                                preferenceUtil.setStringData(AppConstant.FCM_DEVICE_TOKEN, fcmToken);
                            }
                        } else {
                            Util.setException(context, "Please put fcm token...", "initialize", AppConstant.APP_NAME_TAG);
                        }
                    }

                    if (data.has(AppConstant.XIAOMI_TOKEN_FROM_JSON)) {
                        if (!xiaomiToken.isEmpty()) {
                            if (!xiaomiToken.equals(preferenceUtil.getStringData(AppConstant.XiaomiToken))) {
                                preferenceUtil.setBooleanData(AppConstant.CAN_GENERATE_XIAOMI_TOKEN, true);
                                preferenceUtil.setBooleanData(AppConstant.IS_TOKEN_UPDATED, false);
                                preferenceUtil.setBooleanData(AppConstant.IS_UPDATED_XIAOMI_TOKEN, true);
                                preferenceUtil.setStringData(AppConstant.XiaomiToken, xiaomiToken);
                            }
                        } else {
                            Util.setException(context, "Please put xiaomi token...", "initialize", AppConstant.APP_NAME_TAG);
                        }
                    }
                    return new iZooto.Builder(context);

                }
                else
                {
                    Log.e("iZooto","Given String is Not Valid JSON String");

                }

            }

        } catch (Exception e) {
            Util.setException(context, e.toString(), "initialize", AppConstant.APP_NAME_TAG);
            e.printStackTrace();
        }
        return null;

    }
    public static boolean isJSONValid(String targetJson) {
        try {
            new JSONObject(targetJson);
        } catch (JSONException ex) {
            try {
                new JSONArray(targetJson);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }
}
