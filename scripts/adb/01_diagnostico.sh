#!/usr/bin/env bash
# Solo LEE información del estéreo. No cambia nada.
source "$(dirname "$0")/comun.sh"
requiere_dispositivo
OUT=$(carpeta_salida diagnostico)

echo "Guardando diagnóstico en $OUT"
sh_ getprop                                   > "$OUT/getprop.txt"
sh_ cat /proc/cpuinfo                         > "$OUT/cpuinfo.txt"
sh_ cat /proc/meminfo                         > "$OUT/meminfo.txt"
sh_ df -h                                     > "$OUT/almacenamiento.txt"
sh_ pm list packages -f                       > "$OUT/paquetes_todos.txt"
sh_ pm list packages -3                       > "$OUT/paquetes_instalados_por_usuario.txt"
sh_ pm list packages -d                       > "$OUT/paquetes_desactivados.txt"
sh_ cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME > "$OUT/launcher_actual.txt"
sh_ cmd package query-activities --brief -a android.intent.action.MAIN -c android.intent.category.HOME > "$OUT/launchers_disponibles.txt"
sh_ dumpsys power                             > "$OUT/power.txt"
sh_ dumpsys activity services                 > "$OUT/servicios.txt"
sh_ dumpsys meminfo                           > "$OUT/meminfo_apps.txt"
sh_ top -b -n 1                               > "$OUT/top.txt"
sh_ settings list global                      > "$OUT/settings_global.txt"
sh_ settings list system                      > "$OUT/settings_system.txt"
sh_ settings list secure                      > "$OUT/settings_secure.txt"

# Etiquetas legibles de las apps con ícono (para mapear "Radio" -> paquete).
sh_ cmd package query-activities --brief -a android.intent.action.MAIN -c android.intent.category.LAUNCHER > "$OUT/apps_con_icono.txt"

# Receivers y servicios del launcher de fábrica (pista para el error de la batería).
HOME_PKG=$(tail -n 1 "$OUT/launcher_actual.txt" | cut -d/ -f1)
if [ -n "$HOME_PKG" ] && [ "$HOME_PKG" != "app.autolauncher" ]; then
  sh_ dumpsys package "$HOME_PKG"             > "$OUT/launcher_fabrica_${HOME_PKG}.txt"
fi

echo "Resumen:"
grep -E "ro.product.model|ro.build.version.release|ro.board.platform|ro.hardware" "$OUT/getprop.txt" || true
grep -E "MemTotal|MemAvailable" "$OUT/meminfo.txt" || true
echo "Launcher actual: $(tail -n 1 "$OUT/launcher_actual.txt")"
echo "Wake locks activos:"; grep -iA20 "Wake Locks:" "$OUT/power.txt" | head -25 || true
