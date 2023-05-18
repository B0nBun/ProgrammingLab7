package itmo.app.shared;

import java.io.Serializable;

public record ClientRequest(
    String login,
    String password,
    String commandName,
    Serializable commandParameters,
    Serializable additionalObject
)
    implements Serializable {}
