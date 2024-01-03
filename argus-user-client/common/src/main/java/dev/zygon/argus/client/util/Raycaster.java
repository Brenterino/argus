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
package dev.zygon.argus.client.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.RaycastContext;

import java.util.Objects;

/**
 * Code adapted from: <a href="https://fabricmc.net/wiki/tutorial:pixel_raycast">tutorial:pixel_raycast</a>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Raycaster {

    public static HitResult raycast() {
        var result = raycastInternal();
        return Objects.requireNonNullElseGet(result, () -> new HitResult(Vec3d.ZERO) {

            @Override
            public Type getType() {
                return Type.MISS;
            }
        });
    }

    private static HitResult raycastInternal() {
        var client = MinecraftClient.getInstance();
        if (client.cameraEntity == null) return null;
        var camera = client.cameraEntity.getRotationVec(1.0f);
        var verticalRotationAxis = new Vec3f(camera);
        verticalRotationAxis.cross(Vec3f.POSITIVE_Y);
        if (!verticalRotationAxis.normalize()) return null;
        var horizontalRotationAxis = new Vec3f(camera);
        horizontalRotationAxis.cross(verticalRotationAxis);
        horizontalRotationAxis.normalize();

        verticalRotationAxis = new Vec3f(camera);
        verticalRotationAxis.cross(horizontalRotationAxis);

        var direction = map(camera, horizontalRotationAxis, verticalRotationAxis);
        return raycastInDirection(direction);
    }

    private static Vec3d map(Vec3d center, Vec3f horizontalRotationAxis, Vec3f verticalRotationAxis) {
        final var temp2 = new Vec3f(center);
        temp2.rotate(verticalRotationAxis.getDegreesQuaternion(0));
        temp2.rotate(horizontalRotationAxis.getDegreesQuaternion(0));
        return new Vec3d(temp2);
    }

    private static HitResult raycastInDirection(Vec3d direction) {
        final var UNITS_PER_CHUNK = 16;
        var client = MinecraftClient.getInstance();
        var entity = client.getCameraEntity();
        var renderDispatcher = client.getEntityRenderDispatcher();
        var chunkMaxViewDistance = renderDispatcher.gameOptions.getViewDistance();
        var distance = (double) chunkMaxViewDistance * UNITS_PER_CHUNK;
        if (entity == null || client.world == null) {
            return null;
        }
        var target = raycast(entity, distance, direction);
        var extendedReach = distance;
        var cameraPos = entity.getCameraPosVec(1.0f);

        extendedReach = extendedReach * extendedReach;
        if (target != null) {
            extendedReach = target.getPos().squaredDistanceTo(cameraPos);
        }

        var vec3d3 = cameraPos.add(direction.multiply(distance));
        var box = entity.getBoundingBox().stretch(entity.getRotationVec(1.0F)
                .multiply(distance)).expand(1.0D, 1.0D, 1.0D);
        var entityHitResult = ProjectileUtil.raycast(entity, cameraPos, vec3d3, box, entityx -> !entityx.isSpectator() && entityx.collides(), extendedReach);
        if (entityHitResult == null) {
            return target;
        }
        var entity2 = entityHitResult.getEntity();
        var vec3d4 = entityHitResult.getPos();
        var g = cameraPos.squaredDistanceTo(vec3d4);
        if (g < extendedReach || target == null) {
            target = entityHitResult;
            if (entity2 instanceof LivingEntity || entity2 instanceof ItemFrameEntity) {
                client.targetedEntity = entity2;
            }
        }
        return target;
    }

    private static HitResult raycast(Entity entity, double maxDistance, Vec3d direction) {
        var end = entity.getCameraPosVec((float) 1.0)
                .add(direction.multiply(maxDistance));
        return entity.world.raycast(new RaycastContext(entity.getCameraPosVec((float) 1.0), end,
                RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, entity));
    }
}
