# Hallazgos y pruebas

## Música con ZLink

- El permiso "Acceso a notificaciones" está **activo**, pero con música de Android Auto por ZLink
  `MediaSessionManager.getActiveSessions()` no da nada útil: **ZLink no publica una MediaSession**.
  Por eso el launcher no muestra la canción.
- Por Bluetooth puro (AVRCP) el estéreo sí muestra título, artista y tiempo, pero **no la portada**.
- Los **botones del volante sí cambian de canción** con ZLink y el launcher en pantalla. El estéreo
  los entrega por un canal propio.
- `AudioManager.dispatchMediaKeyEvent()` desde el launcher **no funcionó** con ZLink (versión 1).
- La versión 2 manda `ACTION_MEDIA_BUTTON` explícito al paquete de ZLink. **Sin probar**, porque
  la v2 no se pudo instalar. Hay un interruptor en Ajustes para volver a la vía estándar.
- Idea para investigar con ADB: `getevent -lt` y `logcat` mientras se pulsa el volante, para ver
  qué evento o intent usa el estéreo y replicarlo.

## Incidente: la pantalla no se apagó y se descargó la batería

- Con la **versión 1** del launcher como app de inicio, la pantalla **no se apagó** al quitar la
  llave, toda la noche, y **descargó la batería** del coche.
- La v1 **no** tiene servicios en segundo plano, wakelocks ni `FLAG_KEEP_SCREEN_ON`.
- **Hipótesis principal (sin confirmar):** en estas unidades, el **launcher de fábrica** (o un
  proceso ligado a él) maneja la suspensión cuando se quita la llave (ACC off). Al reemplazarlo
  como HOME, ese mecanismo deja de correr.
- **Otras posibilidades:** opción de retardo de apagado en Configuraciones de fábrica; cable ACC a
  corriente fija (descartado si con el launcher de fábrica sí se apaga).
- **Prueba pendiente:** con Inicio Auto desinstalado, ¿la pantalla se apaga sola al quitar la
  llave? Cesar aún no reporta el resultado.
- **Cómo investigarlo con ADB** (con el coche encendido y mirando la pantalla):
  - `adb shell cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME`
    da el launcher de fábrica.
  - `adb shell dumpsys package <launcher_fabrica>` muestra los receivers y servicios que declara.
    Busca acciones tipo ACC, POWER, SLEEP, `com.*.ACC_OFF`.
  - `adb logcat -v time` mientras se quita la llave muestra qué broadcasts o comandos aparecen.
  - `adb shell dumpsys power` muestra los wake locks activos y el estado de la pantalla.

## Instalación de APKs

- La **v2 no se pudo instalar** encima de la v1 ("no se pudo instalar"): **firmas distintas**.
  Cada runner de GitHub genera su propia llave de depuración. Solución pendiente: llave fija.
- El explorador de archivos del estéreo sí instala APKs desde USB ("Gestor de archivos").

## Rendimiento

- Hardware muy limitado (A7 de 32 bits, 2 GB). Evitar mapas, 3D, blur y animaciones pesadas.
- Optimización pendiente, toda reversible: animaciones a 0.5x, límite de procesos y desactivar
  apps que Cesar no use (él decide cuáles).

## Volumen

- Cesar siente el volumen bajo con Android Auto. Pendiente revisar "Configuración del volumen"
  (nivel por fuente), ajustes de ZLink y "Normalizar volumen" en YouTube Music.
