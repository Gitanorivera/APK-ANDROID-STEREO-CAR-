# El estéreo

Datos tomados de fotos de las pantallas "Información del sistema" y "Acerca del dispositivo",
y de la pantalla "Acerca de" de ZLink (26–27 de octubre de 2026).

## Hardware

| Dato | Valor |
|---|---|
| Vehículo | Hyundai Tucson 2016, 2.0 L, versión GLS (México) |
| Modelo de la unidad | F9211B (Android: `F9211B_00020_V001`) |
| Chip | Cortex-A7 4 núcleos a 1.5 GHz. ZLink reporta canal `ZHANGXUN-AC8227L`, así que es muy probable un **Autochips AC8227L** |
| RAM | 2 GB |
| Almacenamiento | 32 GB |
| Pantalla | 1024×600, LVDS (ZLink ve 1024×592 útiles por la barra) |
| MCU | 4.2 (X2) |
| CAR AUDIO | F7.3.7 (X2) |
| CAN BUS | 1.017, **desconectado** (no hay decodificador CAN instalado) |

## Software

| Dato | Valor |
|---|---|
| Android | 10 (nivel de parche: 1 de febrero de 2020) |
| Número de compilación | `android-trunk-p0` |
| Kernel | 4.14.116, compilado el 21 de octubre de 2022 |
| ZLink | 3.9.27 (zjinnova). Activado para CarPlay inalámbrico/cableado y Android Auto. ZLink reporta `Vr:30` (sin confirmar qué significa) |

## Apps instaladas (vistas en el cajón de apps)

Bluetooth, Calculadora, Calendario, CarSet, Chrome, Configuración del vehículo, desinstalación,
Downloads, Galería, Gestor de archivos, Grabadora de sonido, Maps, Musica Local, Net Navi,
Netflix, Play Store, Radio, Sound Effects, Theme, Video, YouTube, ZLINK.

No hay app de cámara: la cámara de reversa se activa sola al meter reversa.

Los nombres de paquete **no se conocen todavía**. `scripts/adb/01_diagnostico.sh` los obtiene.

## Menús del estéreo

- **Información del sistema:** botones "predeterminadas" (restablece todo, **no tocar**),
  "Android" y "Actualización MCU" (**no tocar**).
- **Configuraciones de fábrica:** protegido con contraseña (comunes: 8888, 3368, 1234, 1617).
  Puede tener opciones de retardo de apagado o suspensión relevantes para el problema de batería.
  Revisar solo con fotos y sin cambiar nada sin respaldo.
- **Configuración del volumen:** niveles por fuente (Android Auto / Bluetooth / radio).
