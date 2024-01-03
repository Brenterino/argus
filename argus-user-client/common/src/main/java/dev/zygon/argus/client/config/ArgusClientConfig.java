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

    @ConfigEntry.Gui.Tooltip
    private String argusHost = "https://argus.zygon.dev";

    @ConfigEntry.Gui.Tooltip
    private boolean verifyCertificateEnabled = true;

    @ConfigEntry.Gui.Tooltip
    private boolean webUiEnabled = true;

    @ConfigEntry.Gui.Tooltip
    private int webUiPort = 9000;

    @ConfigEntry.Category("visibility")
    @ConfigEntry.ColorPicker
    @ConfigEntry.Gui.Tooltip
    private int pingColor = 0xFFFFFF;

    @ConfigEntry.Category("visibility")
    @ConfigEntry.BoundedDiscrete(min = 1000, max = 100000)
    @ConfigEntry.Gui.Tooltip
    private int maxViewDistance = 10000;

    @ConfigEntry.Category("visibility")
    @ConfigEntry.BoundedDiscrete(min = 1, max = 180)
    @ConfigEntry.Gui.Tooltip
    private int yawSliceDegrees = 30;

    @ConfigEntry.Category("visibility")
    @ConfigEntry.Gui.Tooltip
    private boolean showAlignmentsDigest = true;

    @ConfigEntry.Category("visibility")
    @ConfigEntry.Gui.Tooltip
    private boolean showDimensionIndicator = true;

    @ConfigEntry.Category("visibility")
    @ConfigEntry.Gui.Tooltip
    private boolean sameDimensionOnly = false;

    @ConfigEntry.Category("visibility")
    @ConfigEntry.Gui.Tooltip
    private boolean streamerModeEnabled = false;

    @ConfigEntry.Category("visibility")
    @ConfigEntry.Gui.Tooltip
    private boolean hideChatLocationsEnabled = false;

    @ConfigEntry.Category("visibility")
    @ConfigEntry.Gui.Tooltip
    private boolean coloredNamesEnabled = true;

    @ConfigEntry.Category("visibility")
    @ConfigEntry.Gui.Tooltip
    private boolean overwriteDefaultNamesEnabled = false;

    @ConfigEntry.Category("visibility")
    @ConfigEntry.Gui.Tooltip
    private boolean showStatusEnabled = true;

    @ConfigEntry.Category("visibility")
    @ConfigEntry.Gui.Tooltip
    private boolean showHealthOnly = false;

    @ConfigEntry.Category("visibility")
    @ConfigEntry.Gui.Tooltip
    private List<String> hiddenGroupAlerts = Collections.emptyList();

    @ConfigEntry.Category("visibility")
    @ConfigEntry.BoundedDiscrete(min = 5, max = 120)
    @ConfigEntry.Gui.Tooltip
    private long locationTimerStartSeconds = 5;

    @ConfigEntry.Category("timings")
    @ConfigEntry.BoundedDiscrete(min = 30, max = 120)
    @ConfigEntry.Gui.Tooltip
    private int cleanLocationsIntervalSeconds = 30;

    @ConfigEntry.Category("timings")
    @ConfigEntry.BoundedDiscrete(min = 1, max = 120)
    @ConfigEntry.Gui.Tooltip
    private int pingExpirationSeconds = 10;

    @ConfigEntry.Category("timings")
    @ConfigEntry.BoundedDiscrete(min = 5, max = 15)
    @ConfigEntry.Gui.Tooltip
    private int locationsExpirationMinutes = 15;

    @ConfigEntry.Category("timings")
    @ConfigEntry.BoundedDiscrete(min = 30, max = 120)
    @ConfigEntry.Gui.Tooltip
    private int refreshTokenCheckIntervalSeconds = 30;

    @ConfigEntry.Category("timings")
    @ConfigEntry.BoundedDiscrete(min = 60, max = 240)
    @ConfigEntry.Gui.Tooltip
    private int refreshTokenRenewBeforeExpirationSeconds = 60;

    @ConfigEntry.Category("timings")
    @ConfigEntry.BoundedDiscrete(min = 30, max = 120)
    @ConfigEntry.Gui.Tooltip
    private int refreshMembershipIntervalSeconds = 60;

    @ConfigEntry.Category("timings")
    @ConfigEntry.BoundedDiscrete(min = 30, max = 120)
    @ConfigEntry.Gui.Tooltip
    private int refreshElectionsIntervalSeconds = 60;

    @ConfigEntry.Category("timings")
    @ConfigEntry.BoundedDiscrete(min = 5, max = 60)
    @ConfigEntry.Gui.Tooltip
    private int refreshSocketClientIntervalSeconds = 60;

    @ConfigEntry.Category("timings")
    @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
    @ConfigEntry.Gui.Tooltip
    private int transmitInitialWaitForConnectionSeconds = 1;

    @ConfigEntry.Category("timings")
    @ConfigEntry.BoundedDiscrete(min = 33, max = 1000)
    @ConfigEntry.Gui.Tooltip
    private int transmitLocationsIntervalMillis = 100;

    @ConfigEntry.Category("timings")
    @ConfigEntry.BoundedDiscrete(min = 33, max = 1000)
    @ConfigEntry.Gui.Tooltip
    private int statusCheckerIntervalMillis = 250;

    @ConfigEntry.Category("timings")
    @ConfigEntry.BoundedDiscrete(min = 33, max = 1000)
    @ConfigEntry.Gui.Tooltip
    private int transmitStatusIntervalMillis = 500;

    @ConfigEntry.Category("timings")
    @ConfigEntry.BoundedDiscrete(min = 1, max = 120)
    @ConfigEntry.Gui.Tooltip
    private int transmitPlayerLocationIntervalSeconds = 3;

    @ConfigEntry.Category("sourcing")
    @ConfigEntry.Gui.Tooltip
    private boolean readChatEnabled = true;

    @ConfigEntry.Category("sourcing")
    @ConfigEntry.Gui.Tooltip
    private boolean readStatusEnabled = true;

    @ConfigEntry.Category("sourcing")
    @ConfigEntry.Gui.Tooltip
    private boolean readLocalEnvironmentEnabled = false;

    public boolean shouldHideGroupAlert(String group) {
        return streamerModeEnabled || hideChatLocationsEnabled || hiddenGroupAlerts.contains(group);
    }

    public boolean shouldShowNameOverwrite() {
        return !streamerModeEnabled && coloredNamesEnabled;
    }

    public boolean shouldShowStatusInformation() {
        return !streamerModeEnabled && showStatusEnabled;
    }

    @Getter @Setter
    private static ArgusClientConfig activeConfig;
}
