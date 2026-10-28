# ADB con el estéreo

## 1. Instalar ADB en la laptop

Descarga **SDK Platform-Tools** del sitio oficial de Android
(<https://developer.android.com/tools/releases/platform-tools>), descomprime y agrega la carpeta
al PATH. Comprueba con `adb version`.

En Windows, corre los scripts desde **Git Bash** (viene con Git) o pide a Claude Code que ejecute
los comandos uno por uno.

## 2. Activar la depuración en el estéreo

1. *Configuraciones de Android → Acerca del dispositivo*: toca 7 veces "Número de compilación".
2. *Opciones de desarrollador*: activa **Depuración USB**. Si existe **ADB por red** o
   **Depuración inalámbrica**, actívala también.
3. Si no aparece, revisar *Configuraciones de fábrica* (algunas unidades lo tienen ahí). Solo mirar;
   no cambiar nada más.

## 3. Conectar

- **Por red (lo más probable):** laptop y estéreo en el mismo Wi-Fi (el hotspot del celular sirve).
  La IP del estéreo está en *Ajustes → Wi-Fi → (red) → Avanzado*.
  `adb connect <IP>:5555` y luego `adb devices`. El estéreo puede pedir aceptar la huella de la laptop.
- **Por cable:** los puertos USB de estas unidades suelen ser solo *host* (para memorias), así que
  probablemente no funcione. Intentar solo si el estéreo tiene un modo USB para PC.

Si hay más de un dispositivo, exporta `ANDROID_SERIAL=<IP>:5555` antes de correr los scripts.

## 4. Scripts (en orden)

| Script | Qué hace | ¿Cambia algo? |
|---|---|---|
| `01_diagnostico.sh` | Junta información: versión real, chip, RAM, apps y paquetes, launcher de fábrica, wake locks, receivers del launcher de fábrica, procesos que más consumen | **No**, solo lee |
| `02_respaldo.sh` | Guarda ajustes (`settings list`), apps activas y desactivadas, y el launcher actual, para poder revertir | **No**, solo lee |
| `03_optimizar.sh` | Animaciones a 0.5x y desactivar las apps de `apps_a_desactivar.txt`. Por defecto **solo muestra** lo que haría; aplica con `--aplicar` | Sí, reversible |
| `04_revertir.sh <carpeta_respaldo>` | Restaura animaciones y vuelve a activar las apps que se desactivaron | Sí, revierte |
| `05_ver_suspension.sh` | Graba el `logcat` y el estado de energía mientras se quita la llave, para investigar el error de la batería | **No**, solo lee |

Los resultados se guardan en `salidas/` (ignorado por git: puede contener nombres de redes o
dispositivos). **No subas esa carpeta al repositorio.**

## 5. Al terminar

`adb disconnect` y desactivar **Depuración USB** / ADB por red en el estéreo.
