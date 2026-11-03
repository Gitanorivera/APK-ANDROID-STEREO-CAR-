package app.autolauncher;

import android.content.Context;
import android.content.res.Configuration;

import java.util.Calendar;

/**
 * Modo noche automático de 19:00 a 7:00: menos brillo y sin destellos para no deslumbrar.
 * Cada pantalla envuelve su contexto con {@link #wrap} para usar los colores de values-night.
 */
final class Night {

    private static final int FROM_HOUR = 19;
    private static final int TO_HOUR = 7;

    private Night() {
    }

    /** ¿Toca modo noche ahora, según la hora y el ajuste del usuario? */
    static boolean wanted(Context c) {
        if (!new Prefs(c).nightAuto()) return false;
        int h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        return h >= FROM_HOUR || h < TO_HOUR;
    }

    /** ¿Esta pantalla se dibujó en modo noche? */
    static boolean applied(Context c) {
        int mode = c.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return mode == Configuration.UI_MODE_NIGHT_YES;
    }

    static Context wrap(Context base) {
        Configuration cfg = new Configuration(base.getResources().getConfiguration());
        int night = wanted(base) ? Configuration.UI_MODE_NIGHT_YES : Configuration.UI_MODE_NIGHT_NO;
        cfg.uiMode = (cfg.uiMode & ~Configuration.UI_MODE_NIGHT_MASK) | night;
        return base.createConfigurationContext(cfg);
    }
}
