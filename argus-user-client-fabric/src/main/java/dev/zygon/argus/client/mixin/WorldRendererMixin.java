package dev.zygon.argus.client.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.zygon.argus.client.config.ArgusClientConfig;
import dev.zygon.argus.client.groups.GroupAlignmentDisplay;
import dev.zygon.argus.client.groups.GroupAlignmentKey;
import dev.zygon.argus.client.groups.GroupStorage;
import dev.zygon.argus.client.location.LocationRender;
import dev.zygon.argus.client.location.LocationRenderEntry;
import dev.zygon.argus.client.location.LocationStorage;
import dev.zygon.argus.client.status.StatusStorage;
import dev.zygon.argus.client.util.DimensionMapper;
import dev.zygon.argus.location.Dimension;
import dev.zygon.argus.location.Location;
import dev.zygon.argus.location.LocationType;
import dev.zygon.argus.status.EffectStatus;
import dev.zygon.argus.status.UserMetadata;
import dev.zygon.argus.status.UserStatus;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static dev.zygon.argus.location.LocationType.BASIC_PING;

@Mixin(value = WorldRenderer.class)
public abstract class WorldRendererMixin {

    @Inject(at = @At("TAIL"), method = "render")
    public void render(MatrixStack matrices,
                       float tickDelta, long limitTime, boolean renderBlockOutline,
                       Camera camera, GameRenderer gameRenderer,
                       LightmapTextureManager lightmapTextureManager,
                       Matrix4f positionMatrix,
                       CallbackInfo ci) {
        var locations = LocationStorage.INSTANCE.getStorage()
                .values()
                .stream()
                .toList(); // take sync hit once?
        var renders = prepareInternal(locations, camera, tickDelta);
        renders.forEach(render -> renderLocation(matrices, render));
    }

