package itmo.app.shared.entities;

public enum FuelType {
    GASOLINE,
    ELECTRICITY,
    MANPOWER,
    PLASMA,
    ANTIMATTER;

    public static FuelType fromString(String string) {
        for (var value : FuelType.values()) {
            if (value.toString().toLowerCase().equals(string.toLowerCase())) {
                return value;
            }
        }
        return null;
    }
}
