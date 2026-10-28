#!/usr/bin/env bash
# Funciones compartidas. Se carga con: source "$(dirname "$0")/comun.sh"
set -euo pipefail

ADB="${ADB:-adb}"

requiere_dispositivo() {
  if ! command -v "$ADB" >/dev/null 2>&1; then
    echo "No encuentro 'adb'. Instala Platform-Tools (ver LEEME.md)." >&2
    exit 1
  fi
  local n
  n=$("$ADB" devices | awk 'NR>1 && $2=="device"' | wc -l | tr -d ' ')
  if [ "$n" = "0" ]; then
    echo "No hay dispositivo conectado. Usa: adb connect <IP>:5555" >&2
    exit 1
  fi
  if [ "$n" != "1" ] && [ -z "${ANDROID_SERIAL:-}" ]; then
    echo "Hay $n dispositivos. Define ANDROID_SERIAL=<IP>:5555" >&2
    exit 1
  fi
}

sh_() { "$ADB" shell "$@" 2>&1 | tr -d '\r'; }

carpeta_salida() {
  local base
  base="$(cd "$(dirname "$0")" && pwd)/salidas/$1_$(date +%Y%m%d_%H%M%S)"
  mkdir -p "$base"
  echo "$base"
}
