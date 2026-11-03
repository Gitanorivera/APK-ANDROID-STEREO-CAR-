# Inicio Auto

Launcher para pantallas Android de auto a 1024×600, con el **diseño premium**: tarjetas de vidrio sobre azul noche, acento cian y modo noche automático.

## Qué hace (versión 3)

- **Diseño premium:** menú lateral de vidrio, reloj grande, botón de Android Auto con brillo, tarjeta de música cuyo resplandor toma el color de la portada, y accesos rápidos con chips de color.
- **Modo noche automático** (19:00 a 7:00): menos brillo y sin destellos para no deslumbrar. Se puede apagar en Ajustes.
- **Bienvenida fija:** se queda en pantalla hasta que tocas **Inicio** o cualquier parte de la pantalla.
- Los botones responden al tocarlos (se encogen un poco). Todo usa fuentes del sistema y nada de desenfoque, para que vaya fluido en el estéreo.

- **Menú lateral:** INICIO, TABLERO, ANDROID AUTO, MÚSICA, TELÉFONO, APPS y AJUSTES.
- **Inicio:** hora grande con fecha, velocidad GPS, botón de Android Auto y reproductor.
- **Accesos rápidos:** RADIO, BLUETOOTH, ECUALIZADOR y VEHÍCULO.
- **Tablero:** velocímetro, viaje actual (distancia y tiempo), velocidad promedio y máxima, y altitud. Todo sale del GPS del estéreo.
- **Botones asignados solos:** el launcher busca ZLINK, Musica Local, Bluetooth, Radio, Sound Effects y Configuración del vehículo. Para cambiar la app de un botón, mantenlo presionado.
- **Música con ZLink:** si ZLink no publica la canción, los botones mandan la señal directo a ZLink. Se puede cambiar en Ajustes.
- **Bienvenida al encender:** nombre opcional y editable, con tu logotipo. Se queda hasta que la tocas.
- **Borrar mis datos:** quita nombre, logotipo, accesos y totales del viaje, por ejemplo antes de vender el auto.

## Seguridad y privacidad

| Qué | Cómo |
|---|---|
| Internet | **Sin permiso de internet**, bloqueado de forma explícita en el manifiesto. La app no puede enviar nada fuera de la pantalla. |
| Dependencias | **Ninguna librería de terceros.** Solo usa el código de Android, así que no hay código ajeno escondido. |
| Permisos | `ACCESS_WIFI_STATE` y `BLUETOOTH`, para mostrar si están encendidos. **Ubicación (GPS)**, para velocidad, distancia y altitud: no se guarda ninguna coordenada, solo los totales del viaje. El GPS se lee **solo con el inicio en pantalla** (sin servicio en segundo plano), para no impedir que el estéreo se duerma. |
| Acceso a notificaciones | Android lo exige para leer qué canción suena. El servicio está vacío: **no lee ni guarda notificaciones**. |
| Componentes | Solo la pantalla de inicio está abierta al sistema, porque así funciona un launcher. Ajustes y el cajón de apps son internos. El servicio de música solo acepta conexiones del sistema. |
| Datos | Se guardan en la memoria privada de la app y **sin respaldo** (`allowBackup=false`). |
| Nombre | Se valida: solo letras, espacios, punto, apóstrofo y guion, con un máximo de 30 caracteres. |
| Logotipo | Se limita a 10 MB, se reduce a 512 px y se vuelve a guardar como PNG. No se conserva nada del archivo original. |
| Compilación | La versión release no es depurable y el código va reducido y ofuscado con R8. |

**Firma:** si el repositorio tiene los secretos de la llave fija (ver abajo), la versión de `main` se firma con ella y cada versión nueva se instala encima de la anterior. Sin esos secretos se firma con la llave de depuración de quien compila, y hay que desinstalar antes de instalar.

### Configurar la firma fija (una sola vez)

1. En tu computadora, **fuera de la carpeta del repositorio**, crea la llave (te pedirá contraseñas; guárdalas en tu gestor de contraseñas):
   `keytool -genkeypair -v -keystore inicio-auto.jks -alias inicio-auto -keyalg RSA -keysize 4096 -validity 10000`
2. Conviértela a texto: `base64 -w0 inicio-auto.jks > llave.txt` (en macOS: `base64 -i inicio-auto.jks -o llave.txt`).
3. En GitHub: *Settings → Secrets and variables → Actions → New repository secret*, crea `KEYSTORE_BASE64` (contenido de `llave.txt`), `KEYSTORE_PASSWORD`, `KEY_ALIAS` (`inicio-auto`) y `KEY_PASSWORD`.
4. Borra `llave.txt` y respalda `inicio-auto.jks` en un lugar seguro: **si pierdes la llave, no podrás actualizar la app sin desinstalarla.**
5. La primera versión con la llave nueva requiere desinstalar la anterior una última vez.

## Cómo compilar

Cada cambio que llega a `main` se compila solo en GitHub Actions ("Compilar APK").
Para descargar el APK: pestaña **Actions** → la compilación más reciente en verde → **Artifacts** → **InicioAuto-apk**. La versión (por ejemplo 2.37) es el número de esa compilación.

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
