package dev.zygon.argus.auth.service;

import dev.zygon.argus.auth.ArgusToken;
import dev.zygon.argus.permission.Permissions;

import java.util.UUID;

public interface ArgusTokenGenerator {

    ArgusToken generate(UUID uuid, String namespace, Permissions permissions);
}
