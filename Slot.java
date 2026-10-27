package app.autolauncher;

/** Accesos directos configurables. */
enum Slot {
    AUTO("Android Auto"),
    MUSIC("Música"),
    PHONE("Teléfono"),
    RADIO("Radio"),
    BLUETOOTH("Bluetooth"),
    EQ("Ecualizador"),
    VEHICLE("Vehículo");

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
