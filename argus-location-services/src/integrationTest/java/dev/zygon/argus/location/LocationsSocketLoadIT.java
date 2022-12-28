package dev.zygon.argus.location;

import dev.zygon.argus.user.User;
import io.quarkus.test.common.http.TestHTTPResource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.websocket.ContainerProvider;
import javax.websocket.Session;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static dev.zygon.argus.location.client.LocationsClients.*;

@Slf4j
@Disabled("Only meant to be used as a base test and should not run directly.")
public class LocationsSocketLoadIT {

    @TestHTTPResource("/locations")
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
            log.info("Alice Received {}", ALICE_RECEIVED.size());
            log.info("Bob Received {}", BOB_RECEIVED.size());
            ALICE_RECEIVED.clear();
            BOB_RECEIVED.clear();
        }, 0, SESSION_SCHEDULING_MAX_DELAY_MILLIS, TimeUnit.MILLISECONDS);
    }

    @AfterEach
    public void tearDown() throws Exception {
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
        aliceSessions.forEach(SessionRunnable::close);
        bobSessions.forEach(SessionRunnable::close);
        ALICE_RECEIVED.clear();
        BOB_RECEIVED.clear();
    }

    private static final int MAX_BUFFER_SIZE_BYTES = 262144;

    @SneakyThrows
    private SessionRunnable createAliceSocket(int id) {
        var session = ContainerProvider.getWebSocketContainer()
                .connectToServer(AliceClient.class, uri);
        session.setMaxTextMessageBufferSize(MAX_BUFFER_SIZE_BYTES);
        session.setMaxBinaryMessageBufferSize(MAX_BUFFER_SIZE_BYTES);
        return new SessionRunnable(session);
    }

    @SneakyThrows
    private SessionRunnable createBobSocket(int id) {
        var session = ContainerProvider.getWebSocketContainer()
                .connectToServer(BobClient.class, uri);
        return new SessionRunnable(session);
    }

    private static class SessionRunnable implements Runnable {

        // Mutable(s)
        private Location location;

        // Immutable(s)
        private final User user;
        private final Session session;

        public SessionRunnable(Session session) {
            this.user = new User(UUID.randomUUID(), "User");
            this.session = session;
            this.location = Location.builder()
                    .x(0.0)
                    .y(0.0)
                    .z(0.0)
                    .w(0)
                    .local(true)
                    .time(Instant.now())
                    .build();
        }

        @SneakyThrows
        @Override
        public void run() {
            location = adjustLocationRandomly(location);
            var userLocation = new UserLocation(user, location);
            var locations = new Locations(Set.of(userLocation));

            session.getBasicRemote()
                    .sendObject(locations);
        }

        private static final double RANDOM_LOWER_BOUND = -2;
        private static final double RANDOM_UPPER_BOUND = 2;

        private Location adjustLocationRandomly(Location location) {
            var random = ThreadLocalRandom.current();
            return Location.builder()
                    .x(location.x() + random.nextDouble(RANDOM_LOWER_BOUND, RANDOM_UPPER_BOUND))
                    .y(location.y() + random.nextDouble(RANDOM_LOWER_BOUND, RANDOM_UPPER_BOUND))
                    .z(location.z() + random.nextDouble(RANDOM_LOWER_BOUND, RANDOM_UPPER_BOUND))
                    .w(location.w())
                    .local(location.local())
                    .time(Instant.now())
                    .build();
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
