package app.autolauncher;

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

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        // Ignorado a propósito.
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        // Ignorado a propósito.
    }
}
