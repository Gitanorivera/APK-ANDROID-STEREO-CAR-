package app.autolauncher;

/** Accesos directos configurables. */
enum Slot {
    AUTO("Android Auto"),
    MUSIC("Música"),
    PHONE("Teléfono"),
    CAMERA("Cámara"),
    EQ("Ecualizador"),
    BLUETOOTH("Bluetooth");

    final String label;

    Slot(String label) {
        this.label = label;
    }

    static Slot from(String name) {
        if (name == null) return null;
        try {
            return Slot.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
