package itmo.app.shared.fieldschema;

import itmo.app.shared.Utils.Validator;

// FieldSchemaComparable.Max=value can not be greater than {0}
// FieldSchemaComparable.GreaterThan=value must be greater than {0}
// FieldSchemaComparable.Min=value can not be lesser than {0}
// FieldSchemaComparable.Clamped=value must be between {0} and {1}

public interface FieldSchemaComparable<
    T extends Comparable<T>, Self extends FieldSchemaComparable<T, Self>
>
    extends FieldSchema<T, Self> {
    public default Self max(T maxValue) {
        return this.refine(
                Validator.from(
                    value -> value.compareTo(maxValue) <= 0,
                    "value must be lower or equal to " + maxValue
                )
            );
    }

    public default Self greaterThan(T minValue) {
        return this.refine(
                Validator.from(
                    value -> value.compareTo(minValue) > 0,
                    "value must be greater than " + minValue
                    // Messages.get("FieldSchemaComparable.GreaterThan", minValue)
                )
            );
    }

    public default Self min(T minValue) {
        return this.refine(
                Validator.from(
                    value -> value.compareTo(minValue) >= 0,
                    "value must be greater or equal to " + minValue
                )
            );
    }

    public default Self clamped(T minValue, T maxValue) {
        return this.refine(
                Validator.from(
                    value ->
                        value.compareTo(minValue) >= 0 && value.compareTo(maxValue) <= 0,
                    "value must be between " + minValue + " and " + maxValue
                )
            );
    }
}
