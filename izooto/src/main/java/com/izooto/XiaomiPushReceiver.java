package com.izooto;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.xiaomi.mipush.sdk.MiPushCommandMessage;
import com.xiaomi.mipush.sdk.MiPushMessage;
import com.xiaomi.mipush.sdk.PushMessageReceiver;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class XiaomiPushReceiver extends PushMessageReceiver {
    private String TAG="XiaomiPushReceiver  PAYLOAD";
    private Payload payload;
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onReceivePassThroughMessage(Context context, MiPushMessage miPushMessage) {
        super.onReceivePassThroughMessage(context, miPushMessage);
        String payload = miPushMessage.getContent();
        Log.v("Push Type","Xiaomi");
        Log.v("Xiaomi payload",payload);

        if(payload!=null && !payload.isEmpty())
         handleNow(context,payload);

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onNotificationMessageArrived(Context context, MiPushMessage miPushMessage) {
        super.onNotificationMessageArrived(context, miPushMessage);
        Log.v(TAG, miPushMessage.getContent());
         miPushMessage.getMessageId();
        String payload = miPushMessage.getContent();
        if(payload!=null && !payload.isEmpty())
            handleNow(context,payload);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void handleNow(Context context, String data) {
        Log.d(TAG, AppConstant.NOTIFICATIONRECEIVED);
        try {

                  PreferenceUtil preferenceUtil =PreferenceUtil.getInstance(context);

                    JSONObject payloadObj = new JSONObject(data);
                    if(payloadObj.has(AppConstant.AD_NETWORK) && payloadObj.has(AppConstant.GLOBAL))
                    {
                       AdMediation.getAdJsonXiaomi(payloadObj);
                        //JSONArray jsonArray=payloadObj.getJSONArray(AppConstant.AD_NETWORK);
                       // JSONObject jsonObject=payloadObj.getJSONObject(AppConstant.GLOBAL);
                       // Log.e("AD",jsonArray.toString());
                       // Log.e("Global",jsonObject.toString());
                        preferenceUtil.setBooleanData(AppConstant.MEDIATION,true);
                    }
                    else {
                        preferenceUtil.setBooleanData(AppConstant.MEDIATION,false);
                        if (payloadObj.optLong(ShortpayloadConstant.CREATEDON) > PreferenceUtil.getInstance(context).getLongValue(AppConstant.DEVICE_REGISTRATION_TIMESTAMP)) {
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
                            payload.setPush_type(AppConstant.PUSH_XIAOMI);
                        }
                        else
                            return;
                    }

            } catch (Exception e) {

                e.printStackTrace();
                Lg.d(TAG, e.toString());
            }


            if (iZooto.appContext == null)
                iZooto.appContext = context;
            Handler mainHandler = new Handler(Looper.getMainLooper());
            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    iZooto.processNotificationReceived(payload);

                } // This is your code
            };
            mainHandler.post(myRunnable);



    }
    @Override
    public void onReceiveRegisterResult(Context context, MiPushCommandMessage miPushCommandMessage) {
        super.onReceiveRegisterResult(context, miPushCommandMessage);
    }

    @Override
    public void onCommandResult(Context context, MiPushCommandMessage miPushCommandMessage) {
        super.onCommandResult(context, miPushCommandMessage);
        try {
            PreferenceUtil preferenceUtils = PreferenceUtil.getInstance(context);
            preferenceUtils.setStringData(AppConstant.XiaomiToken, miPushCommandMessage.getCommandArguments().toString().replace("[", "").replace("]", ""));
            Log.i(AppConstant.XiaomiToken, miPushCommandMessage.getCommandArguments().toString().replace("[", "").replace("]", ""));
        }
        catch (Exception ex)
        {

        }
    }

    private void registrationXiaomi(String xiaomiToken,Context context) {
        Log.e("App","Xiaomi");
        final PreferenceUtil preferenceUtils = PreferenceUtil.getInstance(context);
            String api_url = AppConstant.ADDURL + AppConstant.STYPE + AppConstant.PID + preferenceUtils.getiZootoID(AppConstant.APPPID) + AppConstant.BTYPE_ + AppConstant.BTYPE + AppConstant.DTYPE_ + AppConstant.DTYPE + AppConstant.TIMEZONE + System.currentTimeMillis() + AppConstant.APPVERSION + Util.getSDKVersion(iZooto.appContext) +
                    AppConstant.OS + AppConstant.SDKOS + AppConstant.ALLOWED_ + AppConstant.ALLOWED + AppConstant.ANDROID_ID + Util.getAndroidId(context) + AppConstant.CHECKSDKVERSION + Util.getSDKVersion(iZooto.appContext) + AppConstant.LANGUAGE + Util.getDeviceLanguage() + AppConstant.QSDK_VERSION + AppConstant.SDKVERSION +
                    AppConstant.TOKEN +preferenceUtils.getStringData(AppConstant.FCM_DEVICE_TOKEN) + AppConstant.ADVERTISEMENTID + preferenceUtils.getStringData(AppConstant.ADVERTISING_ID) + AppConstant.XIAOMITOKEN + xiaomiToken + AppConstant.PACKAGE_NAME + context.getPackageName() + AppConstant.SDKTYPE + iZooto.SDKDEF;
            try {
                String deviceName = URLEncoder.encode(Util.getDeviceName(), AppConstant.UTF);
                String osVersion = URLEncoder.encode(Build.VERSION.RELEASE, AppConstant.UTF);
                api_url += AppConstant.ANDROIDVERSION + osVersion + AppConstant.DEVICENAME + deviceName;
            } catch (UnsupportedEncodingException e) {
                Lg.e(AppConstant.APP_NAME_TAG, AppConstant.UNEXCEPTION);
            }
            RestClient.get(api_url, new RestClient.ResponseHandler() {
                @Override
                void onSuccess(final String response) {
                    super.onSuccess(response);
                    preferenceUtils.setBooleanData(AppConstant.CHECK_XIAOMI, true);

                }

                @Override
                void onFailure(int statusCode, String response, Throwable throwable) {
                    super.onFailure(statusCode, response, throwable);

                }
            });

    }
}

