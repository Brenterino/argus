package dev.zygon.argus.location.codec;

import dev.zygon.argus.location.Locations;
import io.vertx.core.json.JsonObject;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

/**
 * Implementation of {@link Decoder.Text} which allows for conversion of
 * JSON into a {@link Locations} record using {@link JsonObject}.
 *
 * @see Decoder.Text for more information on methods.
 */
@Slf4j
public class LocationsDecoder implements Decoder.Text<Locations> {

    @Override
    public Locations decode(@NonNull String text) throws DecodeException {
        try {
            return new JsonObject(text)
                    .mapTo(Locations.class);
        } catch (Exception e) {
            throw new DecodeException(text, "Locations could not be decoded from JSON", e);
        }
    }

    @Override
    public boolean willDecode(String text) {
        return text != null && !text.isBlank();
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
