package itmo.app.shared.entities;

import java.util.stream.Stream;

public enum FuelType {
    GASOLINE,
    ELECTRICITY,
    MANPOWER,
    PLASMA,
    ANTIMATTER;

    public static FuelType fromString(String string) {
        if (string == null) return null;
        for (var value : FuelType.values()) {
            if (value.toString().toLowerCase().equals(string.toLowerCase())) {
                return value;
            }
        }
        return null;
    }

    public static String showIndexedList(String joiner) {
        var names = Stream.of(FuelType.values()).map(t -> t.name()).toList();
        String result = "";
        for (int i = 0; i < names.size(); i++) {
            result +=
                (i + 1) + ". " + names.get(i) + (i == names.size() - 1 ? "" : joiner);
        }
        return result;
    }
}
