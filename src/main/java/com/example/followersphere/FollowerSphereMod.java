package com.example.followersphere;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FollowerSphereMod implements ModInitializer {
    public static final String MOD_ID = "followersphere";
    public static final Identifier TOGGLE_SPHERE_ID = new Identifier(MOD_ID, "toggle");
    public static final Map<UUID, Boolean> sphereActiveMap = new ConcurrentHashMap<>();

    @Override
    public void onInitialize() {
        ServerPlayNetworking.registerGlobalReceiver(TOGGLE_SPHERE_ID, (server, player, handler, buf, responseSender) -> {
            boolean active = buf.readBoolean();
            sphereActiveMap.put(player.getUuid(), active);
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                boolean active = sphereActiveMap.getOrDefault(player.getUuid(), false);
                if (active) {
                    FollowerSphereServer.handleFollowerSphere(player);
                } else {
                    FollowerSphereServer.clearFollowers(player);
                }
            }
        });
    }
}
