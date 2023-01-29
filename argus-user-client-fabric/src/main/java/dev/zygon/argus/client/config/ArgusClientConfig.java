/*
    Argus - Suite of services aimed to enhance Minecraft Multiplayer
    Copyright (C) 2023 Zygon

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package dev.zygon.argus.client.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

@Data
public class ArgusClientConfig {

    // Argus Connection Configuration
    private String argusHost = "https://localhost";
    private boolean verifyCertificateEnabled = false;
    private int refreshTokenCheckIntervalSeconds = 30;
    private int refreshTokenRenewBeforeExpirationSeconds = 60;

    // Client Visibility Related Configuration
    private boolean streamerModeEnabled = false;
    private boolean hideChatLocationsEnabled = false;
    private List<String> hiddenGroupAlerts = Collections.emptyList();
    private boolean coloredNamesEnabled = true;

    // Client Sharing Capabilities
    private boolean broadcastSnitchesEnabled = true;
    private boolean readLocalEntitiesEnabled = false;

    public boolean shouldHideGroupAlert(String group) {
        return streamerModeEnabled || hideChatLocationsEnabled || hiddenGroupAlerts.contains(group);
    }

    @Getter @Setter
    private static ArgusClientConfig activeConfig = new ArgusClientConfig();
}
