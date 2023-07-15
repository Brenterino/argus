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
package dev.zygon.argus.status.client;

import dev.zygon.argus.status.UserStatus;
import dev.zygon.argus.status.codec.UserStatusDecoder;
import dev.zygon.argus.status.codec.UserStatusEncoder;
import jakarta.websocket.*;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.LinkedBlockingDeque;

@Slf4j
public class UserStatusClients {

    public static final LinkedBlockingDeque<UserStatus> ALICE_RECEIVED =
            new LinkedBlockingDeque<>();
    public static final LinkedBlockingDeque<UserStatus> BOB_RECEIVED =
            new LinkedBlockingDeque<>();

    @ClientEndpoint(encoders = UserStatusEncoder.class,
            decoders = UserStatusDecoder.class,
            configurator = AliceJwtConfigurator.class)
    public static class AliceClient {

        @OnOpen
        public void onOpen(Session session) {
            log.info("Opened Alice client.");
        }

        @OnMessage
        public void onMessage(Session session, UserStatus status) {
            ALICE_RECEIVED.add(status);
        }
    }

    @ClientEndpoint(encoders = UserStatusEncoder.class,
            decoders = UserStatusDecoder.class,
            configurator = BobJwtConfigurator.class)
    public static class BobClient {

        @OnOpen
        public void onOpen(Session session) {
            log.info("Opened Bob client.");
        }

        @OnMessage
        public void onMessage(Session session, UserStatus status) {
            BOB_RECEIVED.add(status);
        }
    }

    @ClientEndpoint(encoders = UserStatusEncoder.class,
            decoders = UserStatusDecoder.class,
            configurator = ExpiredJwtConfigurator.class)
    public static class ExpiredTokenClient {

        @OnOpen
        public void onOpen(Session session) {
            log.info("Opened Expired Token client.");
        }

        @OnClose
        public void onClose(Session session) {
            log.info("Closed Expired Token client.");
        }
    }

    private UserStatusClients() {
    }
}
