# Pendientes, por prioridad

## 1. Resolver la suspensión (bloquea todo lo demás)

- [ ] Confirmar con Cesar: con el launcher de fábrica, ¿la pantalla se apaga al quitar la llave?
- [ ] Con ADB: identificar el launcher de fábrica y cómo el estéreo detecta ACC off
      (ver "Cómo investigarlo" en `HALLAZGOS.md`).
- [ ] Decidir la estrategia según lo que se encuentre:
  - Si el launcher de fábrica maneja la suspensión, Inicio Auto **no** reemplaza HOME. Se abre como
    app al encender (por ejemplo con un receiver de `BOOT_COMPLETED` o del evento de encendido del
    estéreo) y el inicio de fábrica sigue siendo HOME.
  - O reproducir en Inicio Auto lo que hace el de fábrica, solo si es una acción documentada y segura.
- [ ] Probar siempre de día: quitar la llave y confirmar que la pantalla se apaga en menos de 2 minutos.

## 2. Corregir riesgos de la versión 2 antes de reinstalar

- [x] **Quitar `TripService`** (servicio en primer plano con GPS continuo). Ahora `TripGps` lee el GPS
      solo mientras el launcher está en pantalla (`onResume`/`onPause`) y lo suelta en `ACTION_SCREEN_OFF`.
- [x] Quitar los permisos `FOREGROUND_SERVICE*` del manifiesto.
- [x] Revisar que nada use `FLAG_KEEP_SCREEN_ON` ni wake locks (revisado: no hay ninguno).

## 3. Firma fija para poder actualizar sin desinstalar

- [x] Workflow y `app/build.gradle` listos para usar la llave fija desde secretos de GitHub.
- [ ] **Cesar:** crear la llave **fuera del repo** y cargar los secretos (`KEYSTORE_BASE64`,
      `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`). Pasos en el README. Nunca subir el `.jks`
      ni las contraseñas.
- [x] `versionCode` automático con el número de compilación.
- [ ] Mientras tanto: desinstalar la versión anterior antes de instalar una nueva.

## 4. Música con ZLink

- [ ] Probar la v2 (botones → `ACTION_MEDIA_BUTTON` directo a ZLink) y el interruptor en Ajustes.
- [ ] Si no funciona: con ADB ver cómo llegan las teclas del volante (`getevent -lt`, `logcat`)
      y replicarlo.

## 5. Optimización del sistema con ADB (reversible)

- [ ] Correr `01_diagnostico.sh` y `02_respaldo.sh`.
- [ ] Proponer a Cesar la lista de apps a desactivar. Él elige; nada de apps de radio, Bluetooth,
      ZLink, cámara, MCU, CarSet, Configuración del vehículo ni servicios del sistema.
- [ ] Aplicar con `03_optimizar.sh` (por defecto solo muestra; aplica con `--aplicar`).

## 6. Al final de cada sesión

- [ ] `adb disconnect`, apagar "Depuración USB" y ADB por red en el estéreo.
