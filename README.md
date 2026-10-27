# Inicio Auto

Launcher para pantallas Android de auto a 1024×600, con el diseño final (opción A con reloj grande y el botón de Android Auto de la opción B).

## Qué hace (versión 2)

- **Menú lateral:** INICIO, TABLERO, ANDROID AUTO, MÚSICA, TELÉFONO, APPS y AJUSTES.
- **Inicio:** hora grande con fecha, velocidad GPS, botón de Android Auto y reproductor.
- **Accesos rápidos:** RADIO, BLUETOOTH, ECUALIZADOR y VEHÍCULO.
- **Tablero:** velocímetro, viaje actual (distancia y tiempo), velocidad promedio y máxima, y altitud. Todo sale del GPS del estéreo.
- **Botones asignados solos:** el launcher busca ZLINK, Musica Local, Bluetooth, Radio, Sound Effects y Configuración del vehículo. Para cambiar la app de un botón, mantenlo presionado.
- **Música con ZLink:** si ZLink no publica la canción, los botones mandan la señal directo a ZLink. Se puede cambiar en Ajustes.
- **Bienvenida al encender:** nombre opcional y editable, con tu logotipo.
- **Borrar mis datos:** quita nombre, logotipo, accesos y totales del viaje, por ejemplo antes de vender el auto.

## Seguridad y privacidad

| Qué | Cómo |
|---|---|
| Internet | **Sin permiso de internet**, bloqueado de forma explícita en el manifiesto. La app no puede enviar nada fuera de la pantalla. |
| Dependencias | **Ninguna librería de terceros.** Solo usa el código de Android, así que no hay código ajeno escondido. |
| Permisos | `ACCESS_WIFI_STATE` y `BLUETOOTH`, para mostrar si están encendidos. **Ubicación (GPS)**, para velocidad, distancia y altitud: no se guarda ninguna coordenada, solo los totales del viaje. Un servicio con aviso fijo sigue midiendo el viaje mientras usas ZLink. |
| Acceso a notificaciones | Android lo exige para leer qué canción suena. El servicio está vacío: **no lee ni guarda notificaciones**. |
| Componentes | Solo la pantalla de inicio está abierta al sistema, porque así funciona un launcher. Ajustes y el cajón de apps son internos. El servicio de música solo acepta conexiones del sistema. |
| Datos | Se guardan en la memoria privada de la app y **sin respaldo** (`allowBackup=false`). |
| Nombre | Se valida: solo letras, espacios, punto, apóstrofo y guion, con un máximo de 30 caracteres. |
| Logotipo | Se limita a 10 MB, se reduce a 512 px y se vuelve a guardar como PNG. No se conserva nada del archivo original. |
| Compilación | La versión release no es depurable y el código va reducido y ofuscado con R8. |

**Riesgo pendiente:** la versión release se firma con la llave de depuración de quien compila. Para uso personal está bien. Si algún día la distribuyes, crea tu propio keystore y cambia `signingConfig` en `app/build.gradle`.

## Cómo compilar

Cada cambio que llega a `main` se compila solo en GitHub Actions ("Compilar APK").
Para descargar el APK: pestaña **Actions** → la compilación más reciente en verde → **Artifacts** → **InicioAuto-apk**.

También se puede abrir la carpeta en Android Studio y compilar con `./gradlew assembleRelease`.

## Estructura

```
app/
  build.gradle
  proguard-rules.pro
  src/main/
    AndroidManifest.xml
    java/app/autolauncher/   código de la app
    res/drawable/            íconos y fondos
    res/layout/              pantallas
    res/mipmap-anydpi-v26/   ícono de la app
    res/values/              textos, colores y estilos
gradle/wrapper/              Gradle wrapper
.github/workflows/           compilación automática
```

## Cómo instalar en la pantalla

1. Copia el APK a una memoria USB y conéctala a la pantalla.
2. Ábrelo con el explorador de archivos. Si lo pide, permite "instalar apps de origen desconocido".
3. Presiona el botón **Inicio**, elige **Inicio Auto** y luego **Siempre**.
4. Toca **"Permitir mostrar la canción"** y activa *Inicio Auto · canción en pantalla*.
5. Toca el botón de Android Auto y elige **ZLink**.
6. En *Ajustes*, escribe tu nombre si quieres y elige tu logotipo.

### Para volver al launcher original

Entra a *Ajustes de Android → Apps → Apps predeterminadas → App de inicio* y elige el original. También puedes desinstalar Inicio Auto.

## Notas sobre tu pantalla

- Con ZLink, los botones de música funcionan con la misma señal que los botones del volante.
- Ver el título depende de si la fuente lo publica. En tus pruebas, por Bluetooth llegan el título, el artista y el tiempo, pero no la portada. Si no llega imagen, se muestra un cuadro del color de acento.
- La bienvenida aparece al arrancar el sistema y al encender la pantalla si estuvo apagada al menos 5 minutos. Algunas unidades "duermen" de forma distinta. Si no te aparece al prender el auto, avísame y lo ajustamos.