    @Unique
    private List<LocationRender> prepareInternal(Collection<Location> locations, Camera camera, float tickDelta) {
        final var UNITS_PER_CHUNK = 16;
        var minecraft = MinecraftClient.getInstance();
        var renderDispatcher = minecraft.getEntityRenderDispatcher();
        var dimension = DimensionMapper.currentDimension();
        var config = ArgusClientConfig.getActiveConfig();
        if (config.isStreamerModeEnabled() || renderDispatcher.gameOptions == null ||
                renderDispatcher.camera == null || minecraft.world == null)
            return Collections.emptyList(); // game not ready :)
        var now = Instant.now();
        var nextRenders = new ArrayList<LocationRender>();
        var chunkMaxViewDistance = renderDispatcher.gameOptions.getViewDistance();
        var maxRenderDistance = chunkMaxViewDistance * UNITS_PER_CHUNK;
        var cameraPosition = camera.getPos();
        var self = minecraft.getSession().getProfile().getId();
        // for now, we will just shove all waypoints on top of each other :)
        // we should find a way to group waypoints based on an angle
        var maxViewDistance = config.getMaxViewDistance();
        var cameraX = cameraPosition.getX();
        var cameraY = cameraPosition.getY();
        var cameraZ = cameraPosition.getZ();
        var segments = new HashMap<Integer, List<Location>>();
        for (var location : locations) {
            var coordinates = location.coordinates();
            var w = coordinates.w();
            var x = coordinates.x() - cameraX;
            var y = coordinates.y() + 0.5 - cameraY;
            var z = coordinates.z() - cameraZ;
            var duration = Duration.between(coordinates.time(), now);
            if (location.type() == LocationType.MISC_USER || location.type() == BASIC_PING) {
                // these types are of blockpos variety, so offset them
                x += 0.5;
                y += 0.5;
                z += 0.5;
            } else {
                y += 2.0;
            }
            var distance = Math.sqrt(x * x + y * y + z * z);
            var user = location.user();
            var uuid = user.uuid();
            var isPing = location.type() == BASIC_PING || location.type() == LocationType.FOCUS_PING;
            var isExpired = isPing ? duration.toSeconds() >= config.getPingExpirationSeconds() :
                    duration.toMinutes() >= config.getLocationsExpirationMinutes();
            var canShowSelf = location.type() == BASIC_PING || !self.equals(uuid);
            if (distance <= maxViewDistance && !isExpired && canShowSelf &&
                    (!config.isSameDimensionOnly() || w == dimension.ordinal())) {
                if (distance > maxRenderDistance) { // render via slices
                    if (!isPing) {
                        var yaw = Math.atan2(x, z) * (180.0d / Math.PI);
                        var yawSegment = (int) Math.floor(yaw / (double) config.getYawSliceDegrees());
                        segments.putIfAbsent(yawSegment, new ArrayList<>());
                        segments.get(yawSegment).add(location);
                    }
                } else {
                    // read local entities if location is a user to see if we can just track that way instead :)
                    if (location.type() == LocationType.USER
                            && config.isReadLocalEnvironmentEnabled()
                            && w == dimension.ordinal()) {
                        var players = minecraft.world.getPlayers();
                        var possiblePlayer = players.stream()
                                .filter(p -> p.getUuid().equals(uuid))
                                .findFirst();
                        if (possiblePlayer.isPresent()) { // overwrite to smooth out animation
                            var player = possiblePlayer.get();
                            var position = player.getPos();
                            x = MathHelper.lerp(tickDelta, player.lastRenderX, position.getX()) - cameraX;
                            y = MathHelper.lerp(tickDelta, player.lastRenderY, position.getY()) + 2.5 - cameraY;
                            z = MathHelper.lerp(tickDelta, player.lastRenderZ, position.getZ()) - cameraZ;
                            distance = Math.sqrt(x * x + y * y + z * z);
                        }
                    }

                    if (!isPing || w == dimension.ordinal()) {
                        var scale = (float) (0.0078125d * (distance + 4.0) / 3.0);
                        var display = fetchDisplayFromUUID(uuid);
                        var status = fetchStatusFromUUID(uuid);
                        var color = Optional.ofNullable(status)
                                .filter(c -> isPing)
                                .map(UserStatus::metadata)
                                .map(UserMetadata::pingColor)
                                .map(Color::new)
                                .orElse(display.color());
                        var name = buildName(location, distance, dimension, display, now);
                        var renderEntry = new LocationRenderEntry(name, color, !isPing ? status : null);
                        var render = new LocationRender(x, y, z, scale, Collections.emptyMap(), List.of(renderEntry));
                        nextRenders.add(render);
                    }
                }
            }
        }

        for (var segment : segments.values()) {
            var alignmentDigest = new HashMap<GroupAlignmentKey, AtomicInteger>();
            var renderEntries = new ArrayList<LocationRenderEntry>(segment.size());
            var x = 0.0d;
            var y = 0.0d;
            var z = 0.0d;
            for (var location : segment) {
                var coordinates = location.coordinates();
                var lx = coordinates.x() - cameraX;
                var ly = coordinates.y() - cameraY;
                var lz = coordinates.z() - cameraZ;

                var distance = Math.sqrt(lx * lx + ly * ly + lz * lz);
                var user = location.user();
                var uuid = user.uuid();
                var display = fetchDisplayFromUUID(uuid);
                var name = buildName(location, distance, dimension, display, now);

                x += lx;
                y += ly;
                z += lz;
                renderEntries.add(new LocationRenderEntry(name, display.color(), null));
                if (config.isShowAlignmentsDigest() && !display.symbol().isBlank()) {
                    var key = new GroupAlignmentKey(display.symbol(), display.color());
                    alignmentDigest.putIfAbsent(key, new AtomicInteger(0));
                    alignmentDigest.get(key)
                            .incrementAndGet();
                }
            }
            Collections.sort(renderEntries);
            x = (x / segment.size());
            y = (y / segment.size());
            z = (z / segment.size());
            var distance = Math.sqrt(x * x + y * y + z * z);
            x = x / distance * maxRenderDistance;
            y = y / distance * maxRenderDistance;
            z = z / distance * maxRenderDistance;
            var scale = (float) (0.0078125d * (maxRenderDistance + 4.0) / 3.0);
            nextRenders.add(new LocationRender(x, y, z, scale, alignmentDigest, renderEntries));
        }
        return nextRenders;
    }

    @Unique
    private String buildName(Location location, double distance, Dimension dimension, GroupAlignmentDisplay display, Instant now) {
        var config = ArgusClientConfig.getActiveConfig();
        var user = location.user();
        var symbol = display.symbol();
        var coordinates = location.coordinates();
        var w = coordinates.w();
        var duration = Duration.between(coordinates.time(), now);
        var isPing = location.type() == BASIC_PING || location.type() == LocationType.FOCUS_PING;
        var name = new StringBuilder();
        if (!symbol.isBlank()) {
            name.append("[").append(symbol).append("]");
        }
        name.append(user.name());
        if (!isPing && duration.toSeconds() > config.getLocationTimerStartSeconds()) {
            name.append(" ");
            var minutes = duration.toMinutesPart();
            var seconds = duration.toSecondsPart();
            if (minutes > 0) {
                name.append(minutes).append(" m ");
            }
            name.append(seconds).append(" s");
        }
        name.append(" (")
                .append((int) distance)
                .append(" m)");
        if (config.isShowDimensionIndicator() && w != dimension.ordinal()) {
            name.append(" ").append(w > dimension.ordinal() ? "↑" : "↓");
        }
        return name.toString();
    }

