package itmo.app.shared.entities;

import java.time.LocalDate;

public record Vehicle(
    Integer id,
    String name,
    Coordinates coordinates,
    LocalDate creationDate,
    Integer enginePower,
    VehicleType type,
    FuelType fuelType
) {}
