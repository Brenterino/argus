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
