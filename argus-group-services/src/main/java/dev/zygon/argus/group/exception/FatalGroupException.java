package dev.zygon.argus.group.exception;

import javax.ws.rs.core.Response.StatusType;

public class FatalGroupException extends GroupException {

    public FatalGroupException(String message, Throwable cause) {
        super(message, cause);
    }

    public FatalGroupException(StatusType status, String message) {
        super(status, message);
    }

    public FatalGroupException(StatusType status, String message, Throwable cause) {
        super(status, message, cause);
    }
}
