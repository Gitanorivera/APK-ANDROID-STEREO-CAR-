package app.autolauncher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

/**
 * Asigna sola la app de cada botón la primera vez, buscando por nombre entre las apps
 * instaladas en el estéreo (ZLINK, Musica Local, Bluetooth, Radio, Sound Effects...).
 * Solo llena los botones que no tienen app; lo que eligió el usuario no se toca.
 */
final class Defaults {

    private Defaults() {
    }

    /** Palabras a buscar en el nombre (sin acentos, minúsculas) o en el paquete. */
    private static String[] hints(Slot s) {
        switch (s) {
            case AUTO:
                return new String[]{"zlink", "carplay", "android auto"};
            case MUSIC:
                return new String[]{"musica local", "musica", "music"};
            case PHONE:
            case BLUETOOTH:
                return new String[]{"bluetooth", "bt phone", "telefono"};
            case RADIO:
                return new String[]{"radio"};
            case EQ:
                return new String[]{"sound effects", "ecualizador", "equalizer", "efectos de sonido"};
            case VEHICLE:
                return new String[]{"configuracion del vehiculo", "car settings", "carset"};
            default:
                return new String[0];
        }
    }

    /** Ejecutar fuera del hilo principal. */
    static void assignMissing(Context c, Prefs prefs) {
        PackageManager pm = c.getPackageManager();
        Intent q = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> apps = pm.queryIntentActivities(q, 0);
        String self = c.getPackageName();

        for (Slot s : Slot.values()) {
            if (prefs.shortcut(s) != null) continue;
            String found = null;
            // Se prueba cada pista en orden: la primera es la más específica.
            for (String hint : hints(s)) {
                for (ResolveInfo ri : apps) {
                    String pkg = ri.activityInfo.packageName;
                    if (pkg.equals(self)) continue;
                    String label = normalize(String.valueOf(ri.loadLabel(pm)));
                    if (label.equals(hint) || label.contains(hint) || pkg.toLowerCase(Locale.ROOT).contains(hint.replace(" ", ""))) {
                        found = pkg;
                        break;
                    }
                }
                if (found != null) break;
            }
            if (found != null) prefs.setShortcut(s, found);
        }
    }

    static String normalize(String s) {
        String n = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        return n.toLowerCase(Locale.ROOT).trim();
    }
}
