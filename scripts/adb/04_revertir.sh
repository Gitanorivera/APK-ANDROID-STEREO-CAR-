#!/usr/bin/env bash
# Revierte usando un respaldo de 02_respaldo.sh: animaciones y apps desactivadas por nosotros.
source "$(dirname "$0")/comun.sh"
requiere_dispositivo
R="${1:?Uso: ./04_revertir.sh <carpeta_respaldo>}"
[ -f "$R/animaciones.txt" ] || { echo "No es una carpeta de respaldo válida: $R" >&2; exit 1; }

while IFS='=' read -r k v; do
  if [ "$v" = "null" ] || [ -z "$v" ]; then sh_ settings delete global "$k" >/dev/null; else sh_ settings put global "$k" "$v"; fi
  echo "Restaurado $k"
done < "$R/animaciones.txt"

# Reactiva lo que estaba activo en el respaldo y ahora está desactivado.
ACTUAL=$(sh_ pm list packages -d)
grep -o 'package:.*' "$R/paquetes_activos.txt" | cut -d: -f2 | while read -r pkg; do
  if echo "$ACTUAL" | grep -qx "package:$pkg"; then
    sh_ pm enable "$pkg"; echo "Reactivado $pkg"
  fi
done
echo "Listo."
