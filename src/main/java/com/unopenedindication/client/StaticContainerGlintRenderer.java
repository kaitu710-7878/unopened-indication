package com.unopenedindication.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.unopenedindication.UnopenedIndicationMod;
import com.unopenedindication.config.ModConfig;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/**
 * Renders batched enchantment glint overlays for static block containers without a BlockEntityRenderer (e.g., barrels).
 */
@Mod.EventBusSubscriber(modid = UnopenedIndicationMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class StaticContainerGlintRenderer {
    private static final Direction[] DIRECTIONS_WITH_NULL = {
            Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, null
    };
    private static final BlockPos.MutableBlockPos MUTABLE_POS = new BlockPos.MutableBlockPos();
    private static final RandomSource MODEL_RANDOM = RandomSource.create(42L);

    private StaticContainerGlintRenderer() {
    }

    @SubscribeEvent
    public static void onRenderLevelStage(final RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {
            return;
        }
        if (!ModConfig.enabled || !ModConfig.renderForStatic) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) {
            return;
        }

        ClientUnopenedCache.classifyPending(level, mc.getBlockEntityRenderDispatcher());

        LongOpenHashSet staticSet = ClientUnopenedCache.getStaticUnopened();
        if (staticSet.isEmpty()) {
            return;
        }

        Vec3 camPos = event.getCamera().getPosition();
        double maxDistSq = ModConfig.maxRenderDistanceSq;
        Frustum frustum = event.getFrustum();
        PoseStack poseStack = event.getPoseStack();
        BlockRenderDispatcher blockRenderer = mc.getBlockRenderer();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer glintConsumer = bufferSource.getBuffer(RenderType.entityGlintDirect());
        int passes = ModConfig.glintPasses;

        boolean renderedAny = false;
        LongIterator it = staticSet.iterator();
        while (it.hasNext()) {
            long posLong = it.nextLong();
            int x = BlockPos.getX(posLong);
            int y = BlockPos.getY(posLong);
            int z = BlockPos.getZ(posLong);

            double dx = (x + 0.5) - camPos.x;
            double dy = (y + 0.5) - camPos.y;
            double dz = (z + 0.5) - camPos.z;
            if (dx * dx + dy * dy + dz * dz > maxDistSq) {
                continue;
            }

            if (!frustum.isVisible(new AABB(x, y, z, x + 1.0, y + 1.0, z + 1.0))) {
                continue;
            }

            MUTABLE_POS.set(x, y, z);
            BlockState state = level.getBlockState(MUTABLE_POS);
            if (state.getRenderShape() != RenderShape.MODEL) {
                continue;
            }

            BakedModel model = blockRenderer.getBlockModel(state);
            poseStack.pushPose();
            poseStack.translate(x - camPos.x, y - camPos.y, z - camPos.z);
            // Slightly expand around block center to prevent z-fighting with baked chunk geometry
            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.scale(1.003F, 1.003F, 1.003F);
            poseStack.translate(-0.5, -0.5, -0.5);

            PoseStack.Pose pose = poseStack.last();
            for (int pass = 0; pass < passes; pass++) {
                for (Direction dir : DIRECTIONS_WITH_NULL) {
                    MODEL_RANDOM.setSeed(42L);
                    List<BakedQuad> quads = model.getQuads(state, dir, MODEL_RANDOM, ModelData.EMPTY, null);
                    for (int i = 0, size = quads.size(); i < size; i++) {
                        glintConsumer.putBulkData(
                                pose,
                                quads.get(i),
                                1.0F, 1.0F, 1.0F,
                                LightTexture.FULL_BRIGHT,
                                OverlayTexture.NO_OVERLAY
                        );
                    }
                }
            }

            poseStack.popPose();
            renderedAny = true;
        }

        if (renderedAny) {
            bufferSource.endBatch(RenderType.entityGlintDirect());
        }
    }
}
