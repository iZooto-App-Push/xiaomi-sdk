package com.izooto;



import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.service.notification.StatusBarNotification;
import android.util.Base64;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.izooto.shortcutbadger.ShortcutBadger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

public class NotificationEventManager {
    private static Bitmap notificationIcon, notificationBanner;//,act1Icon,act2Icon;
    private static int icon;
    private static  int badgeColor;
    private static int priority,lockScreenVisibility;
    private static boolean addCheck;
    private static String lastView_Click ="0";
    private static boolean isCheck;
    public static String iZootoReceivedPayload;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void manageNotification(Payload payload) {
        if (payload.getFetchURL() == null || payload.getFetchURL().isEmpty()) {
            addCheck = false;
                allCloudPush(payload);
            }
        else{
                addCheck = true;
                allAdPush(payload);
            }

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static void allAdPush(Payload payload) {
      if(iZooto.appContext!=null) {
          try {
              PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(iZooto.appContext);
              if (preferenceUtil.getIntData(AppConstant.CLOUD_PUSH) == 1) {
                  if (preferenceUtil.getBoolean(AppConstant.MEDIATION)) {
                      showNotification(payload);
                  } else {
                      processPayload(payload);
                  }
              } else {
                  try {
                      String data = preferenceUtil.getStringData(AppConstant.NOTIFICATION_DUPLICATE);
                      JSONObject jsonObject = new JSONObject();
                      if (!data.isEmpty()) {
                          JSONArray jsonArray1 = new JSONArray(data);
                          if (jsonArray1.length() > 150) {
                              long currentTime = System.currentTimeMillis(); //fetch start time
                              for (int i = 0; i < jsonArray1.length(); i++) {
                                  JSONObject jsonObject2 = jsonArray1.getJSONObject(i);
                                  if ((currentTime - (Long.parseLong(jsonObject2.getString(AppConstant.CHECK_CREATED_ON)))) > Long.parseLong(payload.getTime_to_live())) {
                                      jsonArray1.remove(i);
                                  } else {
                                      if (i < 10) {
                                          jsonArray1.remove(i);
                                      }
                                  }
                              }
                              preferenceUtil.setStringData(AppConstant.NOTIFICATION_DUPLICATE, jsonArray1.toString());
                          } else {
                              if (jsonArray1.length() > 0) {
                                  for (int index = 0; index < jsonArray1.length(); index++) {
                                      JSONObject jsonObject1 = jsonArray1.getJSONObject(index);
                                      if (jsonObject1.getString(AppConstant.CHECK_CREATED_ON).equalsIgnoreCase(payload.getCreated_Time()) && jsonObject1.getString(AppConstant.CHECK_RID).equalsIgnoreCase(payload.getRid())) {
                                          isCheck = true;
                                          if (jsonObject1.getString(AppConstant.Check_Notification).equalsIgnoreCase(AppConstant.YES)) {
                                              jsonArray1.remove(index);
                                          } else {
                                              jsonArray1.remove(index);
                                              jsonObject.put(AppConstant.CHECK_CREATED_ON, payload.getCreated_Time());
                                              jsonObject.put(AppConstant.CHECK_RID, payload.getRid());
                                              jsonObject.put(AppConstant.CHECK_TTL, payload.getTime_to_live());
                                              jsonObject.put(AppConstant.Check_Notification, AppConstant.Check_YES);
                                              jsonArray1.put(jsonObject);
                                          }
                                          break;
                                      } else {
                                          isCheck = false;
                                      }
                                  }

                                  if (isCheck) {
                                      preferenceUtil.setStringData(AppConstant.NOTIFICATION_DUPLICATE, jsonArray1.toString());

                                  } else {
                                      if (preferenceUtil.getBoolean(AppConstant.MEDIATION)) {
                                          showNotification(payload);
                                      } else {
                                          processPayload(payload);
                                      }
                                      jsonObject.put(AppConstant.CHECK_CREATED_ON, payload.getCreated_Time());
                                      jsonObject.put(AppConstant.CHECK_RID, payload.getRid());
                                      jsonObject.put(AppConstant.CHECK_TTL, payload.getTime_to_live());
                                      jsonObject.put(AppConstant.Check_Notification, AppConstant.Check_NO);
                                      jsonArray1.put(jsonObject);
                                      preferenceUtil.setStringData(AppConstant.NOTIFICATION_DUPLICATE, jsonArray1.toString());
                                  }
                              } else {
                                  jsonObject.put(AppConstant.CHECK_CREATED_ON, payload.getCreated_Time());
                                  jsonObject.put(AppConstant.CHECK_RID, payload.getRid());
                                  jsonObject.put(AppConstant.CHECK_TTL, payload.getTime_to_live());
                                  jsonObject.put(AppConstant.Check_Notification, AppConstant.Check_NO);
                                  jsonArray1.put(jsonObject);
                                  preferenceUtil.setStringData(AppConstant.NOTIFICATION_DUPLICATE, jsonArray1.toString());
                                  if (preferenceUtil.getBoolean(AppConstant.MEDIATION)) {
                                      showNotification(payload);
                                  } else {
                                      processPayload(payload);
                                  }
                              }
                          }
                      } else {
                          JSONArray jsonArray = new JSONArray();
                          jsonObject.put(AppConstant.CHECK_CREATED_ON, payload.getCreated_Time());
                          jsonObject.put(AppConstant.CHECK_RID, payload.getRid());
                          jsonObject.put(AppConstant.CHECK_TTL, payload.getTime_to_live());
                          jsonObject.put(AppConstant.Check_Notification, AppConstant.Check_NO);
                          jsonArray.put(jsonObject);
                          preferenceUtil.setStringData(AppConstant.NOTIFICATION_DUPLICATE, jsonArray.toString());
                          if (preferenceUtil.getBoolean(AppConstant.MEDIATION)) {
                              showNotification(payload);
                          } else {
                              processPayload(payload);
                          }
                          preferenceUtil.setStringData(AppConstant.NOTIFICATION_DUPLICATE, jsonArray.toString());
                      }
                  } catch (Exception ex) {
                      Log.v("AdException", ex.toString());
                  }

              }
          } catch (Exception ex) {
              Log.v("AdPush", ex.toString());
          }
      }

    }



    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static void allCloudPush(Payload payload)
    {
       if(iZooto.appContext!=null) {
           PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(iZooto.appContext);
           try {
               if (preferenceUtil.getIntData(AppConstant.CLOUD_PUSH) == 1) {
                   showNotification(payload);

               } else {

                   String data = preferenceUtil.getStringData(AppConstant.NOTIFICATION_DUPLICATE);
                   JSONObject jsonObject = new JSONObject();
                   if (!data.isEmpty()) {
                       JSONArray jsonArray1 = new JSONArray(data);
                       if (jsonArray1.length() > 150) {
                           long currentTime = System.currentTimeMillis(); //fetch start time
                           for (int i = 0; i < jsonArray1.length(); i++) {
                               JSONObject jsonObject2 = jsonArray1.getJSONObject(i);
                               if ((currentTime - (Long.parseLong(jsonObject2.getString(AppConstant.CHECK_CREATED_ON)))) > Long.parseLong(payload.getTime_to_live())) {
                                   jsonArray1.remove(i);
                               } else {
                                   if (i < 10) {
                                       jsonArray1.remove(i);
                                   }
                               }
                           }
                           preferenceUtil.setStringData(AppConstant.NOTIFICATION_DUPLICATE, jsonArray1.toString());

                       } else {
                           if (jsonArray1.length() > 0) {
                               for (int index = 0; index < jsonArray1.length(); index++) {
                                   JSONObject jsonObject1 = jsonArray1.getJSONObject(index);
                                   if (jsonObject1.getString(AppConstant.CHECK_CREATED_ON).equalsIgnoreCase(payload.getCreated_Time()) && jsonObject1.getString(AppConstant.CHECK_RID).equalsIgnoreCase(payload.getRid())) {

                                       isCheck = true;
                                       if (jsonObject1.getString(AppConstant.Check_Notification).equalsIgnoreCase(AppConstant.YES)) {
                                           jsonArray1.remove(index);

                                       } else {
                                           jsonArray1.remove(index);
                                           jsonObject.put(AppConstant.CHECK_CREATED_ON, payload.getCreated_Time());
                                           jsonObject.put(AppConstant.CHECK_RID, payload.getRid());
                                           jsonObject.put(AppConstant.CHECK_TTL, payload.getTime_to_live());
                                           jsonObject.put(AppConstant.Check_Notification, AppConstant.Check_YES);
                                           jsonArray1.put(jsonObject);
                                       }
                                       break;
                                   } else {
                                       isCheck = false;
                                   }

                               }
                               if (isCheck) {
                                   preferenceUtil.setStringData(AppConstant.NOTIFICATION_DUPLICATE, jsonArray1.toString());

                               } else {
                                   showNotification(payload);
                                   jsonObject.put(AppConstant.CHECK_CREATED_ON, payload.getCreated_Time());
                                   jsonObject.put(AppConstant.CHECK_RID, payload.getRid());
                                   jsonObject.put(AppConstant.CHECK_TTL, payload.getTime_to_live());
                                   jsonObject.put(AppConstant.Check_Notification, AppConstant.Check_NO);
                                   jsonArray1.put(jsonObject);
                                   preferenceUtil.setStringData(AppConstant.NOTIFICATION_DUPLICATE, jsonArray1.toString());

                               }
                           } else {
                               showNotification(payload);
                               jsonObject.put(AppConstant.CHECK_CREATED_ON, payload.getCreated_Time());
                               jsonObject.put(AppConstant.CHECK_RID, payload.getRid());
                               jsonObject.put(AppConstant.CHECK_TTL, payload.getTime_to_live());
                               jsonObject.put(AppConstant.Check_Notification, AppConstant.Check_NO);
                               jsonArray1.put(jsonObject);
                               preferenceUtil.setStringData(AppConstant.NOTIFICATION_DUPLICATE, jsonArray1.toString());

                           }
                       }

                   } else {
                       JSONArray jsonArray = new JSONArray();
                       jsonObject.put(AppConstant.CHECK_CREATED_ON, payload.getCreated_Time());
                       jsonObject.put(AppConstant.CHECK_RID, payload.getRid());
                       jsonObject.put(AppConstant.CHECK_TTL, payload.getTime_to_live());
                       jsonObject.put(AppConstant.Check_Notification, AppConstant.Check_NO);
                       jsonArray.put(jsonObject);
                       preferenceUtil.setStringData(AppConstant.NOTIFICATION_DUPLICATE, jsonArray.toString());
                       showNotification(payload);
                   }
               }


           } catch (Exception ex) {
               Log.v("Data", "0" + ex.toString());

           }
       }

    }

    private static void processPayload(final Payload payload) {
        RestClient.get(payload.getFetchURL(), new RestClient.ResponseHandler() {
            @Override
            void onSuccess(String response) {
                super.onSuccess(response);
                if (response != null) {
                    try {

                        Object json = new JSONTokener(response).nextValue();
                        if(json instanceof JSONObject)
                        {
                            JSONObject jsonObject = new JSONObject(response);
                            parseJson(payload,jsonObject);

                        }
                        else if(json instanceof  JSONArray)
                        {
                            JSONArray jsonArray=new JSONArray(response);
                            JSONObject jsonObject=new JSONObject();
                            jsonObject.put("",jsonArray);
                            parseJson(payload,jsonObject);

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            void onFailure(int statusCode, String response, Throwable throwable) {
                super.onFailure(statusCode, response, throwable);

            }
        });
    }


    private static void parseJson(Payload payload, JSONObject jsonObject) {
        try {
            payload.setLink(getParsedValue(jsonObject, payload.getLink()));
            if (!payload.getLink().startsWith("http://") && !payload.getLink().startsWith("https://")) {
                String url = payload.getLink();
                url = "http://" + url;
                payload.setLink(url);

            }
            payload.setBanner(getParsedValue(jsonObject, payload.getBanner()));
            payload.setTitle(getParsedValue(jsonObject, payload.getTitle()));
            payload.setMessage(getParsedValue(jsonObject, payload.getMessage()));
            payload.setIcon(getParsedValue(jsonObject, payload.getIcon()));
            payload.setAct1name(getParsedValue(jsonObject,payload.getAct1name()));
            payload.setAct1link(getParsedValue(jsonObject,payload.getAct1link()));
            if (!payload.getAct1link().startsWith("http://") && !payload.getAct1link().startsWith("https://")) {
                String url = payload.getAct1link();
                url = "http://" + url;
                payload.setAct1link(url);

            }
            payload.setAp("");
            payload.setInapp(0);
            if(payload.getTitle()!=null && !payload.getTitle().equalsIgnoreCase("")) {
                showNotification(payload);
                Log.v("Notification Send","Yes");
            }
            else {
                Log.v("Notification Send","No");
            }



        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getParsedValue(JSONObject jsonObject, String sourceString) {
        try {
            if (sourceString.startsWith("~"))
                return sourceString.replace("~", "");
            else {
                if (sourceString.contains(".")) {
                    JSONObject jsonObject1 = null;
                    String[] linkArray = sourceString.split("\\.");
                    if(linkArray.length==2 || linkArray.length==3)
                    {
                        for (int i = 0; i < linkArray.length; i++) {
                            if (linkArray[i].contains("[")) {
                                String[] linkArray1 = linkArray[i].split("\\[");

                                if (jsonObject1 == null)
                                    jsonObject1 = jsonObject.getJSONArray(linkArray1[0]).getJSONObject(Integer.parseInt(linkArray1[1].replace("]", "")));
                                else {
                                    jsonObject1 = jsonObject1.getJSONArray(linkArray1[0]).getJSONObject(Integer.parseInt(linkArray1[1].replace("]", "")));

                                }

                            } else {
                                return jsonObject1.optString(linkArray[i]);
                            }

                        }
                    }
                    else if(linkArray.length==4)
                    {
                        if (linkArray[2].contains("[")) {
                            String[] linkArray1 = linkArray[2].split("\\[");
                            if(jsonObject1==null) {
                                jsonObject1 = jsonObject.getJSONObject(linkArray[0]).getJSONObject(linkArray[1]).getJSONArray(linkArray1[0]).getJSONObject(Integer.parseInt(linkArray1[1].replace("]", "")));
                            }
                            else
                                jsonObject1 = jsonObject.getJSONObject(linkArray[0]).getJSONObject(linkArray[1]).getJSONArray(linkArray1[0]).getJSONObject(Integer.parseInt(linkArray1[1].replace("]", "")));

                            return jsonObject1.getString(linkArray[3]);

                        }

                    }
                    else if(linkArray.length==5)
                    {
                        if (linkArray[2].contains("[")) {
                            String[] link1 = linkArray[2].split("\\[");
                            if (jsonObject1 == null)
                                jsonObject1 = jsonObject.getJSONObject(linkArray[0]).getJSONObject(linkArray[1]).getJSONArray(link1[0]).getJSONObject(Integer.parseInt(link1[1].replace("]", ""))).getJSONObject(linkArray[3]);
                            else
                                jsonObject1 = jsonObject1.getJSONObject(linkArray[0]).getJSONObject(linkArray[1]).getJSONArray(link1[0]).getJSONObject(Integer.parseInt(link1[1].replace("]", ""))).getJSONObject(linkArray[3]);


                            return jsonObject1.optString(linkArray[4]);
                        }
                    }
                    else
                    {
                        jsonObject.getString(sourceString);
                    }


                } else
                    return jsonObject.getString(sourceString);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private static void showNotification(final Payload payload) {
        if (iZooto.appContext == null)
            return;

        if (addCheck){
            receiveAds(payload);

        }else {
            final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(iZooto.appContext);
            if (Util.isAppInForeground(iZooto.appContext)){
                if (iZooto.inAppOption==null || iZooto.inAppOption.equalsIgnoreCase(AppConstant.NOTIFICATION_)){
                    if (payload.getCustomNotification() == 1 || preferenceUtil.getIntData(AppConstant.NOTIFICATION_PREVIEW)== PushTemplate.TEXT_OVERLAY)
                        NotificationCustomView.receiveCustomNotification(payload);
                    else
                        receivedNotification(payload);
                }else if (iZooto.inAppOption.equalsIgnoreCase(AppConstant.INAPPALERT)){
                    showAlert(payload);
                }
            }else {
                if (payload.getCustomNotification() == 1 || preferenceUtil.getIntData(AppConstant.NOTIFICATION_PREVIEW)==PushTemplate.TEXT_OVERLAY)
                    NotificationCustomView.receiveCustomNotification(payload);
                else
                    receivedNotification(payload);
            }
        }
    }    public static void receiveAds(final Payload payload){

        final Handler handler = new Handler(Looper.getMainLooper());
        final Runnable notificationRunnable = new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {

                String clickIndex = "0";
                String impressionIndex ="0";
                String lastSeventhIndex = "0";
                String lastNinthIndex = "0";


                String data=Util.getIntegerToBinary(payload.getCfg());
                if(data!=null && !data.isEmpty()) {
                    clickIndex = String.valueOf(data.charAt(data.length() - 2));
                    impressionIndex = String.valueOf(data.charAt(data.length() - 1));
                    lastView_Click = String.valueOf(data.charAt(data.length() - 3));
                    lastSeventhIndex = String.valueOf(data.charAt(data.length() - 7));
                    lastNinthIndex = String.valueOf(data.charAt(data.length() - 9));
                }
                else
                {
                    clickIndex = "0";
                    impressionIndex="0";
                    lastView_Click = "0";
                    lastSeventhIndex = "0";
                    lastNinthIndex = "0";

                }

                badgeCountUpdate(payload.getBadgeCount());


                String channelId = iZooto.appContext.getString(R.string.default_notification_channel_id);
                NotificationCompat.Builder notificationBuilder = null;
                Notification summaryNotification = null;
                int SUMMARY_ID = 0;
                Intent intent = null;

                icon = getBadgeIcon(payload.getBadgeicon());
                badgeColor = getBadgeColor(payload.getBadgecolor());
                lockScreenVisibility = setLockScreenVisibility(payload.getLockScreenVisibility());

                intent = notificationClick(payload, payload.getLink(),payload.getAct1link(),payload.getAct2link(),AppConstant.NO,clickIndex,lastView_Click,100,0);
                Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(iZooto.appContext, new Random().nextInt(100) /* Request code */, intent,
                        PendingIntent.FLAG_ONE_SHOT);


                notificationBuilder = new NotificationCompat.Builder(iZooto.appContext, channelId)
                        .setSmallIcon(icon)
                        .setContentTitle(payload.getTitle())
                        .setContentText(payload.getMessage())
                        .setContentIntent(pendingIntent)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(payload.getMessage()))
                        .setDefaults(NotificationCompat.DEFAULT_LIGHTS | NotificationCompat.DEFAULT_SOUND).setVibrate(new long[]{1000, 1000})
                        .setSound(defaultSoundUri)
                        .setVisibility(lockScreenVisibility)
                        .setAutoCancel(true);


                if (!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)){
                    if (payload.getPriority()==0)
                        notificationBuilder.setPriority(Notification.PRIORITY_HIGH);
                    else {
                        priority = priorityForLessOreo(payload.getPriority());
                        notificationBuilder.setPriority(priority);
                    }


                }
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                    if (payload.getGroup() == 1) {
                        notificationBuilder.setGroup(payload.getGroupKey());

                        summaryNotification =
                                new NotificationCompat.Builder(iZooto.appContext, channelId)
                                        .setContentTitle(payload.getTitle())
                                        .setContentText(payload.getMessage())
                                        .setSmallIcon(icon)
                                        .setColor(badgeColor)
                                        .setStyle(new NotificationCompat.InboxStyle()
                                                .addLine(payload.getMessage())
                                                .setBigContentTitle(payload.getGroupMessage()))
                                        .setGroup(payload.getGroupKey())
                                        .setGroupSummary(true)
                                        .build();
                    }
                }

                if (!payload.getSubTitle().contains(AppConstant.NULL)&&payload.getSubTitle()!=null&&!payload.getSubTitle().isEmpty()) {
                    notificationBuilder.setSubText(payload.getSubTitle());

                }
                if (payload.getBadgecolor()!=null&&!payload.getBadgecolor().isEmpty()){
                    notificationBuilder.setColor(badgeColor);
                }
                if(payload.getLedColor()!=null && !payload.getLedColor().isEmpty())
                    notificationBuilder.setColor(Color.parseColor(payload.getLedColor()));
                if (notificationIcon != null)
                    notificationBuilder.setLargeIcon(notificationIcon);
                else if (notificationBanner != null)
                    notificationBuilder.setLargeIcon(notificationBanner);
                if (notificationBanner != null && !payload.getSubTitle().contains(AppConstant.NULL) && payload.getSubTitle()!=null&&!payload.getSubTitle().isEmpty()) {
                    notificationBuilder.setStyle(new NotificationCompat.BigPictureStyle()
                            .bigPicture(notificationBanner)
                            .bigLargeIcon(notificationIcon).setSummaryText(payload.getMessage()));
                }else if (notificationBanner != null && payload.getMessage()!=null && !payload.getMessage().isEmpty())
                {
                    notificationBuilder.setStyle(new NotificationCompat.BigPictureStyle()
                            .bigPicture(notificationBanner)
                            .bigLargeIcon(notificationIcon).setSummaryText(payload.getMessage()));

                }else if (notificationBanner != null && payload.getMessage().isEmpty()){
                    notificationBuilder.setStyle(new NotificationCompat.BigPictureStyle()
                            .bigPicture(notificationBanner)
                            .bigLargeIcon(notificationIcon).setSummaryText(Util.makeBlackString(payload.getTitle())));
                }

                NotificationManager notificationManager =
                        (NotificationManager) iZooto.appContext.getSystemService(Context.NOTIFICATION_SERVICE);
                int notificationId;
                if (payload.getTag()!=null && !payload.getTag().isEmpty())
                    notificationId = Util.convertStringToDecimal(payload.getTag());
                else
                    notificationId = (int) System.currentTimeMillis();


                if (payload.getAct1name() != null && !payload.getAct1name().isEmpty()) {
                    String phone = getPhone(payload.getAct1link());
                    Intent btn1 = notificationClick(payload,payload.getAct1link(),payload.getLink(),payload.getAct2link(),phone,clickIndex,lastView_Click,notificationId,1);
                    PendingIntent pendingIntent1 = PendingIntent.getBroadcast(iZooto.appContext, new Random().nextInt(100), btn1, PendingIntent.FLAG_UPDATE_CURRENT);
                    NotificationCompat.Action action1 =
                            new NotificationCompat.Action.Builder(
                                    0,  payload.getAct1name(),
                                    pendingIntent1).build();
                    notificationBuilder.addAction(action1);


                }


                if (payload.getAct2name() != null && !payload.getAct2name().isEmpty()) {
//                    btn2.setAction(AppConstant.ACTION_BTN_TWO);
                    String phone = getPhone(payload.getAct2link());
                    Intent btn2 = notificationClick(payload,payload.getAct2link(),payload.getLink(),payload.getAct1link(),phone,clickIndex,lastView_Click,notificationId,2);
                    PendingIntent pendingIntent2 = PendingIntent.getBroadcast(iZooto.appContext, new Random().nextInt(100), btn2, PendingIntent.FLAG_UPDATE_CURRENT);
                    NotificationCompat.Action action2 =
                            new NotificationCompat.Action.Builder(
                                    0,payload.getAct2name(),
                                    pendingIntent2).build();
                    notificationBuilder.addAction(action2);
                }
                assert notificationManager != null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel channel;
                    if (payload.getPriority()==0) {
                        priority = NotificationManagerCompat.IMPORTANCE_HIGH;
                        channel = new NotificationChannel(channelId,
                                AppConstant.CHANNEL_NAME, priority);
                    }else {

                        priority = priorityForImportance(payload.getPriority());
                        channel = new NotificationChannel(channelId,
                                AppConstant.CHANNEL_NAME, priority);
                    }
                    if(iZooto.soundID!=null) {
                        priority = NotificationManagerCompat.IMPORTANCE_HIGH;
                        channel = new NotificationChannel(channelId,
                                AppConstant.CHANNEL_NAME, priority);
                        Uri uri = Util.getSoundUri(iZooto.appContext, iZooto.soundID);
                        if (uri != null)
                            channel.setSound(uri, null);
                        else
                            channel.setSound(null, null);
                    }
                    else
                    {
                        channel.setSound(null, null);

                    }

                    notificationManager.createNotificationChannel(channel);
                }
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                    if (payload.getGroup() == 1) {
                        notificationManager.notify(SUMMARY_ID, summaryNotification);
                    }
                }

                    notificationManager.notify(notificationId, notificationBuilder.build());
                try {

                    if(impressionIndex.equalsIgnoreCase("1")) {
                        impressionNotificationApi(payload);
                    }
                    if (lastView_Click.equalsIgnoreCase("1") || lastSeventhIndex.equalsIgnoreCase("1")){
                        lastViewNotificationApi(payload, lastView_Click, lastSeventhIndex, lastNinthIndex);
                    }

                    iZooto.notificationView(payload);
                    if (payload.getMaxNotification() != 0){
                        getMaximumNotificationInTray(iZooto.appContext, payload.getMaxNotification());}

                } catch (Exception e) {
                    e.printStackTrace();
                }

                notificationBanner = null;
                notificationIcon = null;
                /*link = "";
                link1 = "";
                link2 = "";*/

            }

        };


        new AppExecutors().networkIO().execute(new Runnable() {
            @Override
            public void run() {
                String smallIcon = payload.getIcon();
                String banner = payload.getBanner();
                try {
                    if (smallIcon != null && !smallIcon.isEmpty())
                        notificationIcon = Util.getBitmapFromURL(smallIcon);
                    if (banner != null && !banner.isEmpty()) {
                        notificationBanner = Util.getBitmapFromURL(banner);

                    }
                    handler.post(notificationRunnable);
                } catch (Exception e) {
                    Lg.e("Error", e.getMessage());
                    e.printStackTrace();
                    handler.post(notificationRunnable);
                }
            }
        });

    }

    private static void receivedNotification(final Payload payload){
        final Handler handler = new Handler(Looper.getMainLooper());
        final Runnable notificationRunnable = new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {

                String clickIndex = "0";
                String impressionIndex ="0";
                String lastSeventhIndex = "0";
                String lastNinthIndex = "0";


                String data=Util.getIntegerToBinary(payload.getCfg());
                if(data!=null && !data.isEmpty()) {
                    clickIndex = String.valueOf(data.charAt(data.length() - 2));
                    impressionIndex = String.valueOf(data.charAt(data.length() - 1));
                    lastView_Click = String.valueOf(data.charAt(data.length() - 3));
                    lastSeventhIndex = String.valueOf(data.charAt(data.length() - 7));
                    lastNinthIndex = String.valueOf(data.charAt(data.length() - 9));
                }
                else
                {
                    clickIndex = "0";
                    impressionIndex="0";
                    lastView_Click = "0";
                    lastSeventhIndex = "0";
                    lastNinthIndex = "0";

                }

                badgeCountUpdate(payload.getBadgeCount());


                String channelId = iZooto.appContext.getString(R.string.default_notification_channel_id);
                NotificationCompat.Builder notificationBuilder = null;
                Notification summaryNotification = null;
                int SUMMARY_ID = 0;
                Intent intent = null;

                icon = getBadgeIcon(payload.getBadgeicon());
                badgeColor = getBadgeColor(payload.getBadgecolor());
                lockScreenVisibility = setLockScreenVisibility(payload.getLockScreenVisibility());

                intent = notificationClick(payload, payload.getLink(),payload.getAct1link(),payload.getAct2link(),AppConstant.NO,clickIndex,lastView_Click,100,0);
                Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(iZooto.appContext, new Random().nextInt(100) /* Request code */, intent,
                        PendingIntent.FLAG_ONE_SHOT);


                //-------------- RemoteView  notification layout  ---------------
                RemoteViews collapsedView = new RemoteViews(iZooto.appContext.getPackageName(), R.layout.remote_view);
                RemoteViews expandedView = new RemoteViews(iZooto.appContext.getPackageName(), R.layout.remote_view_expands);


                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                    if (notificationBanner == null && notificationIcon == null) {
                        if (!payload.getMessage().isEmpty() && payload.getTitle().length() < 46) {
                            collapsedView.setTextViewText(R.id.tv_title, "" + payload.getTitle());
                            collapsedView.setViewVisibility(R.id.tv_message, 0);
                            collapsedView.setTextViewText(R.id.tv_message, "" + payload.getMessage());
                        } else {
                            collapsedView.setTextViewText(R.id.tv_title, "" + payload.getTitle());
                        }

                    } else {
                        collapsedView.setViewVisibility(R.id.linear_layout_large_icon, 0);
                        if (notificationIcon != null)
                            collapsedView.setImageViewBitmap(R.id.iv_large_icon, Util.makeCornerRounded(notificationIcon));
                        else
                            collapsedView.setImageViewBitmap(R.id.iv_large_icon, Util.makeCornerRounded(notificationBanner));
                        if (!payload.getMessage().isEmpty() && payload.getTitle().length() < 40) {
                            collapsedView.setTextViewText(R.id.tv_title, "" + payload.getTitle());
                            collapsedView.setViewVisibility(R.id.tv_message, 0);
                            collapsedView.setTextViewText(R.id.tv_message, "" + payload.getMessage());
                        } else {
                            collapsedView.setTextViewText(R.id.tv_title, "" + payload.getTitle());
                        }
                    }
                }else {
                    if (!payload.getMessage().isEmpty() && payload.getTitle().length() < 46) {
                        collapsedView.setTextViewText(R.id.tv_title, "" + payload.getTitle());
                        collapsedView.setViewVisibility(R.id.tv_message, 0);
                        collapsedView.setTextViewText(R.id.tv_message, "" + payload.getMessage());
                    } else
                        collapsedView.setTextViewText(R.id.tv_title, "" + payload.getTitle());
                }


                //--------------------- expanded notification ------------------
                if (notificationBanner==null){
                    expandedView.setTextViewText(R.id.tv_title,""+payload.getTitle());
                    if (!payload.getMessage().isEmpty()){
                        expandedView.setViewVisibility(R.id.tv_message, 0);
                        expandedView.setTextViewText(R.id.tv_message, "" + payload.getMessage());
                    }
                }else {
                    if (notificationBanner != null) {
                        if (payload.getAct1name().isEmpty() && payload.getAct2name().isEmpty()) {
                            expandedView.setViewVisibility(R.id.tv_title_with_banner_with_button, 2);
                            expandedView.setViewVisibility(R.id.iv_banner, 0);//0 for visible
                            expandedView.setImageViewBitmap(R.id.iv_banner, notificationBanner);

                            if (!payload.getMessage().isEmpty() && payload.getTitle().length()<46) {
                                expandedView.setViewVisibility(R.id.tv_message_with_banner, 0);
                                expandedView.setTextViewText(R.id.tv_title, "" + payload.getTitle());
                                expandedView.setTextViewText(R.id.tv_message_with_banner, "" + payload.getMessage());

                            }else {
                                if (!payload.getMessage().isEmpty()) {
                                    expandedView.setViewVisibility(R.id.tv_message_with_banner_with_button, 0);
                                    expandedView.setTextViewText(R.id.tv_title, "" + payload.getTitle());
                                    expandedView.setTextViewText(R.id.tv_message_with_banner_with_button, "" + payload.getMessage());
                                }else
                                    expandedView.setTextViewText(R.id.tv_title, "" + payload.getTitle());

                            }
                        } else {
                            expandedView.setViewVisibility(R.id.tv_title_with_banner_with_button, 0);
                            expandedView.setViewVisibility(R.id.tv_title, 2);//2 for gone
                            expandedView.setViewVisibility(R.id.iv_banner, 0);
                            expandedView.setTextViewText(R.id.tv_title_with_banner_with_button, "" + payload.getTitle());
                            expandedView.setImageViewBitmap(R.id.iv_banner, notificationBanner);
                            if (!payload.getMessage().isEmpty() && payload.getTitle().length()<46) {
                                expandedView.setViewVisibility(R.id.tv_message_with_banner_with_button, 0);
                                expandedView.setTextViewText(R.id.tv_message_with_banner_with_button, "" + payload.getMessage());
                            }
                        }
                    }
                }
                notificationBuilder = new NotificationCompat.Builder(iZooto.appContext, channelId)
                        .setSmallIcon(icon)
                        .setContentTitle(payload.getTitle())
                        .setContentText(payload.getMessage())
                        .setContentIntent(pendingIntent)
                        .setDefaults(NotificationCompat.DEFAULT_LIGHTS | NotificationCompat.DEFAULT_SOUND).setVibrate(new long[]{1000, 1000})
                        .setSound(defaultSoundUri)
                        .setVisibility(lockScreenVisibility)
                        .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                        .setCustomContentView(collapsedView)
                        .setCustomBigContentView(expandedView)
                        .setAutoCancel(true);


                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                    notificationBuilder.setCustomHeadsUpContentView(collapsedView);
                    if (notificationIcon != null)
                        notificationBuilder.setLargeIcon(notificationIcon);
                    else {
                        if (notificationBanner != null)
                            notificationBuilder.setLargeIcon(notificationBanner);
                    }
                }

                if (!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)){
                    if (payload.getPriority()==0)
                        notificationBuilder.setPriority(Notification.PRIORITY_HIGH);
                    else {
                        priority = priorityForLessOreo(payload.getPriority());
                        notificationBuilder.setPriority(priority);
                    }


                }
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                    if (payload.getGroup() == 1) {

                        if (payload.getMessage().isEmpty()){
                            notificationBuilder.setGroup(payload.getGroupKey());

                            summaryNotification =
                                    new NotificationCompat.Builder(iZooto.appContext, channelId)
                                            .setContentText(Util.makeBoldString(payload.getTitle()))
                                            .setSmallIcon(icon)
                                            .setColor(badgeColor)
                                            .setStyle(new NotificationCompat.InboxStyle()
                                                    .addLine(Util.makeBlackString(payload.getTitle()))
                                                    .setBigContentTitle(payload.getGroupMessage()))
                                            .setGroup(payload.getGroupKey())
                                            .setGroupSummary(true)
                                            .build();
                        }else {
                            notificationBuilder.setGroup(payload.getGroupKey());

                            summaryNotification =
                                    new NotificationCompat.Builder(iZooto.appContext, channelId)
                                            .setContentTitle(payload.getTitle())
                                            .setContentText(payload.getMessage())
                                            .setSmallIcon(icon)
                                            .setColor(badgeColor)
                                            .setStyle(new NotificationCompat.InboxStyle()
                                                    .addLine(payload.getMessage())
                                                    .setBigContentTitle(payload.getGroupMessage()))
                                            .setGroup(payload.getGroupKey())
                                            .setGroupSummary(true)
                                            .build();
                        }
                    }
                }

                if (!payload.getSubTitle().contains(AppConstant.NULL)&&payload.getSubTitle()!=null&&!payload.getSubTitle().isEmpty()) {
                    notificationBuilder.setSubText(payload.getSubTitle());

                }
                if (payload.getBadgecolor()!=null&&!payload.getBadgecolor().isEmpty()){
                    notificationBuilder.setColor(badgeColor);
                }
                if(payload.getLedColor()!=null && !payload.getLedColor().isEmpty())
                    notificationBuilder.setColor(Color.parseColor(payload.getLedColor()));

                NotificationManager notificationManager =
                        (NotificationManager) iZooto.appContext.getSystemService(Context.NOTIFICATION_SERVICE);

                int notificationID;
                if (payload.getTag()!=null && !payload.getTag().isEmpty())
                    notificationID = Util.convertStringToDecimal(payload.getTag());
                else
                    notificationID = (int) System.currentTimeMillis();
                if (payload.getAct1name() != null && !payload.getAct1name().isEmpty()) {
                    String phone = getPhone(payload.getAct1link());
                    Intent btn1 = notificationClick(payload,payload.getAct1link(),payload.getLink(),payload.getAct2link(),phone,clickIndex,lastView_Click,notificationID,1);
                    PendingIntent pendingIntent1 = PendingIntent.getBroadcast(iZooto.appContext, new Random().nextInt(100), btn1, PendingIntent.FLAG_UPDATE_CURRENT);
                    NotificationCompat.Action action1 =
                            new NotificationCompat.Action.Builder(
                                    R.drawable.transparent_image,  payload.getAct1name(),
                                    pendingIntent1).build();
                    notificationBuilder.addAction(action1);
                }


                if (payload.getAct2name() != null && !payload.getAct2name().isEmpty()) {
//                    btn2.setAction(AppConstant.ACTION_BTN_TWO);
                    String phone = getPhone(payload.getAct2link());
                    Intent btn2 = notificationClick(payload,payload.getAct2link(),payload.getLink(),payload.getAct1link(),phone,clickIndex,lastView_Click,notificationID,2);
                    PendingIntent pendingIntent2 = PendingIntent.getBroadcast(iZooto.appContext, new Random().nextInt(100), btn2, PendingIntent.FLAG_UPDATE_CURRENT);
                    NotificationCompat.Action action2 =
                            new NotificationCompat.Action.Builder(
                                    R.drawable.transparent_image,payload.getAct2name(),
                                    pendingIntent2).build();
                    notificationBuilder.addAction(action2);
                }
                assert notificationManager != null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel channel;
                    if (payload.getPriority()==0) {
                        priority = NotificationManagerCompat.IMPORTANCE_HIGH;
                        channel = new NotificationChannel(channelId,
                                AppConstant.CHANNEL_NAME, priority);
                    }else {

                        priority = priorityForImportance(payload.getPriority());
                        channel = new NotificationChannel(channelId,
                                AppConstant.CHANNEL_NAME, priority);
                    }
                    if(iZooto.soundID!=null) {
                        priority = NotificationManagerCompat.IMPORTANCE_HIGH;
                        channel = new NotificationChannel(channelId,
                                AppConstant.CHANNEL_NAME, priority);
                        Uri uri = Util.getSoundUri(iZooto.appContext, iZooto.soundID);
                        if (uri != null)
                            channel.setSound(uri, null);
                        else
                            channel.setSound(null, null);
                    }
                    else
                    {
                        channel.setSound(null, null);

                    }

                    notificationManager.createNotificationChannel(channel);
                }
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                    if (payload.getGroup() == 1) {
                        notificationManager.notify(SUMMARY_ID, summaryNotification);
                    }
                }

//                if (payload.getCollapseId()!=null && !payload.getCollapseId().isEmpty()){
//                    int notifyId = Util.convertStringToDecimal(payload.getCollapseId());
//                    notificationManager.notify(notifyId, notificationBuilder.build());
//                }else
                    notificationManager.notify(notificationID, notificationBuilder.build());
                try {

                    if(impressionIndex.equalsIgnoreCase("1")) {
                        impressionNotificationApi(payload);
                    }
                    if (lastView_Click.equalsIgnoreCase("1") || lastSeventhIndex.equalsIgnoreCase("1")){
                        lastViewNotificationApi(payload, lastView_Click, lastSeventhIndex, lastNinthIndex);
                    }
                    iZooto.notificationView(payload);

                    if (payload.getMaxNotification() != 0){
                        getMaximumNotificationInTray(iZooto.appContext, payload.getMaxNotification());}

                } catch (Exception e) {
                    e.printStackTrace();
                }

