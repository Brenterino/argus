package dev.zygon.argus.client.util;

import dev.zygon.argus.location.Dimension;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DimensionMapper {

    public static Dimension fromSnitch(String dimension) {
        return switch (dimension) {
            case "world_nether" -> Dimension.NETHER;
            case "world_the_end" -> Dimension.END;
            default -> Dimension.OVERWORLD;
        };
    }

    public static Dimension fromProximity(String dimension) {
        return switch (dimension) {
            case "the_nether" -> Dimension.NETHER;
            case "the_end" -> Dimension.END;
            default -> Dimension.OVERWORLD;
        };
    }
}
