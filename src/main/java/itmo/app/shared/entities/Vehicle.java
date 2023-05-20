package itmo.app.shared.entities;

import itmo.app.shared.fieldschema.FieldSchema;
import itmo.app.shared.fieldschema.FieldSchemaEnum;
import itmo.app.shared.fieldschema.FieldSchemaNum;
import itmo.app.shared.fieldschema.FieldSchemaString;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Scanner;

public record Vehicle(
    Integer id,
    String name,
    String createdBy,
    Coordinates coordinates,
    LocalDate creationDate,
    Integer enginePower,
    VehicleType type,
    FuelType fuelType
)
    implements Comparable<Vehicle> {
    public int compareTo(Vehicle other) {
        return this.name().compareTo(other.name());
    }

    public int compareToCreationSchema(Vehicle.CreationSchema other) {
        return this.name().compareTo(other.name());
    }

    public static record CreationSchema(
        String name,
        Coordinates coordinates,
        Integer enginePower,
        VehicleType vehicleType,
        FuelType fuelType
    )
        implements Serializable {
        public static Vehicle.CreationSchema fromScanner(
            Scanner scanner,
            boolean logScanned
        ) {
            String name = fields.name.promptUntilValid(
                "name",
                scanner,
                "unreachable",
                logScanned
            );
            Integer coordinateX = Coordinates.fields.x.promptUntilValid(
                "x coordinate",
                scanner,
                "integer is required",
                logScanned
            );
            Float coordinateY = Coordinates.fields.y.promptUntilValid(
                "y coordinate",
                scanner,
                "float required",
                logScanned
            );
            Integer enginePower = fields.enginePower.promptUntilValid(
                "engine power",
                scanner,
                "float required",
                logScanned
            );
            VehicleType vehicleType = fields.vehicleType.promptUntilValid(
                "vehicle type",
                scanner,
                "vehicle type must be one of the following: " +
                VehicleType.showIndexedList(", "),
                logScanned
            );
            FuelType fuelType = fields.fuelType.promptUntilValid(
                "fuel type",
                scanner,
                "fuel type must be one of the following: " +
                FuelType.showIndexedList(", "),
                logScanned
            );

            return new Vehicle.CreationSchema(
                name,
                new Coordinates(coordinateX, coordinateY),
                enginePower,
                vehicleType,
                fuelType
            );
        }

        public static final class fields {

            public static final FieldSchemaString name = FieldSchema
                .str()
                .nonnull()
                .nonempty();
            public static final FieldSchemaNum<Integer> enginePower = FieldSchema
                .integer()
                .nonnull()
                .greaterThan(0);
            public static final FieldSchemaEnum<FuelType> fuelType = FieldSchema.enumeration(
                FuelType.class
            );
            public static final FieldSchemaEnum<VehicleType> vehicleType = FieldSchema.enumeration(
                VehicleType.class
            );
        }
    }
}
