package app.autolauncher;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;

/**
 * Lee el GPS solo mientras Inicio Auto está en pantalla. Sin servicio en segundo plano:
 * al salir de la app o apagarse la pantalla se suelta, para no impedir que el estéreo
 * se duerma (docs/ERRORES.md #1). No guarda coordenadas ni tiene internet.
 */
final class GpsReader implements LocationListener {

    private final Context context;
    private final Prefs prefs;
    private LocationManager lm;
    private boolean listening;

    GpsReader(Context context, Prefs prefs) {
        this.context = context.getApplicationContext();
        this.prefs = prefs;
    }

    static boolean hasPermission(Context c) {
        return c.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    void start() {
        if (listening || !hasPermission(context)) return;
        lm = context.getSystemService(LocationManager.class);
        if (lm == null) return;
        try {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 0f, this, Looper.getMainLooper());
            listening = true;
        } catch (SecurityException | IllegalArgumentException ignored) {
            // Sin GPS o sin permiso: el tablero muestra "buscando señal" o "sin permiso".
        }
    }

    /** Suelta el GPS y guarda los totales del viaje. Se puede llamar varias veces. */
    void stop() {
        if (lm != null && listening) lm.removeUpdates(this);
        listening = false;
        Trip.I.save(prefs);
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
}
