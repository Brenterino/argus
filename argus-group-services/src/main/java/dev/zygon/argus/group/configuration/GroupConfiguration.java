package dev.zygon.argus.group.configuration;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import org.jooq.SQLDialect;

@ConfigMapping(prefix = "argus.group")
public interface GroupConfiguration {

    @WithDefault("DEFAULT")
    SQLDialect jooqDialect();

    @WithDefault("1")
    int maxOwnedGroups();
}
