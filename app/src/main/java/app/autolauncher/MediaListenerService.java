package app.autolauncher;

import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

/**
 * Android exige este servicio para que una app pueda leer qué canción suena
 * (título, artista, tiempo) y controlar la reproducción.
 *
 * Intencionalmente vacío: NO lee, guarda ni envía notificaciones.
 * Además, la app no tiene permiso de internet.
 */
public class MediaListenerService extends NotificationListenerService {

    /** ¿El usuario activó "Acceso a notificaciones" para esta app? */
    static boolean isEnabled(Context c) {
        ComponentName me = new ComponentName(c, MediaListenerService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            NotificationManager nm = c.getSystemService(NotificationManager.class);
            return nm != null && nm.isNotificationListenerAccessGranted(me);
        }
        String flat = Settings.Secure.getString(c.getContentResolver(), "enabled_notification_listeners");
        if (flat == null) return false;
        for (String part : flat.split(":")) {
            if (me.equals(ComponentName.unflattenFromString(part))) return true;
        }
        return false;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        // Ignorado a propósito.
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        // Ignorado a propósito.
    }
}
