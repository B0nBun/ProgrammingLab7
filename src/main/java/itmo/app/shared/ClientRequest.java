package itmo.app.shared;

import java.io.Serializable;

public record ClientRequest(
    String login,
    String passwordHash,
    String commandName,
    Serializable commandParameters,
    Serializable additionalObject
)
    implements Serializable {}
