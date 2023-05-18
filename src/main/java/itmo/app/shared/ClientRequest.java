package itmo.app.shared;

import java.io.Serializable;

public record ClientRequest<P extends Serializable, A extends Serializable>(
    String login,
    String password,
    String commandName,
    P commandParameters,
    A additionalObject
)
    implements Serializable {}
