# Errores reportados por Cesar

Cada error con versión, causa (confirmada o probable) y estado. Actualiza este archivo cuando
cambie algo.

| # | Error reportado | Versión | Causa | Estado |
|---|---|---|---|---|
| 1 | **La pantalla nunca se apagó y descargó la batería del coche** (toda la noche, con el launcher como inicio) | v1 | **Probable:** al reemplazar el inicio de fábrica deja de correr lo que duerme el estéreo con ACC off. Sin confirmar | **Abierto, prioridad 1.** No usar el launcher como HOME hasta resolver |
| 2 | **"No se pudo instalar"** la v2 | v2 | **Confirmada:** firma distinta a la v1 (cada runner de GitHub genera su propia llave de depuración) | Abierto. Mientras tanto, desinstalar antes de instalar. Solución: llave fija (`PENDIENTES.md` §3) |
| 3 | **No se ve la información de la canción** con ZLink (título, artista) | v1 | **Confirmada:** ZLink no publica MediaSession; el permiso de notificaciones sí estaba activo | Sin solución directa. La tarjeta muestra "Música en Android Auto" |
| 4 | **Los botones de la pantalla (anterior/pausa/siguiente) no cambian la canción** con ZLink | v1 | **Confirmada:** `AudioManager.dispatchMediaKeyEvent` no le llega a ZLink. El volante sí funciona (canal propio del estéreo) | v2 manda `ACTION_MEDIA_BUTTON` directo a ZLink, **sin probar** (v2 no se instaló) |
| 5 | **Al apretar un botón no abre la app, manda a "Todas las apps"** | v1 | **Confirmada:** los botones sin app asignada abrían el selector, y no se entendía | v2 asigna solo por nombre (ZLINK, Musica Local, Bluetooth, Radio, Sound Effects, Configuración del vehículo). Sin probar |
| 6 | **El botón "Cámara" llevaba al ecualizador** y no tiene caso | v1 | El estéreo no tiene app de cámara (la de reversa es automática). Se le asignó otra app | **Resuelto en v2:** se quitó el botón |
| 7 | **"No se acepta el formato del archivo"** al primer intento de instalar | v1 | Probable: se abrió el `.zip` del artifact en lugar del `.apk`, o el explorador no reconocía el tipo | **Resuelto:** se instaló desde el Gestor de archivos |
| 8 | **La compilación falló en 13 s** en GitHub | — | **Confirmada:** al subir por la web, GitHub aplanó las carpetas | **Resuelto:** repo ordenado en carpetas (PR #1) |
| 9 | **El volumen se escucha bajo** con Android Auto | — | Probable: niveles por fuente del estéreo, ZLink, "Normalizar volumen" de YouTube Music o volumen del teléfono | Abierto. Revisar ajustes; el launcher no puede subirlo por encima del máximo |
| 10 | **No se ve la portada** del álbum por Bluetooth | — | Probable: el estéreo solo soporta AVRCP sin portada | Sin solución. Se muestra un cuadro del color de acento |

## Decisiones de Cesar (no reabrir sin preguntarle)

- **Sin OBD-II:** "es demasiado".
- **Sin mapa propio** en el tablero y **sin botón extra a ZLink** para mapas: demasiados botones,
  y se sacrificaba mucho.
- **Sin "HYUNDAI TUCSON 2016"** en la barra lateral.
- **Se quedan:** bienvenida con nombre editable y logo, color de acento configurable, asignación de
  botones, tablero GPS con tarjetas y diseño V2.
