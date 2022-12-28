package dev.zygon.argus.group.exception;

import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import static org.jboss.resteasy.reactive.RestResponse.Status.BAD_REQUEST;
import static org.jboss.resteasy.reactive.RestResponse.Status.INTERNAL_SERVER_ERROR;

@Slf4j
public class GroupExceptionMapper {

    @ServerExceptionMapper
    public RestResponse<String> mapException(GroupException e) {
        if (e instanceof FatalGroupException) {
            log.error("Caught fatal error. Returning status ({}) to client.",
                    e.getStatus(), e);
        }
        return RestResponse
                .status(e.getStatus(), e.getMessage());
    }

    @ServerExceptionMapper
    public RestResponse<String> mapException(IllegalArgumentException e) {
        return RestResponse
                .status(BAD_REQUEST, e.getMessage());
    }

    @ServerExceptionMapper
    public RestResponse<Void> mapException(NullPointerException e) {
        log.warn("NPE caught, sending BAD_REQUEST(400) to client.", e);
        return RestResponse
                .status(BAD_REQUEST);
    }
}
