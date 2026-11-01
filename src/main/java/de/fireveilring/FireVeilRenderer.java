package de.fireveilring;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.phys.Vec3;

/** Zeichnet den flachen Ring auf Fusshoehe, solange der Veil aktiv ist. */
public final class FireVeilRenderer {
    private FireVeilRenderer() {}

    public static void init() {
        LevelRenderEvents.COLLECT_SUBMITS.register(FireVeilRenderer::render);
    }

    private static void render(LevelRenderContext context) {
        FireVeilConfig cfg = FireVeilConfig.get();
        if (!cfg.enabled || FireVeilState.remainingSeconds() <= 0.0) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }

        float partialTick = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        Vec3 center = cfg.followPlayer ? player.getPosition(partialTick) : FireVeilState.castPos();
        Vec3 cam = context.levelState().cameraRenderState.pos;

        final int argb = ((int) Math.round(255.0 * cfg.opacity) << 24) | cfg.rgb();
        final double inner = Math.max(0.0, cfg.radius - cfg.thickness / 2.0);
        final double outer = cfg.radius + cfg.thickness / 2.0;
        final int segments = cfg.segments;

        PoseStack pose = context.poseStack();
        pose.pushPose();
        pose.translate(center.x - cam.x, center.y + cfg.heightOffset - cam.y, center.z - cam.z);
        context.submitNodeCollector().submitCustomGeometry(pose, RenderTypes.debugQuads(),
                (p, buffer) -> drawRing(p, buffer, inner, outer, segments, argb));
        pose.popPose();
    }

    private static void drawRing(PoseStack.Pose p, VertexConsumer buffer,
                                 double inner, double outer, int segments, int argb) {
        double step = (Math.PI * 2.0) / segments;
        for (int i = 0; i < segments; i++) {
            double a0 = i * step;
            double a1 = (i + 1) * step;
            float c0 = (float) Math.cos(a0), s0 = (float) Math.sin(a0);
            float c1 = (float) Math.cos(a1), s1 = (float) Math.sin(a1);

            float ix0 = (float) (c0 * inner), iz0 = (float) (s0 * inner);
            float ox0 = (float) (c0 * outer), oz0 = (float) (s0 * outer);
            float ix1 = (float) (c1 * inner), iz1 = (float) (s1 * inner);
            float ox1 = (float) (c1 * outer), oz1 = (float) (s1 * outer);

            // Vorderseite
            buffer.addVertex(p, ix0, 0f, iz0).setColor(argb);
            buffer.addVertex(p, ox0, 0f, oz0).setColor(argb);
            buffer.addVertex(p, ox1, 0f, oz1).setColor(argb);
            buffer.addVertex(p, ix1, 0f, iz1).setColor(argb);
            // Rueckseite (falls Backface-Culling aktiv ist)
            buffer.addVertex(p, ix1, 0f, iz1).setColor(argb);
            buffer.addVertex(p, ox1, 0f, oz1).setColor(argb);
            buffer.addVertex(p, ox0, 0f, oz0).setColor(argb);
            buffer.addVertex(p, ix0, 0f, iz0).setColor(argb);
        }
    }
}