                notificationBanner = null;
                notificationIcon = null;


            }

        };


        new AppExecutors().networkIO().execute(new Runnable() {
            @Override
            public void run() {
                String smallIcon = payload.getIcon();
                String banner = payload.getBanner();
                try {
                    if (smallIcon != null && !smallIcon.isEmpty())
                        notificationIcon = Util.getBitmapFromURL(smallIcon);
                    if (banner != null && !banner.isEmpty()) {
                        notificationBanner = Util.getBitmapFromURL(banner);

                    }
                    handler.post(notificationRunnable);
                } catch (Exception e) {
                    Lg.e("Error", e.getMessage());
                    e.printStackTrace();
                    handler.post(notificationRunnable);
                }
            }
        });
    }

    public static boolean isInt(String s) {
        if (s.isEmpty())
            return false;

        if(!s.matches("-?\\d+"))
            return false;

        try {
            Integer.parseInt(s); //1234//what is use case variable i // Number format exception check kiya tha
            return true;
        } catch(NumberFormatException er) {
            Util.setException(iZooto.appContext, er.toString(), "isInt", AppConstant.APPName_2);
            return false;
        }

    }

    private static String getFinalUrl(Payload payload) {
        byte[] data = new byte[0];
        try {
            data = payload.getLink().getBytes(AppConstant.UTF);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String encodedLink = Base64.encodeToString(data, Base64.DEFAULT);
        Uri builtUri = Uri.parse(payload.getLink())
                .buildUpon()
                .appendQueryParameter(AppConstant.URL_ID, payload.getId())
                .appendQueryParameter(AppConstant.URL_CLIENT, payload.getKey())
                .appendQueryParameter(AppConstant.URL_RID, payload.getRid())
                .appendQueryParameter(AppConstant.URL_BKEY_, PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.FCM_DEVICE_TOKEN))
                .appendQueryParameter(AppConstant.URL_FRWD___, encodedLink)
                .build();
        return builtUri.toString();
    }
    public static String decodeURL(String url)
    {


        if(url.contains(AppConstant.URL_FWD)) {
            String[] arrOfStr = url.split(AppConstant.URL_FWD_);
            String[] second = arrOfStr[1].split(AppConstant.URL_BKEY);
            String decodeData = new String(Base64.decode(second[0], Base64.DEFAULT));
            return decodeData;
        }
        else
        {
            return url;
        }



    }

     static int priorityForImportance(int priority) {
        if (priority > 9)
            return NotificationManagerCompat.IMPORTANCE_MAX;
        if (priority > 7)
            return NotificationManagerCompat.IMPORTANCE_HIGH;
        return NotificationManagerCompat.IMPORTANCE_HIGH;
    }
     static int priorityForLessOreo(int priority) {
        if (priority > 0)
            return Notification.PRIORITY_HIGH;
        return Notification.PRIORITY_HIGH;
    }
     static int setLockScreenVisibility(int visibility) {
        if (visibility < 0)
            return NotificationCompat.VISIBILITY_SECRET;
        if (visibility == 0)
            return NotificationCompat.VISIBILITY_PRIVATE;
        return NotificationCompat.VISIBILITY_PUBLIC;

    }

     static void badgeCountUpdate(int count){
        final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(iZooto.appContext);
        try {
            if (count > 0) {
                if (preferenceUtil.getIntData(AppConstant.NOTIFICATION_COUNT)>=1){
                    preferenceUtil.setIntData(AppConstant.NOTIFICATION_COUNT,preferenceUtil.getIntData(AppConstant.NOTIFICATION_COUNT)+1);
                }else {
                    preferenceUtil.setIntData(AppConstant.NOTIFICATION_COUNT,1);
                }
            }
            ShortcutBadger.applyCountOrThrow(iZooto.appContext,preferenceUtil.getIntData(AppConstant.NOTIFICATION_COUNT));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean isAppInForeground(Context context) {
        List<ActivityManager.RunningTaskInfo> task =
                ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE))
                        .getRunningTasks(1);
        if (task.isEmpty()) {
            // app is in background
            return false;
        }
        return task
                .get(0)
                .topActivity
                .getPackageName()
                .equalsIgnoreCase(context.getPackageName());
    }

    private static void showAlert(final Payload payload){
        final Activity activity = iZooto.curActivity;
        if (activity!=null) {
            activity.runOnUiThread(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public void run() {
                    AlertDialog.Builder mBuilder = new AlertDialog.Builder(activity);
                    mBuilder.setTitle(payload.getTitle());
                    mBuilder.setMessage(payload.getMessage());

                    if (Util.getApplicationIcon(iZooto.appContext)!=null){
                        mBuilder.setIcon(Util.getApplicationIcon(iZooto.appContext));
                    }

                    String clickIndex = "0";
                    String impressionIndex ="0";
                    String lastSeventhIndex = "0";
                    String lastNinthIndex = "0";


                    String data=Util.getIntegerToBinary(payload.getCfg());
                    if(data!=null && !data.isEmpty()) {
                        clickIndex = String.valueOf(data.charAt(data.length() - 2));
                        impressionIndex = String.valueOf(data.charAt(data.length() - 1));
                        lastView_Click = String.valueOf(data.charAt(data.length() - 3));
                        lastSeventhIndex = String.valueOf(data.charAt(data.length() - 7));
                        lastNinthIndex = String.valueOf(data.charAt(data.length() - 9));
                    }
                    else
                    {
                        clickIndex = "0";
                        impressionIndex="0";
                        lastView_Click = "0";
                        lastSeventhIndex = "0";
                        lastNinthIndex = "0";

                    }

                    mBuilder.setNeutralButton(AppConstant.DIALOG_DISMISS, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    final String finalClickIndex1 = clickIndex;
                    // final String finalLastclickIndex = lastView_Click;
                    mBuilder.setPositiveButton(AppConstant.DIALOG_OK,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    dialog.dismiss();
                                    Intent intent = notificationClick(payload, payload.getLink(), payload.getAct1link(), payload.getAct2link(), AppConstant.NO, finalClickIndex1, lastView_Click, 100, 0);
                                    activity.sendBroadcast(intent);
                                }
                            });


                    mBuilder.setCancelable(true);
                    AlertDialog alertDialog = mBuilder.create();
                    alertDialog.setCanceledOnTouchOutside(false);
                    alertDialog.show();
                    try {

                        if(impressionIndex.equalsIgnoreCase("1")) {
                            impressionNotificationApi(payload);
                        }
                        if (lastView_Click.equalsIgnoreCase("1") || lastSeventhIndex.equalsIgnoreCase("1")){
                            lastViewNotificationApi(payload, lastView_Click, lastSeventhIndex, lastNinthIndex);
                        }
                       // iZooto.notificationView(payload);
                        final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(iZooto.appContext);
                        if (!preferenceUtil.getBoolean(AppConstant.IS_HYBRID_SDK))
                            iZooto.notificationView(payload);
                        else {
                            onReceiveNotificationHybrid(iZooto.appContext, payload);
                            iZootoReceivedPayload = preferenceUtil.getStringData(AppConstant.PAYLOAD_JSONARRAY);
                            iZooto.notificationViewHybrid(iZootoReceivedPayload, payload);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });
        }

    }

     static Intent notificationClick(Payload payload, String getLink ,String getLink1, String getLink2, String phone, String finalClickIndex, String lastClick, int notificationId, int button){
        String link = getLink;
        String link1 = getLink1;
        String link2 = getLink2;
        if (payload.getFetchURL() == null || payload.getFetchURL().isEmpty()) {
            if (link.contains(AppConstant.ANDROID_TOKEN) || link.contains(AppConstant.DEVICE_ID) || link.contains(AppConstant.R_XIAOMI_TOKEN)|| link.contains(AppConstant.R_HMS_TOKEN) || link.contains(AppConstant.R_FCM_TOKEN)) {
               if(Build.MANUFACTURER.equalsIgnoreCase("Huawei")) {
                   link = link.replace(AppConstant.ANDROID_TOKEN, PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.HMS_TOKEN)).replace(AppConstant.DEVICE_ID, Util.getAndroidId(iZooto.appContext)).replace(AppConstant.R_XIAOMI_TOKEN, PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.XiaomiToken)).replace(AppConstant.R_HMS_TOKEN, PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.HMS_TOKEN)).replace(AppConstant.R_FCM_TOKEN, PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.FCM_DEVICE_TOKEN));
               }
               if(PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.FCM_DEVICE_TOKEN)!=null || PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.XiaomiToken)!=null) {
                   link = link.replace(AppConstant.ANDROID_TOKEN, PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.FCM_DEVICE_TOKEN)).replace(AppConstant.DEVICE_ID, Util.getAndroidId(iZooto.appContext)).replace(AppConstant.R_XIAOMI_TOKEN, PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.XiaomiToken)).replace(AppConstant.R_HMS_TOKEN, PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.HMS_TOKEN)).replace(AppConstant.R_FCM_TOKEN, PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.FCM_DEVICE_TOKEN));
               }
            }

            if (link1.contains(AppConstant.ANDROID_TOKEN) || link1.contains(AppConstant.DEVICE_ID) || link1.contains(AppConstant.R_XIAOMI_TOKEN)|| link1.contains(AppConstant.R_HMS_TOKEN) || link1.contains(AppConstant.R_FCM_TOKEN)) {
                if(Build.MANUFACTURER.equalsIgnoreCase("Huawei")) {
                    link1 = link1.replace(AppConstant.ANDROID_TOKEN, PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.HMS_TOKEN)).replace(AppConstant.DEVICE_ID, Util.getAndroidId(iZooto.appContext)).replace(AppConstant.R_XIAOMI_TOKEN, PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.XiaomiToken)).replace(AppConstant.R_HMS_TOKEN, PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.HMS_TOKEN)).replace(AppConstant.R_FCM_TOKEN, PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.FCM_DEVICE_TOKEN));
                }
                if(PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.FCM_DEVICE_TOKEN)!=null || PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.XiaomiToken)!=null) {
                    link1 = link1.replace(AppConstant.ANDROID_TOKEN, PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.FCM_DEVICE_TOKEN)).replace(AppConstant.DEVICE_ID, Util.getAndroidId(iZooto.appContext)).replace(AppConstant.R_XIAOMI_TOKEN, PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.XiaomiToken)).replace(AppConstant.R_HMS_TOKEN, PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.HMS_TOKEN)).replace(AppConstant.R_FCM_TOKEN, PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.FCM_DEVICE_TOKEN));
                }
            }
            if (link2.contains(AppConstant.ANDROID_TOKEN) || link2.contains(AppConstant.DEVICE_ID) || link2.contains(AppConstant.R_XIAOMI_TOKEN)|| link2.contains(AppConstant.R_HMS_TOKEN) || link2.contains(AppConstant.R_FCM_TOKEN)) {
                if(Build.MANUFACTURER.equalsIgnoreCase("Huawei")) {
                    link2 = link2.replace(AppConstant.ANDROID_TOKEN, PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.HMS_TOKEN)).replace(AppConstant.DEVICE_ID, Util.getAndroidId(iZooto.appContext)).replace(AppConstant.R_XIAOMI_TOKEN, PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.XiaomiToken)).replace(AppConstant.R_HMS_TOKEN, PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.HMS_TOKEN)).replace(AppConstant.R_FCM_TOKEN, PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.FCM_DEVICE_TOKEN));
                }
                if(PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.FCM_DEVICE_TOKEN)!=null || PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.XiaomiToken)!=null) {
                    link2 = link2.replace(AppConstant.ANDROID_TOKEN, PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.FCM_DEVICE_TOKEN)).replace(AppConstant.DEVICE_ID, Util.getAndroidId(iZooto.appContext)).replace(AppConstant.R_XIAOMI_TOKEN, PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.XiaomiToken)).replace(AppConstant.R_HMS_TOKEN, PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.HMS_TOKEN)).replace(AppConstant.R_FCM_TOKEN, PreferenceUtil.getInstance(iZooto.appContext).getStringData(AppConstant.FCM_DEVICE_TOKEN));
                }
            }
        } else {
            String notificationLink = payload.getLink();
            notificationLink = getFinalUrl(payload);
        }



         Intent intent = new Intent(iZooto.appContext, NotificationActionReceiver.class);
        intent.putExtra(AppConstant.KEY_WEB_URL, link);
        intent.putExtra(AppConstant.KEY_NOTIFICITON_ID, notificationId);
        intent.putExtra(AppConstant.KEY_IN_APP, payload.getInapp());
        intent.putExtra(AppConstant.KEY_IN_CID, payload.getId());
        intent.putExtra(AppConstant.KEY_IN_RID, payload.getRid());
        intent.putExtra(AppConstant.KEY_IN_BUTOON, button);
        intent.putExtra(AppConstant.KEY_IN_ADDITIONALDATA, payload.getAp());
        intent.putExtra(AppConstant.KEY_IN_PHONE, phone);
        intent.putExtra(AppConstant.KEY_IN_ACT1ID, payload.getAct1ID());
        intent.putExtra(AppConstant.KEY_IN_ACT2ID, payload.getAct2ID());
        intent.putExtra(AppConstant.LANDINGURL, link);
        intent.putExtra(AppConstant.ACT1TITLE, payload.getAct1name());
        intent.putExtra(AppConstant.ACT2TITLE, payload.getAct2name());
        intent.putExtra(AppConstant.ACT1URL, link1);
        intent.putExtra(AppConstant.ACT2URL, link2);
        intent.putExtra(AppConstant.CLICKINDEX, finalClickIndex);
        intent.putExtra(AppConstant.LASTCLICKINDEX, lastClick);
        intent.putExtra(AppConstant.PUSH,payload.getPush_type());
        intent.putExtra(AppConstant.CFGFORDOMAIN, payload.getCfg());
        return intent;
    }

     static void impressionNotificationApi(final Payload payload){
        if(iZooto.appContext!=null) {

            final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(iZooto.appContext);
            String imprURL;
            int dataCfg = Util.getBinaryToDecimal(payload.getCfg());
            if (dataCfg > 0) {
                imprURL = "https://impr" + dataCfg + ".izooto.com/imp" + dataCfg;
            } else
                imprURL = RestClient.IMPRESSION_URL;

            try {
                HashMap<String, String> data = new HashMap<>();
                data.put(AppConstant.PID, preferenceUtil.getiZootoID(AppConstant.APPPID));
                data.put(AppConstant.CID_, payload.getId());
                data.put(AppConstant.ANDROID_ID, Util.getAndroidId(iZooto.appContext));
                data.put(AppConstant.RID, payload.getRid());
                data.put("op", "view");
                data.put("ct", payload.getPush_type());
                RestClient.newPostRequest(imprURL, data, new RestClient.ResponseHandler() {
                    @Override
                    void onFailure(int statusCode, String response, Throwable throwable) {
                        super.onFailure(statusCode, response, throwable);
                    }

                    @Override
                    void onSuccess(String response) {
                        super.onSuccess(response);
                    }
                });

            } catch (Exception ex) {
                Util.setException(iZooto.appContext, ex.toString(), AppConstant.APPName_2, "viewNotificationAPi");
            }
        }
    }
     static int getBadgeColor(String setColor){
        int iconColor;
        if (setColor.contains("#")){
            try{
                iconColor = Color.parseColor(setColor);
            } catch(IllegalArgumentException ex){
                // handle your exceptizion
                iconColor = Color.TRANSPARENT;
                ex.printStackTrace();
            }
        }else if (setColor!=null&&!setColor.isEmpty()){
            try{
                iconColor = Color.parseColor("#"+setColor);
            } catch(IllegalArgumentException ex){ // handle your exception
                iconColor = Color.TRANSPARENT;
                ex.printStackTrace();
            }
        }else {
            iconColor = Color.TRANSPARENT;
        }
        return iconColor;
    }

     static int getBadgeIcon(String setBadgeIcon){
        int bIicon;
        if (iZooto.icon!=0)
        {
            bIicon=iZooto.icon;
        }
        else
        {
            if (setBadgeIcon.equalsIgnoreCase(AppConstant.DEFAULT_ICON)){
                bIicon=R.drawable.ic_notifications_black_24dp;
            }else {

                if (isInt(setBadgeIcon)){
                    bIicon = iZooto.appContext.getApplicationInfo().icon;
                }else {
                    int checkExistence = iZooto.appContext.getResources().getIdentifier(setBadgeIcon, "drawable", iZooto.appContext.getPackageName());
                    if ( checkExistence != 0 ) {  // the resource exists...
                        bIicon = checkExistence;

                    }
                    else {  // checkExistence == 0  // the resource does NOT exist!!
                        int checkExistenceMipmap = iZooto.appContext.getResources().getIdentifier(
                                setBadgeIcon, "mipmap", iZooto.appContext.getPackageName());
                        if ( checkExistenceMipmap != 0 ) {  // the resource exists...
                            bIicon = checkExistenceMipmap;

                        }else {

                            bIicon =R.drawable.ic_notifications_black_24dp;// iZooto.appContext.getApplicationInfo().logo;
                        }

                    }

                }

            }

        }
        return bIicon;
    }
     static String getPhone(String getActLink){
        String phone;

        String checkNumber =decodeURL(getActLink);
        if (checkNumber.contains(AppConstant.TELIPHONE))
            phone = checkNumber;
        else
            phone = AppConstant.NO;
        return phone;
    }
     static void lastViewNotificationApi(final Payload payload, String lastViewIndex, String seventhCFG, String ninthCFG){
        final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(iZooto.appContext);
        String dayDiff1 = Util.dayDifference(Util.getTime(), preferenceUtil.getStringData(AppConstant.CURRENT_DATE_VIEW_WEEKLY));
        String updateWeekly = preferenceUtil.getStringData(AppConstant.CURRENT_DATE_VIEW_WEEKLY);
        String updateDaily = preferenceUtil.getStringData(AppConstant.CURRENT_DATE_VIEW_DAILY);
        String time = preferenceUtil.getStringData(AppConstant.CURRENT_DATE_VIEW);

        if (seventhCFG.equalsIgnoreCase("1")){

            if (ninthCFG.equalsIgnoreCase("1")){
                if (!updateDaily.equalsIgnoreCase(Util.getTime())){
                    preferenceUtil.setStringData(AppConstant.CURRENT_DATE_VIEW_DAILY, Util.getTime());
                    lastViewNotification(payload);
                }
            }else {
                if (updateWeekly.isEmpty() || Integer.parseInt(dayDiff1) >= 7){
                    preferenceUtil.setStringData(AppConstant.CURRENT_DATE_VIEW_WEEKLY, Util.getTime());
                    lastViewNotification(payload);
                }
            }
        }else if (lastViewIndex.equalsIgnoreCase("1") && seventhCFG.equalsIgnoreCase("0")){
            String dayDiff = Util.dayDifference(Util.getTime(), preferenceUtil.getStringData(AppConstant.CURRENT_DATE_VIEW));
            if (time.isEmpty() || Integer.parseInt(dayDiff) >= 7) {
                preferenceUtil.setStringData(AppConstant.CURRENT_DATE_VIEW, Util.getTime());
                lastViewNotification(payload);
            }
        }


    }

     static void lastViewNotification(final Payload payload){
        if(iZooto.appContext!=null) {
            final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(iZooto.appContext);
            try {
                HashMap<String, Object> data = new HashMap<>();
                data.put(AppConstant.LAST_NOTIFICAION_VIEWED, true);
                JSONObject jsonObject = new JSONObject(data);

                String limURL;
                int dataCfg = Util.getBinaryToDecimal(payload.getCfg());

                if (dataCfg > 0) {
                    limURL = "https://lim" + dataCfg + ".izooto.com/lim" + dataCfg;
                } else
                    limURL = RestClient.LASTNOTIFICATIONVIEWURL;

                Map<String, String> mapData = new HashMap<>();
                mapData.put(AppConstant.PID, preferenceUtil.getiZootoID(AppConstant.APPPID));
                mapData.put(AppConstant.VER_, AppConstant.SDKVERSION);
                mapData.put(AppConstant.ANDROID_ID, "" + Util.getAndroidId(iZooto.appContext));
                mapData.put(AppConstant.VAL, "" + jsonObject.toString());
                mapData.put(AppConstant.ACT, "add");
                mapData.put(AppConstant.ISID_, "1");
                mapData.put(AppConstant.ET_, "" + AppConstant.USERP_);
                RestClient.newPostRequest(limURL, mapData, new RestClient.ResponseHandler() {
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
                Util.setException(iZooto.appContext, ex.toString(), AppConstant.APPName_2, "lastViewNotification");
            }
        }
    }

    /*
     *Set Maximum notification in the tray through getMaximumNotificationInTray() method
     * */
     static void getMaximumNotificationInTray(Context context, int mn){
        if(context!=null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    NotificationManager notificationManagerActive =
                            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    StatusBarNotification[] notifications = notificationManagerActive.getActiveNotifications();
                    SortedMap<Long, Integer> activeNotifIds = new TreeMap<>();
                    for (StatusBarNotification notification : notifications) {
                        if (notification.getTag() == null) {
                            activeNotifIds.put(notification.getNotification().when, notification.getId());
                        }
                    }
                    int data = activeNotifIds.size() - mn;
                    for (Map.Entry<Long, Integer> mapData : activeNotifIds.entrySet()) {
                        if (data <= 0)
                            return;
                        data--;
                        NotificationManager notificationManager =
                                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.cancel(mapData.getValue());
                    }
                }
            } catch (Exception e) {
                Util.setException(context, e.toString(), AppConstant.APPName_2, "MaxNotification in Tray");
            }
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
     static void onReceiveNotificationHybrid(Context context, Payload payload){
        PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
        JSONObject payloadJSON = new JSONObject();
        JSONArray jsonArray;
        try {
            if (!preferenceUtil.getStringData(AppConstant.PAYLOAD_JSONARRAY).isEmpty())
                jsonArray = new JSONArray(preferenceUtil.getStringData(AppConstant.PAYLOAD_JSONARRAY));
            else
                jsonArray = new JSONArray();
            if (jsonArray.length() >= 10){
                jsonArray.remove(0);
            }
            try {
                payloadJSON.put("title", payload.getTitle());
                payloadJSON.put("message", payload.getMessage());
                payloadJSON.put("banner", payload.getBanner());
                payloadJSON.put("landingURL", payload.getLink());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            jsonArray.put(payloadJSON);
            preferenceUtil.setStringData(AppConstant.PAYLOAD_JSONARRAY, jsonArray.toString());
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

}
