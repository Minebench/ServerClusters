package de.themoep.serverclusters.bungee;

import java.util.concurrent.ExecutionException;

public class ServerNotFoundException extends ExecutionException {
    public ServerNotFoundException(String message) {
        super(message);
    }
}
