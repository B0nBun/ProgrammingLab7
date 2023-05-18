package itmo.app.shared.entities;

import itmo.app.shared.fieldschema.FieldSchema;
import itmo.app.shared.fieldschema.FieldSchemaNum;
import java.io.Serializable;

public record Coordinates(Integer x, Float y) implements Serializable {
    public static final class fields {

        public static final FieldSchemaNum<Integer> x = FieldSchema.integer().nonnull();
        public static final FieldSchemaNum<Float> y = FieldSchema
            .floating()
            .nonnull()
            .min(-738f);
    }
}
