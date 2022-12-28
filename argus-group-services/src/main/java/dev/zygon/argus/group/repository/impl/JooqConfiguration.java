package dev.zygon.argus.group.repository.impl;

import dev.zygon.argus.group.configuration.GroupConfiguration;
import org.jooq.Configuration;
import org.jooq.impl.DefaultConfiguration;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Produces;

public class JooqConfiguration {

    @Produces
    @ApplicationScoped
    public Configuration jooqConfiguration(GroupConfiguration config) {
        var configuration = new DefaultConfiguration();
        configuration.set(config.jooqDialect());
        return configuration;
    }
}
