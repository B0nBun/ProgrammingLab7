package itmo.app.shared.fieldschema;

import itmo.app.shared.Utils.Validator;
import java.util.ArrayList;
import java.util.List;

public class FieldSchemaString
    implements FieldSchemaComparable<String, FieldSchemaString> {

    private List<Validator<String>> validators;

    private FieldSchemaString(List<Validator<String>> initValidators) {
        this.validators = initValidators;
    }

    FieldSchemaString() {
        this(new ArrayList<>());
    }

    public FieldSchemaString nonempty() {
        return this.refine(Validator.from(s -> s.length() != 0, "string can't be empty"));
    }

    public String parse(String input) {
        return input;
    }

    public List<Validator<String>> validators() {
        return this.validators;
    }

    public FieldSchemaString refine(Validator<String> validator) {
        var newValidators = new ArrayList<>(this.validators);
        newValidators.add(validator);
        return new FieldSchemaString(newValidators);
    }
}
