#!/usr/bin/env bash
# Investiga el error de la batería. Solo LEE.
# Uso: con el coche ENCENDIDO, corre el script y, cuando lo indique, quita la llave.
# Vuelve a encender antes de 2 minutos si la pantalla no se apaga.
source "$(dirname "$0")/comun.sh"
requiere_dispositivo
OUT=$(carpeta_salida suspension)

sh_ dumpsys power > "$OUT/power_antes.txt"
"$ADB" logcat -c || true
echo "Grabando 90 segundos. QUITA LA LLAVE AHORA y observa la pantalla."
( "$ADB" logcat -v time > "$OUT/logcat.txt" 2>&1 & echo $! > "$OUT/.pid" )
sleep 90
kill "$(cat "$OUT/.pid")" 2>/dev/null || true; rm -f "$OUT/.pid"
sh_ dumpsys power > "$OUT/power_despues.txt" || echo "(sin conexión: el estéreo pudo haberse dormido)" > "$OUT/power_despues.txt"

echo "Posibles pistas (ACC, sleep, suspend, standby, screen):"
# "acc" solo como palabra o con _ (ACC_OFF, acc_on), no dentro de "access" o "account".
grep -iE "(^|[^a-z])acc([^a-z]|$)|acc_|sleep|suspend|standby|screen_off|goToSleep|wakelock" "$OUT/logcat.txt" | head -60 || true
echo "Guardado en $OUT"