    @Unique
    private GroupAlignmentDisplay fetchDisplayFromUUID(UUID uuid) {
        var config = ArgusClientConfig.getActiveConfig();
        var displays = GroupStorage.INSTANCE.getDisplays();
        var noDisplays = displays == null || displays.isEmpty();
        if (config.shouldShowNameOverwrite() && !noDisplays) {
            return displays.getOrDefault(uuid, GroupAlignmentDisplay.DEFAULT_DISPLAY);
        } else {
            return GroupAlignmentDisplay.DEFAULT_DISPLAY;
        }
    }

    @Unique @Nullable
    private UserStatus fetchStatusFromUUID(UUID uuid) {
        var config = ArgusClientConfig.getActiveConfig();
        var statuses = StatusStorage.INSTANCE.getStorage();
        var noStatuses = statuses == null || statuses.isEmpty();
        if (config.shouldShowStatusInformation() && !noStatuses) {
            var status = statuses.get(uuid);
            if (status != null && config.isShowHealthOnly()) {
                status = new UserStatus(uuid, status.health(), List.of(), List.of());
            }
            return status;
        } else {
            return null;
        }
    }

    @Unique
    private void renderLocation(MatrixStack stack, LocationRender render) {
        var client = MinecraftClient.getInstance();
        var entityRender = client.getEntityRenderDispatcher();
        var rotation = entityRender.getRotation();
        var tessellator = Tessellator.getInstance();
        var buffer = tessellator.getBuffer();

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableDepthTest();

        stack.push();
        stack.translate(render.x(), render.y(), render.z());
        stack.multiply(rotation);
        stack.scale(-render.scale(), -render.scale(), render.scale());
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SrcFactor.SRC_ALPHA,
                GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SrcFactor.ONE,
                GlStateManager.DstFactor.ZERO
        );
        RenderSystem.disableTexture();

        var matrix = stack.peek().getPositionMatrix();
        var waypointColor = render.averageColor();
        drawWaypoint(tessellator, buffer, matrix, waypointColor);
        drawNameTags(matrix, render);

