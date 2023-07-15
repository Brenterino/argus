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
package dev.zygon.argus.status;

import dev.zygon.argus.status.client.UserStatusClients;
import io.quarkus.test.common.http.TestHTTPResource;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.Session;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@Slf4j
@Disabled("Only meant to be used as a base test and should not run directly.")
public class UserStatusSocketLoadIT {

    @TestHTTPResource("/statuses")
    private URI uri;

    private List<SessionRunnable> aliceSessions;
    private List<SessionRunnable> bobSessions;
    private ScheduledThreadPoolExecutor executor;
    private static final int SESSIONS_PER_GROUP = 500;
    private static final int BOB_OFFSET = 501;

    private static final int SESSION_THREAD_POOLS = 4;

    private static final int SESSION_SCHEDULE_MIN_DELAY_MILLIS = 500;
    private static final int SESSION_SCHEDULING_MAX_DELAY_MILLIS = 1000;
    private static final int TEST_PLANNED_DURATION_MINUTES = 3;

    @BeforeEach
    public void setUp() {
        aliceSessions = IntStream.range(0, SESSIONS_PER_GROUP)
                .mapToObj(this::createAliceSocket)
                .toList();
        bobSessions = IntStream.range(BOB_OFFSET, SESSIONS_PER_GROUP + BOB_OFFSET)
                .mapToObj(this::createBobSocket)
                .toList();
        executor = new ScheduledThreadPoolExecutor(SESSION_THREAD_POOLS);
    }

    @Test
    public void testLoad() throws Exception {
        aliceSessions.forEach(this::schedule);
        bobSessions.forEach(this::schedule);
        scheduleQueueClean();

        var testDuration = Duration.of(TEST_PLANNED_DURATION_MINUTES, ChronoUnit.MINUTES);

        Thread.sleep(testDuration.toMillis());
    }

    private void schedule(SessionRunnable runnable) {
        var random = ThreadLocalRandom.current();
        executor.scheduleAtFixedRate(runnable,
                random.nextLong(SESSION_SCHEDULING_MAX_DELAY_MILLIS),
                random.nextLong(SESSION_SCHEDULE_MIN_DELAY_MILLIS,
                        SESSION_SCHEDULING_MAX_DELAY_MILLIS + 1L),
                TimeUnit.MILLISECONDS);
    }

    private void scheduleQueueClean() {
        executor.scheduleAtFixedRate(() -> {
            log.info("Alice Received {}", UserStatusClients.ALICE_RECEIVED.size());
            log.info("Bob Received {}", UserStatusClients.BOB_RECEIVED.size());
            UserStatusClients.ALICE_RECEIVED.clear();
            UserStatusClients.BOB_RECEIVED.clear();
        }, 0, SESSION_SCHEDULING_MAX_DELAY_MILLIS, TimeUnit.MILLISECONDS);
    }

    @AfterEach
    public void tearDown() throws Exception {
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
        aliceSessions.forEach(SessionRunnable::close);
        bobSessions.forEach(SessionRunnable::close);
        UserStatusClients.ALICE_RECEIVED.clear();
        UserStatusClients.BOB_RECEIVED.clear();
    }

    private static final int MAX_BUFFER_SIZE_BYTES = 262144;

    @SneakyThrows
    private SessionRunnable createAliceSocket(int id) {
        var session = ContainerProvider.getWebSocketContainer()
                .connectToServer(UserStatusClients.AliceClient.class, uri);
        session.setMaxTextMessageBufferSize(MAX_BUFFER_SIZE_BYTES);
        session.setMaxBinaryMessageBufferSize(MAX_BUFFER_SIZE_BYTES);
        return new SessionRunnable(session);
    }

    @SneakyThrows
    private SessionRunnable createBobSocket(int id) {
        var session = ContainerProvider.getWebSocketContainer()
                .connectToServer(UserStatusClients.BobClient.class, uri);
        return new SessionRunnable(session);
    }

    private static class SessionRunnable implements Runnable {

        // Mutable(s)
        private UserStatus status;

        // Immutable(s)
        private final Session session;

        public SessionRunnable(Session session) {
            this.session = session;
            var item = new ItemStatus(0, "L", 10);
            var effects = new EffectStatus(50, "G",
                    Instant.now().plus(100, ChronoUnit.SECONDS));
            this.status = new UserStatus(UUID.randomUUID(), 5.0f,
                    List.of(item), List.of(effects));
        }

        @SneakyThrows
        @Override
        public void run() {
            status = adjustStatusRandomly(status);

            session.getBasicRemote()
                    .sendObject(status);
        }

        private static final int RANDOM_LOWER_BOUND = -2;
        private static final int RANDOM_UPPER_BOUND = 2;

        private UserStatus adjustStatusRandomly(UserStatus status) {
            var items = adjustItemsRandomly(status.items());
            var effects = adjustEffectsRandomly(status.effects());
            return new UserStatus(status.source(), 1.0f, items, effects);
        }

        private List<ItemStatus> adjustItemsRandomly(List<ItemStatus> items) {
            return items.stream()
                    .map(this::adjustItemRandomly)
                    .toList();
        }

        private ItemStatus adjustItemRandomly(ItemStatus item) {
            var random = ThreadLocalRandom.current();
            return new ItemStatus(
                    item.color(),
                    item.symbol(),
                    item.count() + random.nextInt(RANDOM_LOWER_BOUND, RANDOM_UPPER_BOUND)
            );
        }

        private List<EffectStatus> adjustEffectsRandomly(List<EffectStatus> effects) {
            return effects.stream()
                    .map(this::adjustEffectRandomly)
                    .toList();
        }

        private EffectStatus adjustEffectRandomly(EffectStatus effect) {
            var random = ThreadLocalRandom.current();
            return new EffectStatus(
                    effect.color(),
                    effect.symbol(),
                    effect.expiration().plus(random.nextInt(RANDOM_LOWER_BOUND, RANDOM_UPPER_BOUND),
                            ChronoUnit.SECONDS)
            );
        }

        public void close() {
            try {
                session.close();
            } catch (Exception e) {
                log.error("Could not close session properly!!", e);
            }
        }
    }
}
