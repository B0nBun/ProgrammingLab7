package itmo.app.shared;

import itmo.app.shared.exceptions.ValidationException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.function.Function;
import java.util.function.Predicate;

public class Utils {

    public static ByteBuffer objectToBuffer(Object response) throws IOException {
        try (
            var byteOut = new ByteArrayOutputStream();
            var objectStream = new ObjectOutputStream(byteOut);
        ) {
            objectStream.writeObject(response);
            byte[] objectBytes = byteOut.toByteArray();

            int objectSize = objectBytes.length;
            var buffer = ByteBuffer.allocate(Integer.BYTES + objectSize);
            buffer.putInt(objectSize);
            buffer.put(objectBytes);
            return buffer;
        }
    }

    @FunctionalInterface
    public static interface Validator<T> {
        /**
         * Function, which validates that the value of type {@code T} is correct
         *
         * @param value Value to validate
         * @return The same value if it is valid, otherwise throws
         * @throws ValidationException Thrown if the validation failed
         */
        T validate(T value) throws ValidationException;

        public static <T> Validator<T> from(
            Predicate<T> predicate,
            Function<T, String> messageGetter
        ) {
            return value -> {
                boolean valid = predicate.test(value);
                if (valid) return value;
                throw new ValidationException(messageGetter.apply(value));
            };
        }

        public static <T> Validator<T> from(Predicate<T> predicate, String failMessage) {
            return Validator.from(predicate, __ -> failMessage);
        }
    }

    @FunctionalInterface
    public static interface NumberParser<N> {
        /**
         * Method, which parses some kind of the number value from provided String. (It actually can be
         * any type, but {@link NumberFormatException} is what signifies the failed parsing)
         *
         * @param string A string from which the value should be parsed
         * @return A parsed value
         * @throws NumberFormatException This exception is thrown if the parsing is failed
         */
        N parse(String string) throws NumberFormatException;
    }
}
