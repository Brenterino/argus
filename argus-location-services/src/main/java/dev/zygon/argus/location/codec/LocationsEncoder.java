package dev.zygon.argus.location.codec;

import dev.zygon.argus.location.Locations;
import io.vertx.core.json.JsonObject;
import lombok.NonNull;

import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;
import jakarta.websocket.EndpointConfig;

/**
 * Implementation of {@link Encoder.Text} which allows for conversion of
 * a {@link Locations} record into JSON using {@link JsonObject}.
 *
 * @see Encoder.Text
 */
public class LocationsEncoder implements Encoder.Text<Locations> {

    @Override
    public String encode(@NonNull Locations locations) throws EncodeException {
        try {
            return JsonObject.mapFrom(locations)
                    .encode();
        } catch (Exception e) {
            throw new EncodeException(locations, "Data could not be encoded to JSON", e);
        }
    }

    @Override
    public void init(EndpointConfig config) {
        // No initialization required
    }

    @Override
    public void destroy() {
        // No destroy override required
    }
}
