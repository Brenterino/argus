package dev.zygon.argus.group;

import lombok.NonNull;

import java.util.Collections;
import java.util.Set;

public record Groups(@NonNull Set<Group> groups) {

    public Groups(Set<Group> groups) {
        this.groups = groups != null ? Collections.unmodifiableSet(groups) :
                Collections.emptySet();
    }
}
