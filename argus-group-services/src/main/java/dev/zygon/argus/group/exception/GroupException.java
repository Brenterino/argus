package dev.zygon.argus.group.exception;

import lombok.Getter;

import javax.ws.rs.core.Response.StatusType;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

public class GroupException extends RuntimeException {

    @Getter
    private final StatusType status;

    public GroupException(String message) {
        this(INTERNAL_SERVER_ERROR, message);
    }

    public GroupException(String message, Throwable cause) {
        this(INTERNAL_SERVER_ERROR, message, cause);
    }

    public GroupException(StatusType status, String message) {
        super(message);
        this.status = status;
    }

    public GroupException(StatusType status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }
}
