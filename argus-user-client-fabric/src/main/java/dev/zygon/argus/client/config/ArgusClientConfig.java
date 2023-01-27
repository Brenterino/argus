package dev.zygon.argus.client.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

@Data
public class ArgusClientConfig {

    // Argus Connection Configuration
    private String argusHost = "https://localhost:8080";
    private int refreshTokenCheckIntervalSeconds = 30;

    private int refreshTokenRenewBeforeExpirationSeconds = 60;

    // Client Visibility Related Configuration
    private boolean streamerModeEnabled = false;
    private boolean hideSnitchesEnabled = false;
    private List<String> hiddenSnitchGroups = Collections.emptyList();
    private boolean coloredNamesEnabled = true;

    // Client Sharing Capabilities
    private boolean broadcastSnitchesEnabled = true;
    private boolean readLocalEntitiesEnabled = false;

    public boolean shouldHideSnitch(String group) {
        return streamerModeEnabled || hideSnitchesEnabled || hiddenSnitchGroups.contains(group);
    }

    @Getter
    @Setter
    private static ArgusClientConfig activeConfig = new ArgusClientConfig();
}
