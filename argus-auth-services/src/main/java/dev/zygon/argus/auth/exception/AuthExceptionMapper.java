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
package dev.zygon.argus.auth.exception;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import javax.ws.rs.WebApplicationException;

import static org.jboss.resteasy.reactive.RestResponse.Status.INTERNAL_SERVER_ERROR;

public class AuthExceptionMapper {

    @ServerExceptionMapper
    public RestResponse<String> mapException(WebApplicationException e) {
        return RestResponse
                .status(INTERNAL_SERVER_ERROR, "Downstream call failed unexpectedly. Try again later.");
    }
}
