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
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.Collections;
import java.util.List;

@Data
@Config(name = "argus")
public class ArgusClientConfig implements ConfigData {

    // Argus Connection Configuration
    @ConfigEntry.Gui.Tooltip
    private String argusHost = "http://localhost";

    @ConfigEntry.Gui.Tooltip
    private boolean verifyCertificateEnabled = false;

    @ConfigEntry.BoundedDiscrete(min = 30, max = 120)
    @ConfigEntry.Gui.Tooltip
    private int refreshTokenCheckIntervalSeconds = 30;

    @ConfigEntry.BoundedDiscrete(min = 60, max = 240)
    @ConfigEntry.Gui.Tooltip
    private int refreshTokenRenewBeforeExpirationSeconds = 60;

    @ConfigEntry.BoundedDiscrete(min = 5, max = 30)
    @ConfigEntry.Gui.Tooltip
    private int refreshInitialWaitForTokenSeconds = 5;

    @ConfigEntry.BoundedDiscrete(min = 60, max = 120)
    @ConfigEntry.Gui.Tooltip
    private int refreshMembershipIntervalSeconds = 60;

    @ConfigEntry.BoundedDiscrete(min = 60, max = 120)
    @ConfigEntry.Gui.Tooltip
    private int refreshElectionsIntervalSeconds = 60;

    @ConfigEntry.BoundedDiscrete(min = 60, max = 120)
    @ConfigEntry.Gui.Tooltip
    private int refreshLocationClientIntervalSeconds = 60;

    @ConfigEntry.BoundedDiscrete(min = 10, max = 20)
    @ConfigEntry.Gui.Tooltip
    private int transmitInitialWaitForConnectionSeconds = 10;

    @ConfigEntry.BoundedDiscrete(min = 1, max = 5)
    @ConfigEntry.Gui.Tooltip
    private int transmitLocationsIntervalSeconds = 1;

    // Client Visibility Related Configuration
    @ConfigEntry.Gui.Tooltip
    private boolean streamerModeEnabled = false;

    @ConfigEntry.Gui.Tooltip
    private boolean hideChatLocationsEnabled = false;

    @ConfigEntry.Gui.Tooltip
    private List<String> hiddenGroupAlerts = Collections.emptyList();

    @ConfigEntry.Gui.Tooltip
    private boolean coloredNamesEnabled = true;

    // Client Sharing Capabilities
    @ConfigEntry.Gui.Tooltip
    private boolean broadcastSnitchesEnabled = true;

    @ConfigEntry.Gui.Tooltip
    private boolean readLocalEntitiesEnabled = false;

    public boolean shouldHideGroupAlert(String group) {
        return streamerModeEnabled || hideChatLocationsEnabled || hiddenGroupAlerts.contains(group);
    }

    @Getter @Setter
    private static ArgusClientConfig activeConfig;
}
