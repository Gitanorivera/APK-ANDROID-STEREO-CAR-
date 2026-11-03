package app.autolauncher;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

/**
 * Lee el GPS solo mientras el inicio está en pantalla. Sin servicio en segundo plano
 * para no impedir que el estéreo se duerma al quitar la llave.
 * No guarda coordenadas: solo los totales del viaje (ver Trip).
 */
final class TripGps implements LocationListener {

    private static final long SAVE_EVERY_MS = 30_000L;

    private final LocationManager lm;
    private final Prefs prefs;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean listening;

    /** Guarda cada 30 s: al quitar la llave el estéreo puede apagarse sin avisar. */
    private final Runnable saver = new Runnable() {
        @Override
        public void run() {
            Trip.I.save(prefs);
            handler.postDelayed(this, SAVE_EVERY_MS);
        }
    };

    TripGps(Context c, Prefs prefs) {
        this.lm = c.getApplicationContext().getSystemService(LocationManager.class);
        this.prefs = prefs;
    }

    static boolean hasPermission(Context c) {
        return c.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    void start(Context c) {
        if (listening || lm == null || !hasPermission(c)) return;
        try {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 0f, this, Looper.getMainLooper());
            listening = true;
            handler.postDelayed(saver, SAVE_EVERY_MS);
        } catch (SecurityException | IllegalArgumentException e) {
            listening = false;
        }
    }

    void stop() {
        handler.removeCallbacks(saver);
        if (listening && lm != null) lm.removeUpdates(this);
        if (listening) Trip.I.save(prefs);
        listening = false;
    }

    @Override
    public void onLocationChanged(Location location) {
        Trip.I.onLocation(location);
    }

    // Necesarios en Android 10 y anteriores (ahí no tienen implementación por defecto).
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }
}
