#!/usr/bin/env bash
# Respaldo para poder revertir. Solo LEE.
source "$(dirname "$0")/comun.sh"
requiere_dispositivo
OUT=$(carpeta_salida respaldo)

sh_ settings list global  > "$OUT/settings_global.txt"
sh_ settings list system  > "$OUT/settings_system.txt"
sh_ settings list secure  > "$OUT/settings_secure.txt"
sh_ pm list packages -e   > "$OUT/paquetes_activos.txt"
sh_ pm list packages -d   > "$OUT/paquetes_desactivados.txt"
sh_ cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME > "$OUT/launcher_actual.txt"
for k in window_animation_scale transition_animation_scale animator_duration_scale; do
  echo "$k=$(sh_ settings get global "$k")" >> "$OUT/animaciones.txt"
done
echo "Respaldo listo en: $OUT"
echo "Para revertir: ./04_revertir.sh \"$OUT\""
