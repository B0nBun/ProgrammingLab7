package itmo.app.shared.entities;

public enum VehicleType {
    DRONE,
    SHIP,
    BICYCLE;

    public static VehicleType fromString(String string) {
        for (var value : VehicleType.values()) {
            if (value.toString().toLowerCase().equals(string.toLowerCase())) {
                return value;
            }
        }
        return null;
    }
}
