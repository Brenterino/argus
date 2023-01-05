package dev.zygon.argus.group.audit;

import lombok.NonNull;

import java.util.List;

public record AuditLog(@NonNull List<Audit> log, int pages) {
}
