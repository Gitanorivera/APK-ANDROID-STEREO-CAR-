# Inicio Auto — instrucciones para Claude Code

Proyecto personal de Cesar: rediseñar y optimizar el estéreo Android chino de su
**Hyundai Tucson 2016 2.0 GLS**. Habla con él en **español**, de forma clara y sin tecnicismos
innecesarios. Él no es desarrollador Android, pero sí entiende seguridad de la información.

Lee primero, en este orden:
1. `docs/ESTEREO.md`: qué hardware y software tiene el estéreo.
2. `docs/ERRORES.md`: **todos los errores que reportó Cesar**, con causa y estado.
3. `docs/HALLAZGOS.md`: lo que ya se probó y qué falló (incluye un incidente de batería).
4. `docs/PENDIENTES.md`: siguientes pasos, por prioridad.
5. `scripts/adb/LEEME.md`: cómo conectarse al estéreo con ADB y qué hace cada script.

## Reglas que no se rompen

1. **Nada irreversible sin confirmación explícita.** Antes de cada cambio en el estéreo, di qué
   vas a hacer, cómo se revierte, y espera un "sí".
2. **Respaldo primero.** Corre `scripts/adb/02_respaldo.sh` antes de cualquier cambio.
3. **Desactivar, nunca desinstalar** apps de fábrica (`pm disable-user --user 0`, se revierte con
   `pm enable`). Nunca `pm uninstall` sobre apps del sistema.
4. **No tocar:** firmware del MCU, ROMs, particiones, `Configuraciones de fábrica` del estéreo,
   ni el botón "predeterminadas". El chip (AC8227L) casi no tiene comunidad: un error lo deja inservible.
5. **No dejar el launcher propio como app de inicio con el coche apagado** hasta resolver el
   problema de suspensión (ver `docs/HALLAZGOS.md`). Ya descargó la batería una vez.
6. **La app no tiene internet** (`INTERNET` removido en el manifiesto) y no usa librerías de
   terceros. Mantenlo así salvo que Cesar lo apruebe explícitamente.
7. **Al terminar cada sesión de ADB**, apaga la depuración por red (`adb disconnect` y desactivar
   "Depuración USB" / ADB por red en el estéreo). Con ella activa, cualquiera en la red lo controla.
8. Nada de datos personales en el código: nombre y logotipo se configuran en la app y se borran
   con "Borrar mis datos" (el coche se venderá algún día con el estéreo).

## El launcher (este repositorio)

- App Android en Java, sin dependencias. Paquete `app.autolauncher`, minSdk 26, targetSdk 34.
- Código en `app/src/main/java/app/autolauncher/`, recursos en `app/src/main/res/`.
- Compila solo en GitHub Actions (`.github/workflows/compilar.yml`) en cada push a `main` y en
  cada pull request. El APK sale en **Actions → Artifacts → InicioAuto-apk**.
- Local: `./gradlew assembleRelease` (requiere Android SDK; con Android Studio basta).
- Firma: con los secretos `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS` y `KEY_PASSWORD`, el
  workflow firma `main` con la llave fija; sin ellos (y en pull requests) usa la llave de depuración,
  que **cambia en cada máquina**. Nunca subas el `.jks` ni contraseñas. Ver README, "Configurar la firma fija".
- `versionCode` = número de compilación de GitHub Actions (`GITHUB_RUN_NUMBER`).
- Diseños aprobados: mockup en el canvas de Claude ("Launcher auto 1024×600"), fila **V2**
  (menú lateral con texto, color cian, tablero GPS). Ya está implementado en el código actual.

## Cómo trabajar

- Cambios al código: rama nueva → pull request → que la compilación pase en verde → merge.
- Mensajes de commit y textos de la app en español.
- Cesar prueba en el coche: pídele fotos de la pantalla cuando algo no se vea bien.
