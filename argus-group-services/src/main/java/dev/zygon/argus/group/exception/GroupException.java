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

import lombok.Getter;

import javax.ws.rs.core.Response.StatusType;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

public class GroupException extends RuntimeException {

    @Getter
    private final transient StatusType status;

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
