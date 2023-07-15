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
package dev.zygon.argus.location.client;

import dev.zygon.argus.location.Locations;
import dev.zygon.argus.location.codec.LocationsDecoder;
import dev.zygon.argus.location.codec.LocationsEncoder;
import lombok.extern.slf4j.Slf4j;

import jakarta.websocket.*;
import java.util.concurrent.LinkedBlockingDeque;

@Slf4j
public class LocationsClients {

    public static final LinkedBlockingDeque<Locations> ALICE_RECEIVED =
            new LinkedBlockingDeque<>();
    public static final LinkedBlockingDeque<Locations> BOB_RECEIVED =
            new LinkedBlockingDeque<>();

    @ClientEndpoint(encoders = LocationsEncoder.class,
            decoders = LocationsDecoder.class,
            configurator = AliceJwtConfigurator.class)
    public static class AliceClient {

        @OnOpen
        public void onOpen(Session session) {
            log.info("Opened Alice client.");
        }

        @OnMessage
        public void onMessage(Session session, Locations locations) {
            ALICE_RECEIVED.add(locations);
        }
    }

    @ClientEndpoint(encoders = LocationsEncoder.class,
            decoders = LocationsDecoder.class,
            configurator = BobJwtConfigurator.class)
    public static class BobClient {

        @OnOpen
        public void onOpen(Session session) {
            log.info("Opened Bob client.");
        }

        @OnMessage
        public void onMessage(Session session, Locations locations) {
            BOB_RECEIVED.add(locations);
        }
    }

    @ClientEndpoint(encoders = LocationsEncoder.class,
            decoders = LocationsDecoder.class,
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

    private LocationsClients() {
    }
}
