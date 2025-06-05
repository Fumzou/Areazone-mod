package com.example.followersphere;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class FollowerSphereServer {
    private static final double FOLLOW_DISTANCE = 1.0D;
    private static final double FOLLOW_SPEED = 1.0D;

    public static void handleFollowerSphere(ServerPlayerEntity player) {
        Vec3d playerPos = player.getPos();
        Box zone = new Box(
                playerPos.x - 100, playerPos.y - 1, playerPos.z - 100,
                playerPos.x + 100, playerPos.y + 100, playerPos.z + 100
        );
        List<LivingEntity> entities = player.getWorld().getEntitiesByClass(LivingEntity.class, zone,
                e -> (e instanceof AnimalEntity) && !(e instanceof ServerPlayerEntity));

        Vec3d look = player.getRotationVec(1.0F).normalize();
        Vec3d target = playerPos.subtract(look.multiply(FOLLOW_DISTANCE));

        for (LivingEntity entity : entities) {
            EntityNavigation nav = entity.getNavigation();
            nav.startMovingTo(target.x, target.y, target.z, FOLLOW_SPEED);
        }
    }

    public static void clearFollowers(ServerPlayerEntity player) {
        Vec3d playerPos = player.getPos();
        Box zone = new Box(
                playerPos.x - 100, playerPos.y - 1, playerPos.z - 100,
                playerPos.x + 100, playerPos.y + 100, playerPos.z + 100
        );
        List<LivingEntity> entities = player.getWorld().getEntitiesByClass(LivingEntity.class, zone,
                e -> (e instanceof AnimalEntity) && !(e instanceof ServerPlayerEntity));

        for (LivingEntity entity : entities) {
            EntityNavigation nav = entity.getNavigation();
            nav.stop();
        }
    }
}
