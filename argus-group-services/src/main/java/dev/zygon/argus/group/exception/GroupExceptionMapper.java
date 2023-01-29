/*
    Argus - Suite of services aimed to enhance Minecraft Multiplayer
    Copyright (C) 2023 Zygon

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
