package app.autolauncher;

import android.location.Location;
import android.os.SystemClock;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Datos del GPS para el tablero: velocidad, rumbo, altitud y totales del viaje.
 * Solo se guardan totales (distancia, tiempo en movimiento y velocidad máxima),
 * nunca coordenadas. Todo vive en el hilo principal.
 */
final class Trip {

    interface Listener {
        void onTripUpdate();
    }

    static final Trip I = new Trip();

    private static final float GOOD_ACCURACY_M = 30f;
    private static final float MOVING_KMH = 3f;

    float speedKmh;
    float bearing;
    boolean hasBearing;
    double altitudeM;
    boolean hasAltitude;
    private long lastFixElapsed;

    double distanceM;
    long movingMs;
    float maxKmh;

    private Location last;
    private boolean loaded;
    private final List<Listener> listeners = new CopyOnWriteArrayList<>();

    private Trip() {
    }

    void load(Prefs p) {
        if (loaded) return;
        loaded = true;
        distanceM = p.tripDistanceM();
        movingMs = p.tripMovingMs();
        maxKmh = p.tripMaxKmh();
    }

    void save(Prefs p) {
        p.saveTrip((float) distanceM, movingMs, maxKmh);
    }

    void reset(Prefs p) {
        distanceM = 0;
        movingMs = 0;
        maxKmh = 0;
        last = null;
        save(p);
        notifyListeners();
    }

    void addListener(Listener l) {
        listeners.add(l);
    }

    void removeListener(Listener l) {
        listeners.remove(l);
    }

    void onLocation(Location l) {
        float accuracy = l.hasAccuracy() ? l.getAccuracy() : 999f;
        speedKmh = l.hasSpeed() ? l.getSpeed() * 3.6f : 0f;
        hasBearing = l.hasBearing() && speedKmh > 5f;
        bearing = l.getBearing();
        hasAltitude = l.hasAltitude();
        altitudeM = l.getAltitude();
        lastFixElapsed = SystemClock.elapsedRealtime();

        if (accuracy <= GOOD_ACCURACY_M) {
            if (last != null) {
                long dt = (l.getElapsedRealtimeNanos() - last.getElapsedRealtimeNanos()) / 1_000_000L;
                if (speedKmh >= MOVING_KMH && dt > 0 && dt < 10_000) {
                    float d = last.distanceTo(l);
                    if (d < 300f) {
                        distanceM += d;
                        movingMs += dt;
                    }
                }
            }
            last = l;
            if (speedKmh > maxKmh && speedKmh < 250f) maxKmh = speedKmh;
        }
        notifyListeners();
    }

    boolean hasFix() {
        return lastFixElapsed > 0 && SystemClock.elapsedRealtime() - lastFixElapsed < 5000;
    }

    float avgKmh() {
        return movingMs > 60_000 ? (float) (distanceM / (movingMs / 1000.0) * 3.6) : 0f;
    }

    static String heading(float deg) {
        String[] names = {"NORTE", "NORESTE", "ESTE", "SURESTE", "SUR", "SUROESTE", "OESTE", "NOROESTE"};
        int i = Math.round(((deg % 360f) + 360f) % 360f / 45f) % 8;
        return "RUMBO " + names[i];
    }

    private void notifyListeners() {
        for (Listener l : listeners) l.onTripUpdate();
    }
}
