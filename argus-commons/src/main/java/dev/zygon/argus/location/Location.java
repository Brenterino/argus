package dev.zygon.argus.location;

import lombok.Builder;
import lombok.NonNull;

import java.time.Instant;

/**
 * Record which contains location data. This location could be related to an
 * entity, waypoint (permanent/temporary), or for other purposes.
 *
 * @param x     the x-coordinate position.
 * @param y     the y-coordinate position.
 * @param z     the z-coordinate position.
 * @param w     which dimension the position is in.
 * @param local if the position was determined from a local observer.
 *              <b>May only be relevant for some purposes.</b>
 * @param time  the time the location was captured.
 *              May only be relevant for some purposes.
 */
@Builder
public record Location(double x, double y, double z, int w,
                       boolean local, @NonNull Instant time) {
}
