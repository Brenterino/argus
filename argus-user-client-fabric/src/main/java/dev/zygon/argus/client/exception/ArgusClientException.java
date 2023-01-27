package dev.zygon.argus.client.exception;

import com.mojang.authlib.exceptions.AuthenticationException;

public class ArgusClientException extends Exception {

    public ArgusClientException(String message, AuthenticationException cause) {
        super(message, cause);
    }
}
