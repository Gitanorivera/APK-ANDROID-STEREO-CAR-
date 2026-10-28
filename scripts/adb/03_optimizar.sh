#!/usr/bin/env bash
# Optimización reversible. Por defecto SOLO MUESTRA lo que haría. Aplica con --aplicar.
source "$(dirname "$0")/comun.sh"
requiere_dispositivo
APLICAR=0
[ "${1:-}" = "--aplicar" ] && APLICAR=1

hacer() {
  if [ "$APLICAR" = 1 ]; then echo "+ adb shell $*"; sh_ "$@"; else echo "(simulación) adb shell $*"; fi
}

if [ "$APLICAR" = 1 ] && [ -z "$(ls -d "$(dirname "$0")"/salidas/respaldo_* 2>/dev/null)" ]; then
  echo "Primero corre ./02_respaldo.sh" >&2; exit 1
fi

for k in window_animation_scale transition_animation_scale animator_duration_scale; do
  hacer settings put global "$k" 0.5
done

LISTA="$(dirname "$0")/apps_a_desactivar.txt"
grep -vE '^\s*(#|$)' "$LISTA" | while read -r pkg; do
  # Solo nombres de paquete válidos; evita inyectar comandos desde el archivo.
  if [[ "$pkg" =~ ^[a-zA-Z][a-zA-Z0-9_]*(\.[a-zA-Z0-9_]+)+$ ]]; then
    hacer pm disable-user --user 0 "$pkg"
  else
    echo "Ignorado (no es un paquete válido): $pkg" >&2
  fi
done

[ "$APLICAR" = 1 ] || echo "Nada se cambió. Revisa la lista y corre: ./03_optimizar.sh --aplicar"
