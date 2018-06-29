package ru.mpei.mpei_pk;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import ru.mpei.mpei_pk.activities.LoadingActivity;
import ru.mpei.mpei_pk.activities.WebActivity;

/**
 * Created by infrostorm on 12.11.2017.
 */

public class FMService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //записываем в лог от кого было принято сообщение
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        //если сообщение не пустое получаем данные и печатаем в лог
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            //закладка для работы с firebase job dispatcher для длительных обработок входящих сообщений
            //if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
            /*} else {
                // Handle message within 10 seconds
                handleNow();
            }*/
            String title  = remoteMessage.getData().get("title");
            String body = remoteMessage.getData().get("body");
            sendNotification(title, body);
        }
        //получаем заголовок и тело уведомления и вызыываем уведомление
        if (remoteMessage.getNotification() != null) {
            String title  = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            sendNotification(title, body);
            Log.d(TAG, "Message Notification title: " + title + " body: " + body );
        }
    }

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private void handleNow() {
        Log.d(TAG, "Short lived task is done.");
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String messageTitle, String messageBody) {
        Intent intent = new Intent(this, LoadingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);
        //Для новых версий android необходимо указывать канал уведомлений
        String channelId = getString(R.string.default_notification_channel_id);
        //получаем URI звука
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                //строим уведомление с требуемыми параметрами
                new NotificationCompat.Builder(this, channelId)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(messageTitle)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent)
                        .setDefaults(Notification.DEFAULT_ALL);

        //получаем экземпляр сервиса нотификации
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //показываем полученное уведомление
        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }


}
