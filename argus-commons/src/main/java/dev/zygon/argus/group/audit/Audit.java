package dev.zygon.argus.group.audit;

import dev.zygon.argus.permission.Permission;
import lombok.Builder;
import lombok.NonNull;

import java.time.OffsetDateTime;
import java.util.UUID;

@Builder
public record Audit(@NonNull UUID changer, @NonNull UUID target,
                    @NonNull AuditAction action, @NonNull Permission permission,
                    @NonNull OffsetDateTime occurred) {
}
