package com.cgana.trmsdriver.fcm;

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

import com.cgana.trmsdriver.MainActivity;
import com.cgana.trmsdriver.R;
import com.cgana.trmsdriver.data.local.TokenManager;
import com.cgana.trmsdriver.data.model.FCMTokenRequest;
import com.cgana.trmsdriver.data.model.GenericResponse;
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
        if (!remoteMessage.getData().isEmpty()) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            Map<String, String> data = remoteMessage.getData();
            String type = data.get("type");

            // Handle different notification types (Module 4 Part 3)
            if ("proximity_alert".equals(type)) {
                handleProximityAlert(data);
            } else if ("destination_reached".equals(type)) {
                handleDestinationReached(data);
            } else if ("missed_stop_alert".equals(type)) {
                handleMissedStopAlert(data);
            } else {
                // Handle other message types
                handleDataMessage(remoteMessage);
            }
        }

        // Check if message contains notification payload
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            Log.d(TAG, "Notification - Title: " + title + ", Body: " + body);

            showNotification(title, body, remoteMessage.getData());
        }
    }

    private void handleDataMessage(RemoteMessage remoteMessage) {
        String type = remoteMessage.getData().get("type");

        if (type != null) {
            switch (type) {
                case "dashboard_update":
                    // Silently refresh dashboard (send broadcast)
                    sendBroadcast(new Intent("com.cgana.trmsdriver.REFRESH_DASHBOARD"));
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

    /**
     * Handle proximity alert notification (Module 4 Part 3)
     */
    private void handleProximityAlert(Map<String, String> data) {
        String seatNumber = data.get("seat_number");
        String destination = data.get("destination");
        String distanceMeters = data.get("distance_meters");

        if (seatNumber == null || destination == null || distanceMeters == null) {
            Log.w(TAG, "Incomplete proximity alert data");
            return;
        }

        try {
            int seat = Integer.parseInt(seatNumber);
            float distance = Float.parseFloat(distanceMeters);

            String title = getString(R.string.approaching_destination);
            String message = getString(R.string.approaching_notification_text, seat, destination, distance);

            sendProximityNotification(title, message);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error parsing proximity alert data", e);
        }
    }

    /**
     * Handle destination reached notification (Module 4 Part 3)
     */
    private void handleDestinationReached(Map<String, String> data) {
        String seatNumber = data.get("seat_number");
        String destination = data.get("destination");

        if (seatNumber == null || destination == null) {
            return;
        }

        String title = "Destination Reached";
        String message = "Seat " + seatNumber + " has reached " + destination;

        sendProximityNotification(title, message);
    }

    /**
     * Handle missed stop alert (Module 4 Part 3)
     */
    private void handleMissedStopAlert(Map<String, String> data) {
        String seatNumber = data.get("seat_number");
        String destination = data.get("destination");

        if (seatNumber == null || destination == null) {
            return;
        }

        String title = getString(R.string.missed_stop);
        String message = "Seat " + seatNumber + " missed stop at " + destination;

        showNotification(title, message, data);
    }

    /**
     * Send proximity notification with high priority and alarm sound (Module 4 Part 3)
     */
    private void sendProximityNotification(String title, String message) {
        // Create proximity notification channel
        String channelId = "proximity_alerts";
        createProximityNotificationChannel(channelId);

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        );

        // Use alarm sound for proximity alerts
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_location)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(alarmSound)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVibrate(new long[]{0, 500, 200, 500, 200, 500})
            .setContentIntent(pendingIntent)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(message));

        NotificationManager notificationManager =
            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            int notificationId = (int) System.currentTimeMillis();
            notificationManager.notify(notificationId, notificationBuilder.build());
        }
    }

    /**
     * Create notification channel for proximity alerts (Module 4 Part 3)
     */
    private void createProximityNotificationChannel(String channelId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                channelId,
                getString(R.string.proximity_alert),
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Alerts when vehicle is approaching passenger destinations");
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 500, 200, 500, 200, 500});
            channel.setShowBadge(true);

            NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void sendTokenToServer(String token) {
        TokenManager tokenManager = new TokenManager(this);
        String authToken = tokenManager.getToken();

        if (authToken == null) {
            Log.w(TAG, "Not authenticated, cannot send FCM token");
            return;
        }

        // TODO: Implement FCM token registration API when backend is ready
        Log.d(TAG, "FCM token saved locally. Backend registration pending.");

        /*
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
        */
    }
}

