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
package dev.zygon.argus.client.event;

import dev.zygon.argus.client.config.ArgusClientConfig;
import dev.zygon.argus.client.location.LocalLocationStorage;
import dev.zygon.argus.client.name.NameStorage;
import dev.zygon.argus.client.util.DimensionMapper;
import dev.zygon.argus.location.Dimension;
import dev.zygon.argus.location.Location;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public enum ChatEventHandler {

    INSTANCE;

    private static final String SNITCH_PATTERN_TEXT = ".{2}(Login|Logout|Enter)\\s{2}.{2}(\\w+)\\s{2}.{2}(\\w+)\\s{2}.{2}\\[(-?\\d+)\\s(-?\\d+)\\s(-?\\d+)]\\s{2}.{2}\\[(\\d.+)m\\s.{2}(\\w+\\s\\w+).{2}]";
    private static final String SNITCH_HOVER_PATTERN_TEXT = ".{2}(Location:)\\s.{2}\\((\\w+)\\)\\s\\[(-?\\d+)\\s(-?\\d+)\\s(-?\\d+)]\\s.{2}(Name:)\\s.{2}(\\w+)\\s.{2}(Group:)\\s.{2}(\\w+)";
    private static final String PEARL_PATTERN_TEXT = "(.{2}\\[(\\w+)]\\s)?.{2}(Your pearl is held by|The pearl of .{2}(\\w+) is held by)\\s.{2}(\\w+)\\s.{2}\\[(-?\\d+)\\s(-?\\d+)\\s(-?\\d+)\\s(\\w+)]";

    private static final Pattern SNITCH_PATTERN = Pattern.compile(SNITCH_PATTERN_TEXT);
    private static final Pattern SNITCH_HOVER_PATTERN = Pattern.compile(SNITCH_HOVER_PATTERN_TEXT);
    private static final Pattern PEARL_PATTERN = Pattern.compile(PEARL_PATTERN_TEXT);

    // Return is if the chat message should be 'consumed' here to not
    public boolean onChatMessage(String text, String hoverText) {
        var snitchMatcher = SNITCH_PATTERN.matcher(text);
        if (snitchMatcher.matches()) {
            return onSnitchHit(snitchMatcher, hoverText);
        } else {
            var pearlMatcher = PEARL_PATTERN.matcher(text);
            return pearlMatcher.matches() && onPearlHit(pearlMatcher);
        }
    }

    private boolean onSnitchHit(Matcher snitchMatcher, String hoverText) {
        var snitchHit = new SnitchHit(snitchMatcher);
        var snitchHitHover = checkSnitchHoverHit(hoverText);
        var dimension = snitchHitHover.map(SnitchHitHover::dimension)
                .map(DimensionMapper::fromSnitch)
                .orElse(Dimension.OVERWORLD);
        var group = snitchHitHover.map(SnitchHitHover::group)
                .orElse(null);
        var uuid = NameStorage.INSTANCE.idFromName(snitchHit.player());
        try {
            var location = Location.builder()
                    .x(Double.parseDouble(snitchHit.x()))
                    .y(Double.parseDouble(snitchHit.y()))
                    .z(Double.parseDouble(snitchHit.z()))
                    .w(dimension.ordinal())
                    .local(true)
                    .time(Instant.now())
                    .build();
            LocalLocationStorage.INSTANCE.track(uuid, location);
            return shouldHideMessage(group);
        } catch (NumberFormatException e) {
            log.warn("[ARGUS] Snitch hit data could not be translated to location data!", e);
            return false;
        }
    }

    private Optional<SnitchHitHover> checkSnitchHoverHit(String hoverText) {
        return Optional.ofNullable(hoverText)
                .map(SNITCH_HOVER_PATTERN::matcher)
                .filter(Matcher::matches)
                .map(SnitchHitHover::new);
    }

    private boolean onPearlHit(Matcher matcher) {
        var pearlHit = new PearlHit(matcher);
        var group = pearlHit.group();
        var dimension = DimensionMapper.fromSnitch(pearlHit.dimension());
        var holder = NameStorage.INSTANCE.idFromName(pearlHit.holder());
        var pearled = NameStorage.INSTANCE.extendedIdFromName(pearlHit.pearled());
        try {
            var location = Location.builder()
                    .x(Double.parseDouble(pearlHit.x()))
                    .y(Double.parseDouble(pearlHit.y()))
                    .z(Double.parseDouble(pearlHit.z()))
                    .w(dimension.ordinal())
                    .local(true)
                    .time(Instant.now())
                    .build();
            LocalLocationStorage.INSTANCE.track(holder, location);
            LocalLocationStorage.INSTANCE.track(pearled, location);
            return shouldHideMessage(group);
        } catch (NumberFormatException e) {
            log.warn("[ARGUS] Pearl hit data could not be translated to location data!", e);
            return false;
        }
    }

    private boolean shouldHideMessage(String group) {
        var config = ArgusClientConfig.getActiveConfig();
        return config.shouldHideGroupAlert(group);
    }

    private record SnitchHit(
            String action, String player, String snitch,
            String x, String y, String z,
            String distance, String cardinality) {

        private SnitchHit(Matcher matcher) {
            this(matcher.group(1), matcher.group(2), matcher.group(3),
                    matcher.group(4), matcher.group(5), matcher.group(6),
                    matcher.group(7), matcher.group(8));
        }
    }

    private record SnitchHitHover(
            String dimension, String x, String y, String z,
            String snitch, String group) {

        private SnitchHitHover(Matcher matcher) {
            this(matcher.group(2), matcher.group(3), matcher.group(4),
                    matcher.group(5), matcher.group(7), matcher.group(9));
        }
    }

    private record PearlHit(
            String group, String pearled, String holder,
            String x, String y, String z, String dimension) {

        private PearlHit(Matcher matcher) {
            this(matcher.group(2), matcher.group(4), matcher.group(5),
                    matcher.group(6), matcher.group(7), matcher.group(8),
                    matcher.group(9));
        }
    }
}
