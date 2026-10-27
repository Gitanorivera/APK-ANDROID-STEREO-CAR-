package app.autolauncher;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

/**
 * Lee el GPS mientras el estéreo está encendido, aunque estés en ZLink, para que
 * la distancia del viaje no se pierda. No guarda coordenadas ni tiene internet.
 */
public class TripService extends Service implements LocationListener {

    private static final String CHANNEL = "viaje";
    private static final int NOTIF_ID = 1;
    private static final long SAVE_EVERY_MS = 30_000L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Prefs prefs;
    private LocationManager lm;
    private boolean listening;

    private final Runnable saver = new Runnable() {
        @Override
        public void run() {
            Trip.I.save(prefs);
            handler.postDelayed(this, SAVE_EVERY_MS);
        }
    };

    static boolean hasPermission(Context c) {
        return c.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    static void start(Context c) {
        if (!hasPermission(c)) return;
        try {
            c.startForegroundService(new Intent(c, TripService.class));
        } catch (RuntimeException ignored) {
            // El sistema puede negarlo en casos raros; se intenta de nuevo al volver al inicio.
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = new Prefs(this);
        Trip.I.load(prefs);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        goForeground();
        if (!listening) startListening();
        return START_STICKY;
    }

    private void goForeground() {
        NotificationManager nm = getSystemService(NotificationManager.class);
        if (nm != null) {
            NotificationChannel ch = new NotificationChannel(CHANNEL, "Viaje (GPS)",
                    NotificationManager.IMPORTANCE_MIN);
            ch.setShowBadge(false);
            nm.createNotificationChannel(ch);
        }
        Notification n = new Notification.Builder(this, CHANNEL)
                .setSmallIcon(R.drawable.ic_gauge)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Midiendo el viaje con el GPS")
                .setOngoing(true)
                .build();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIF_ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        } else {
            startForeground(NOTIF_ID, n);
        }
    }

    private void startListening() {
        if (!hasPermission(this)) {
            stopSelf();
            return;
        }
        lm = getSystemService(LocationManager.class);
        if (lm == null) {
            stopSelf();
            return;
        }
        try {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 0f, this, Looper.getMainLooper());
            listening = true;
            handler.postDelayed(saver, SAVE_EVERY_MS);
        } catch (SecurityException | IllegalArgumentException e) {
            stopSelf();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Trip.I.onLocation(location);
    }

    // Necesarios en Android 10 y anteriores.
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(saver);
        if (lm != null && listening) lm.removeUpdates(this);
        listening = false;
        if (prefs != null) Trip.I.save(prefs);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
