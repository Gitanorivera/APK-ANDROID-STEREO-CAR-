package app.autolauncher;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.regex.Pattern;

/** Ajustes guardados solo en la memoria privada de la app (sin respaldo en la nube). */
final class Prefs {

    static final int[] ACCENTS = {0xFF45D8F6, 0xFF34D399, 0xFFFFB020, 0xFF5AB0FF};
    static final int MAX_NAME = 30;

    // Letras (incluye acentos y ñ), espacios, punto, apóstrofo y guion.
    private static final Pattern NAME_OK = Pattern.compile("^[\\p{L}\\p{M} .'-]*$");

    private static final String FILE = "launcher";
    private static final String K_NAME = "name";
    private static final String K_WELCOME = "welcome";
    private static final String K_ACCENT = "accent";
    private static final String K_PREVIEW = "preview_welcome";
    private static final String K_SLOT = "slot_";

    private final SharedPreferences sp;

    Prefs(Context c) {
        sp = c.getApplicationContext().getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    String name() {
        String n = sp.getString(K_NAME, "");
        String clean = sanitizeName(n);
        return clean == null ? "" : clean;
    }

    void setName(String clean) {
        sp.edit().putString(K_NAME, clean).apply();
    }

    boolean welcomeEnabled() {
        return sp.getBoolean(K_WELCOME, true);
    }

    void setWelcomeEnabled(boolean on) {
        sp.edit().putBoolean(K_WELCOME, on).apply();
    }

    int accentIndex() {
        int i = sp.getInt(K_ACCENT, 0);
        return (i < 0 || i >= ACCENTS.length) ? 0 : i;
    }

    int accent() {
        return ACCENTS[accentIndex()];
    }

    void setAccentIndex(int i) {
        if (i >= 0 && i < ACCENTS.length) sp.edit().putInt(K_ACCENT, i).apply();
    }

    String shortcut(Slot s) {
        return sp.getString(K_SLOT + s.name(), null);
    }

    void setShortcut(Slot s, String pkg) {
        if (pkg == null) sp.edit().remove(K_SLOT + s.name()).apply();
        else sp.edit().putString(K_SLOT + s.name(), pkg).apply();
    }

    /** Pide a la pantalla de inicio que muestre la bienvenida una vez (vista previa). */
    void requestPreview() {
        sp.edit().putBoolean(K_PREVIEW, true).apply();
    }

    boolean consumePreview() {
        if (!sp.getBoolean(K_PREVIEW, false)) return false;
        sp.edit().remove(K_PREVIEW).apply();
        return true;
    }

    /** Modo noche automático de 19:00 a 7:00. */
    boolean nightAuto() {
        return sp.getBoolean("night_auto", true);
    }

    void setNightAuto(boolean on) {
        sp.edit().putBoolean("night_auto", on).apply();
    }

    /** Botones de música: mandar directo a ZLink (true) o por la vía estándar del sistema. */
    boolean mediaDirect() {
        return sp.getBoolean("media_direct", true);
    }

    void setMediaDirect(boolean on) {
        sp.edit().putBoolean("media_direct", on).apply();
    }

    // Totales del viaje (sin coordenadas: solo distancia, tiempo y velocidad máxima).
    float tripDistanceM() {
        return sp.getFloat("trip_dist", 0f);
    }

    long tripMovingMs() {
        return sp.getLong("trip_ms", 0L);
    }

    float tripMaxKmh() {
        return sp.getFloat("trip_max", 0f);
    }

    void saveTrip(float distanceM, long movingMs, float maxKmh) {
        sp.edit().putFloat("trip_dist", distanceM).putLong("trip_ms", movingMs)
                .putFloat("trip_max", maxKmh).apply();
    }

    /** Borra todo: nombre, colores, accesos directos. */
    void clearAll() {
        sp.edit().clear().commit();
    }

    /**
     * Limpia y valida el nombre del saludo.
     * @return el nombre limpio (puede ser vacío) o null si no es válido.
     */
    static String sanitizeName(String raw) {
        if (raw == null) return "";
        String s = raw.trim().replaceAll("\\s+", " ");
        if (s.length() > MAX_NAME) return null;
        if (!NAME_OK.matcher(s).matches()) return null;
        return s;
    }
}
