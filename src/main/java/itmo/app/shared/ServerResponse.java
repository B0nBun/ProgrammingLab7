package itmo.app.shared;

import java.io.Serializable;

public record ServerResponse(String output) implements Serializable {}
