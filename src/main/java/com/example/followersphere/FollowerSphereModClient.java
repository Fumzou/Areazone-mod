package com.example.followersphere;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.PacketByteBuf;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class FollowerSphereModClient implements ClientModInitializer {
    private static KeyBinding toggleKey;
    private static boolean sphereActive = false;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.followersphere.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_KP_1,
                "key.categories.misc"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) {
                sphereActive = !sphereActive;
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeBoolean(sphereActive);
                ClientPlayNetworking.send(FollowerSphereMod.TOGGLE_SPHERE_ID, buf);
                if (client.player != null) {
                    client.player.sendMessage(Text.literal(sphereActive ? "Follower Sphere activée" : "Follower Sphere désactivée"), false);
                }
            }
        });

        WorldRenderEvents.END.register(context -> {
            if (!sphereActive) return;
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null) return;

            double px = mc.player.getX();
            double py = mc.player.getY();
            double pz = mc.player.getZ();

            MatrixStack matrices = context.matrixStack();
            matrices.push();
            matrices.translate(-mc.gameRenderer.getCamera().getPos().x,
                    -mc.gameRenderer.getCamera().getPos().y,
                    -mc.gameRenderer.getCamera().getPos().z);

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1f,1f,1f,0.1f);

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(com.mojang.blaze3d.vertex.VertexFormat.Mode.LINE_STRIP, net.minecraft.client.render.VertexFormats.POSITION);

            int radius = 100;
            int latSteps = 30;
            int lonSteps = 60;
            for (int i = 0; i <= latSteps; i++) {
                double theta = (Math.PI / 2) * i / latSteps;
                double sinTheta = Math.sin(theta);
                double cosTheta = Math.cos(theta);
                for (int j = 0; j <= lonSteps; j++) {
                    double phi = (2 * Math.PI) * j / lonSteps;
                    double x = px + radius * cosTheta * Math.cos(phi);
                    double y = py + radius * sinTheta;
                    double z = pz + radius * cosTheta * Math.sin(phi);
                    buffer.vertex(x, y, z).next();
                }
            }

            tessellator.draw();
            RenderSystem.disableBlend();
            matrices.pop();
        });
    }
}