        stack.pop();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
    }

    @Unique
    private void drawWaypoint(Tessellator tessellator, BufferBuilder buffer, Matrix4f matrix, Color waypointColor) {
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, -4.0f, 24.0f, 0.0f)
                .color(waypointColor.getRed(), waypointColor.getGreen(), waypointColor.getBlue(), 0xFF)
                .next();
        buffer.vertex(matrix, 4.0f, 24.0f, 0.0f)
                .color(waypointColor.getRed(), waypointColor.getGreen(), waypointColor.getBlue(), 0xFF)
                .next();
        buffer.vertex(matrix, 4.0f, 16.0f, 0.0f)
                .color(waypointColor.getRed(), waypointColor.getGreen(), waypointColor.getBlue(), 0xFF)
                .next();
        buffer.vertex(matrix, -4.0f, 16.0f, 0.0f)
                .color(waypointColor.getRed(), waypointColor.getGreen(), waypointColor.getBlue(), 0xFF)
                .next();
        tessellator.draw();
    }

    @Unique
    private void drawNameTags(Matrix4f matrix, LocationRender render) {
        var client = MinecraftClient.getInstance();
        var builders = client.getBufferBuilders();
        var vertexConsumerProvider = builders.getEntityVertexConsumers();
        var textRenderer = client.textRenderer;
        var alignmentDigest = render.alignmentDigest();

        RenderSystem.enableTexture();
        var offset = alignmentDigest.isEmpty() ? 0.0f : 1.0f;
        if (!alignmentDigest.isEmpty()) {
            drawAlignmentDigest(matrix, alignmentDigest);
        }
        final var textHeight = textRenderer.fontHeight;
        final var textStagger = 1.0f;
        for (var user : render.entries()) {
            var textWidth = textRenderer.getWidth(user.text());
            var textStart = textWidth / 2;
            var color = user.color().getRGB();
            var text = new LiteralText(user.text());

            if (user.status() != null) {
                drawUserStatus(matrix, user.status(), offset);
                offset++;
            }
            textRenderer.draw(text, -textStart, -textHeight * offset - textStagger * offset, color, false, matrix,
                    vertexConsumerProvider, true, 0x60000000, 0xF000F0);
            vertexConsumerProvider.draw();

            offset++;
        }
        RenderSystem.disableTexture();
    }

    @Unique
    private void drawUserStatus(Matrix4f matrix, UserStatus status, float offset) {
        var now = Instant.now(); // less precise compared to start of render, but close enough together probably :)
        var client = MinecraftClient.getInstance();
        var builders = client.getBufferBuilders();
        var vertexConsumerProvider = builders.getEntityVertexConsumers();
        var textRenderer = client.textRenderer;
        final var textHeight = textRenderer.fontHeight;
        final var textStagger = 1.0f;
        final var BREAK_PIXEL_WIDTH = 2;

        var healthColor = Color.RED.getRGB(); // TODO make this configurable?
        var healthText = "♥ " + status.health(); // TODO make this configurable?
        var items = status.items();
        var activeEffects = status.effects()
                .stream()
                .filter(e -> !e.isExpired(now))
                .toList();

        var fullHealthTextWidth = textRenderer.getWidth(healthText);
        var fullItemTextWidth = items.stream()
                .map(i -> i.symbol() + " " + i.count())
                .map(textRenderer::getWidth)
                .reduce(0, Integer::sum) + BREAK_PIXEL_WIDTH * items.size();
        var fullEffectTextWidth = activeEffects.stream()
                        .map(EffectStatus::symbol)
                        .map(textRenderer::getWidth)
                        .reduce(0, Integer::sum) + BREAK_PIXEL_WIDTH * activeEffects.size();
        var breakCount = 1 + (items.isEmpty() ? 0 : 1) +
                (activeEffects.isEmpty() ? 0 : 1);
        var fullTextWidth = fullHealthTextWidth + fullItemTextWidth + fullEffectTextWidth +
                BREAK_PIXEL_WIDTH * breakCount;
        var textIndex = -fullTextWidth / 2;
        var y = -textHeight * offset - textStagger * offset;

        // render health
        textRenderer.draw(healthText, textIndex, y, healthColor, false, matrix,
                vertexConsumerProvider, true, 0x60000000, 0xF000F0);
        textIndex += fullHealthTextWidth + BREAK_PIXEL_WIDTH;

        // render effects
        for (var effect : activeEffects) {
            textRenderer.draw(effect.symbol(), textIndex, y, effect.color(), false, matrix,
                    vertexConsumerProvider, true, 0x60000000, 0xF000F0);
            textIndex += textRenderer.getWidth(effect.symbol()) + BREAK_PIXEL_WIDTH;
        }

        // render items
        for (var item : items) {
            var itemText = item.symbol() + " " + item.count();
            textRenderer.draw(itemText, textIndex, y, item.color(), false, matrix,
                    vertexConsumerProvider, true, 0x60000000, 0xF000F0);
            textIndex += textRenderer.getWidth(itemText) + BREAK_PIXEL_WIDTH;
        }
    }

    @Unique
    private void drawAlignmentDigest(Matrix4f matrix, Map<GroupAlignmentKey, AtomicInteger> digest) {
        var client = MinecraftClient.getInstance();
        var builders = client.getBufferBuilders();
        var vertexConsumerProvider = builders.getEntityVertexConsumers();
        var textRenderer = client.textRenderer;

        final var BREAK_PIXEL_WIDTH = 2;
        var fullTextWidth = digest.entrySet()
                .stream()
                .map(e -> e.getKey().symbol() + " " + e.getValue().toString())
                .map(textRenderer::getWidth)
                .reduce(0, Integer::sum);
        fullTextWidth += BREAK_PIXEL_WIDTH * digest.size(); // spaces between
        var textIndex = -fullTextWidth / 2;
        for (var entry : digest.entrySet()) {
            var k = entry.getKey();
            var v = entry.getValue();
            var symbol = k.symbol();
            var color = k.color();
            var text = symbol + " " + v.toString();
            textRenderer.draw(text, textIndex, 0, color.getRGB(), false, matrix,
                    vertexConsumerProvider, true, 0x60000000, 0xF000F0);
            textIndex += textRenderer.getWidth(text) + BREAK_PIXEL_WIDTH;
        }
    }
}
