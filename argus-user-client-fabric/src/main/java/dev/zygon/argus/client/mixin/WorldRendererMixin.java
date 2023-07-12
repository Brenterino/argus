package dev.zygon.argus.client.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.zygon.argus.client.config.ArgusClientConfig;
import dev.zygon.argus.client.groups.GroupAlignmentDisplay;
import dev.zygon.argus.client.groups.GroupStorage;
import dev.zygon.argus.client.location.LocationRender;
import dev.zygon.argus.client.location.LocationRenderEntry;
import dev.zygon.argus.client.location.LocationStorage;
import dev.zygon.argus.location.Location;
import dev.zygon.argus.location.LocationType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
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
        var client = MinecraftClient.getInstance();
        var renderDispatcher = client.getEntityRenderDispatcher();
        var config = ArgusClientConfig.getActiveConfig();
        if (config.isStreamerModeEnabled() || renderDispatcher.gameOptions == null ||
                renderDispatcher.camera == null || client.world == null)
            return Collections.emptyList(); // game not ready :)
        var now = Instant.now();
        var nextRenders = new ArrayList<LocationRender>();
        var chunkMaxViewDistance = renderDispatcher.gameOptions.getViewDistance();
        var maxRenderDistance = chunkMaxViewDistance * UNITS_PER_CHUNK;
        var cameraPosition = camera.getPos();
        var self = client.getSession().getProfile().getId();
        // for now, we will just shove all waypoints on top of each other :)
        // we should find a way to group waypoints based on an angle
        var maxViewDistance = config.getMaxViewDistance();
        var cameraX = cameraPosition.getX();
        var cameraY = cameraPosition.getY();
        var cameraZ = cameraPosition.getZ();
        var segments = new HashMap<Integer, List<Location>>();
        for (var location : locations) {
            var coordinates = location.coordinates();
            var x = coordinates.x() - cameraX;
            var y = coordinates.y() + 2.5 - cameraY;
            var z = coordinates.z() - cameraZ;
            if (location.type() == LocationType.MISC_USER) { // snitch is blockpos, so center it inside the block
                x += 0.5;
                z += 0.5;
            }
            var distance = Math.sqrt(x * x + y * y + z * z);
            var user = location.user();
            var uuid = user.uuid();
            if (distance <= maxViewDistance && !self.equals(uuid)) {
                if (distance > maxRenderDistance) { // render via slices
                    var yaw = Math.atan2(x, z) * (180.0d / Math.PI);
                    var yawSegment = (int) Math.floor(yaw / (double) config.getYawSliceDegrees());
                    segments.putIfAbsent(yawSegment, new ArrayList<>());
                    segments.get(yawSegment).add(location);
                } else {
                    // read local entities if location is a user to see if we can just track that way instead :)
                    if (location.type() == LocationType.USER && config.isReadLocalEntitiesEnabled()) {
                        var players = client.world.getPlayers();
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
                    var scale = (float) (0.0078125d * (distance + 4.0) / 3.0);
                    var display = fetchDisplayFromUUID(uuid);
                    var name = buildName(location, distance, display, now);
                    var renderEntry = new LocationRenderEntry(name, display.color());
                    var render = new LocationRender(x, y, z, scale, List.of(renderEntry));
                    nextRenders.add(render);
                }
            }
        }

        for (var segment : segments.values()) {
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
                var name = buildName(location, distance, display, now);

                x += lx;
                y += ly;
                z += lz;
                renderEntries.add(new LocationRenderEntry(name, display.color()));
            }
            x = (x / segment.size());
            y = (y / segment.size());
            z = (z / segment.size());
            var distance = Math.sqrt(x * x + y * y + z * z);
            x = x / distance * maxRenderDistance;
            y = y / distance * maxRenderDistance;
            z = z / distance * maxRenderDistance;
            var scale = (float) (0.0078125d * (maxRenderDistance + 4.0) / 3.0);
            nextRenders.add(new LocationRender(x, y, z, scale, renderEntries));
        }
        return nextRenders;
    }

    @Unique
    private String buildName(Location location, double distance, GroupAlignmentDisplay display, Instant now) {
        var user = location.user();
        var symbol = display.symbol();
        var coordinates = location.coordinates();
        var duration = Duration.between(coordinates.time(), now);
        var name = new StringBuilder();
        if (!symbol.isBlank()) {
            name.append("[").append(symbol).append("]");
        }
        name.append(user.name())
                .append(" ");
        if (duration.toSeconds() > 5L) { // TODO make this a configuration :)
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

        // TODO calculate placement for text in a new form, thinking of using sqrt(entryCount) to shape it
        RenderSystem.enableTexture();
        var offset = 0.0f;
        final var textHeight = textRenderer.fontHeight;
        final var textStagger = 1.0f;
        for (var user : render.entries()) {
            var textWidth = textRenderer.getWidth(user.text());
            var textStart = textWidth / 2;
            var color = user.color().getRGB();
            var text = new LiteralText(user.text());

            textRenderer.draw(text, -textStart, -textHeight * offset - textStagger * offset, color, false, matrix,
                    vertexConsumerProvider, true, 0x60000000, 0xF000F0);
            vertexConsumerProvider.draw();

            offset++;
        }
        RenderSystem.disableTexture();
    }
}
