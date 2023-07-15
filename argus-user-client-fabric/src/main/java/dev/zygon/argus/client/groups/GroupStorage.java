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
package dev.zygon.argus.client.groups;

import dev.zygon.argus.client.api.ArgusGroupApi;
import dev.zygon.argus.client.api.ArgusPermissionApi;
import dev.zygon.argus.client.connector.customize.ArgusMojangTokenGenerator;
import dev.zygon.argus.client.event.RemoteLocationHandler;
import dev.zygon.argus.client.event.RemoteStatusHandler;
import dev.zygon.argus.group.Group;
import dev.zygon.argus.permission.GroupPermission;
import dev.zygon.argus.permission.GroupPermissions;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import retrofit2.Call;
import retrofit2.Response;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public enum GroupStorage {

    INSTANCE;

    @Setter private ArgusGroupApi groups;
    @Setter private ArgusPermissionApi permissions;
    @Setter @Getter private Set<GroupPermission> membership;
    @Setter @Getter private Set<GroupPermission> elections;
    @Setter @Getter private Map<Group, GroupMetadata> metadata;
    @Setter @Getter private Map<UUID, GroupAlignmentDisplay> displays;

    public void refreshMemberships() {
        doCall(groups.groups(), this::updateMembership);
    }

    private void updateMembership(Set<GroupPermission> membership) {
        var metadataGroups = readGroups();
        var computedMetadata = membership.stream()
                .map(GroupPermission::group)
                .filter(metadataGroups::contains)
                .collect(Collectors.toMap(Function.identity(), GroupMetadataExtractor::fromGroup));
        var computedDisplays = computedMetadata.values()
                .stream()
                .map(GroupMetadata::displays)
                .flatMap(display -> display.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (first, second) -> first));
        setMembership(membership);
        setMetadata(computedMetadata);
        setDisplays(computedDisplays);
    }

    public List<Group> readGroups() {
        return Optional.ofNullable(elections)
                .stream()
                .flatMap(Collection::stream)
                .filter(gp -> gp.permission().canRead())
                .map(GroupPermission::group)
                .toList();
    }

    public void refreshElections() {
        doCall(permissions.elected(), this::updateElections);
    }

    private void updateElections(Set<GroupPermission> nextElections) {
        if (elections != null && !elections.equals(nextElections)) {
            log.info("[ARGUS] Elections changed, forcing token refresh and reopening location socket on refresh.");
            var tokens = ArgusMojangTokenGenerator.INSTANCE;
            tokens.onNextRefresh(() -> {
                RemoteLocationHandler.INSTANCE.restartClient();
                RemoteStatusHandler.INSTANCE.restartClient();
            });
            tokens.forceRefresh();
        }
        elections = nextElections;
        updateMembership(membership); // force re-calculation of metadata
    }

    private void doCall(Call<GroupPermissions> apiCall, Consumer<Set<GroupPermission>> setter) {
        try {
            Optional.of(apiCall.execute())
                    .map(Response::body)
                    .map(GroupPermissions::permissions)
                    .ifPresent(setter);
        } catch (Exception e) {
            log.warn("[ARGUS] Retrieval of group membership failed!", e);
        }
    }

    public void clean() {
        membership = null;
        elections = null;
        metadata = null;
        displays = null;
    }
}
