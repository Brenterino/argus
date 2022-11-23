package dev.zygon.argus.location;

import lombok.NonNull;

import java.util.Collections;
import java.util.Set;

public record Locations(@NonNull Set<UserLocation> data) {

    public Locations(Set<UserLocation> data) {
        this.data = data != null ? data : Collections.emptySet();
    }
}
