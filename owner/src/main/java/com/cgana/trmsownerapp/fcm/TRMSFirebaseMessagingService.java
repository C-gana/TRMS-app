package com.cgana.trmsownerapp.fcm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.cgana.trmsownerapp.MainActivity;
import com.cgana.trmsownerapp.R;
import com.cgana.trmsownerapp.data.api.FCMApiService;
import com.cgana.trmsownerapp.data.api.RetrofitClient;
import com.cgana.trmsownerapp.data.local.TokenManager;
import com.cgana.trmsownerapp.data.model.FCMTokenRequest;
import com.cgana.trmsownerapp.data.model.GenericResponse;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TRMSFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "TRMSFCMService";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "New FCM Token: " + token);

        // Save token to TokenManager
        TokenManager tokenManager = new TokenManager(this);
        tokenManager.saveFCMToken(token);
        Log.d(TAG, "FCM Token saved to TokenManager");

        // Send token to backend
        sendTokenToServer(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "Message received from: " + remoteMessage.getFrom());

        // Check if message contains data payload
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data: " + remoteMessage.getData());
            handleDataMessage(remoteMessage);
        }

        // Check if message contains notification payload
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Notification title: " + remoteMessage.getNotification().getTitle());
            Log.d(TAG, "Notification body: " + remoteMessage.getNotification().getBody());

            showNotification(
                    remoteMessage.getNotification().getTitle(),
                    remoteMessage.getNotification().getBody(),
                    remoteMessage.getData()
            );
        }
    }

    private void handleDataMessage(RemoteMessage remoteMessage) {
        String type = remoteMessage.getData().get("type");

        if (type != null) {
            switch (type) {
                case "dashboard_update":
                    // Silently refresh dashboard (send broadcast)
                    sendBroadcast(new Intent("com.cgana.trmsownerapp.REFRESH_DASHBOARD"));
                    break;

                case "boarding":
                    String seatNumber = remoteMessage.getData().get("seat_number");
                    showNotification(
                            "Passenger Boarded",
                            "Seat " + seatNumber + " occupied. Select destination.",
                            remoteMessage.getData()
                    );
                    break;

                case "proximity":
                    String destination = remoteMessage.getData().get("destination");
                    String distance = remoteMessage.getData().get("distance");
                    showNotification(
                            "Approaching Destination",
                            "Vehicle approaching " + destination + " - " + distance + "m away",
                            remoteMessage.getData()
                    );
                    break;

                case "missed_stop":
                    String vehicleId = remoteMessage.getData().get("vehicle_id");
                    String dest = remoteMessage.getData().get("destination");
                    String seat = remoteMessage.getData().get("seat_number");
                    showNotification(
                            "Missed Stop Alert",
                            "Vehicle " + vehicleId + " - Passenger seat " + seat + " did not alight at " + dest,
                            remoteMessage.getData()
                    );
                    break;

                case "timeout":
                    String timeoutSeat = remoteMessage.getData().get("seat_number");
                    showNotification(
                            "Timeout Alert",
                            "Destination not selected for seat " + timeoutSeat + " within 90 seconds",
                            remoteMessage.getData()
                    );
                    break;

                case "destination_sync":
                    // Refresh destinations list
                    sendBroadcast(new Intent("com.cgana.trmsownerapp.REFRESH_DESTINATIONS"));
                    showNotification(
                            "Destinations Updated",
                            "Destination list has been updated",
                            remoteMessage.getData()
                    );
                    break;

                case "test_alert":
                    String alertType = remoteMessage.getData().get("alert_type");
                    String severity = remoteMessage.getData().get("severity");
                    String message = remoteMessage.getData().get("message");
                    String destinationName = remoteMessage.getData().get("destination_name");

                    String testAlertTitle = "Test Alert";
                    if (alertType != null && !alertType.isEmpty()) {
                        testAlertTitle += " - " + alertType;
                    }

                    String testAlertBody = message != null ? message : "Test alert received";
                    if (destinationName != null && !destinationName.isEmpty()) {
                        testAlertBody += " (Destination: " + destinationName + ")";
                    }
                    if (severity != null && !severity.isEmpty()) {
                        testAlertBody = "[" + severity.toUpperCase() + "] " + testAlertBody;
                    }

                    Log.d(TAG, "Test alert received - Type: " + alertType + ", Severity: " + severity);
                    showNotification(testAlertTitle, testAlertBody, remoteMessage.getData());
                    break;
            }
        }
    }

    private void showNotification(String title, String body, Map<String, String> data) {
        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // Create intent
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add extras from data
        if (data != null) {
            for (Map.Entry<String, String> entry : data.entrySet()) {
                intent.putExtra(entry.getKey(), entry.getValue());
            }
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Create notification channel for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "TRMS Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for alerts, boarding, and updates");
            notificationManager.createNotificationChannel(channel);
        }

        // Use unique notification ID based on current time
        int notificationId = (int) System.currentTimeMillis();
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    private void sendTokenToServer(String token) {
        TokenManager tokenManager = new TokenManager(this);
        String authToken = tokenManager.getToken();

        if (authToken == null) {
            Log.w(TAG, "Not authenticated, cannot send FCM token");
            return;
        }

        FCMApiService apiService = RetrofitClient.getInstance().getFCMApi();
        FCMTokenRequest request = new FCMTokenRequest(token);

        apiService.registerFCMToken(request, "Bearer " + authToken)
                .enqueue(new Callback<GenericResponse>() {
                    @Override
                    public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Log.d(TAG, "FCM token sent to server successfully");
                        } else {
                            Log.e(TAG, "Failed to send FCM token: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<GenericResponse> call, Throwable t) {
                        Log.e(TAG, "Error sending FCM token: " + t.getMessage());
                    }
                });
    }
}

