package com.unopenedindication.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.unopenedindication.client.ClientUnopenedCache;
import com.unopenedindication.client.GlintMultiBufferSource;
import com.unopenedindication.client.GlintRenderTypes;
import com.unopenedindication.config.ModConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityRenderDispatcherMixin {
    @Shadow
    public Camera camera;

    @Shadow
    @Nullable
    public abstract <E extends BlockEntity> BlockEntityRenderer<E> getRenderer(E blockEntity);

    @ModifyVariable(
            method = "render(Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V",
            at = @At("HEAD"),
            argsOnly = true
    )
    private <E extends BlockEntity> MultiBufferSource unopenedindication$wrapGlintBuffer(MultiBufferSource bufferSource, E blockEntity) {
        if (bufferSource instanceof GlintMultiBufferSource || !unopenedindication$shouldRenderGlint(blockEntity)) {
            return bufferSource;
        }
        return GlintMultiBufferSource.wrap(bufferSource);
    }

    @Inject(
            method = "render(Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V",
            at = @At("TAIL")
    )
    private <E extends BlockEntity> void unopenedindication$renderExtraGlintPasses(
            E blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, CallbackInfo ci) {
        if (!(bufferSource instanceof GlintMultiBufferSource glintWrapper)) {
            return;
        }
        BlockEntityRenderer<E> renderer = this.getRenderer(blockEntity);
        if (renderer == null) {
            return;
        }

        MultiBufferSource rawSource = glintWrapper.unwrap();
        // Always render steady ambient base glow so brightness stays >= top-quarter even during wave troughs
        renderer.render(
                blockEntity,
                partialTick,
                poseStack,
                GlintMultiBufferSource.wrapGlintOnly(rawSource, GlintRenderTypes.BER_GLINT_AMBIENT),
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY
        );
        if (rawSource instanceof MultiBufferSource.BufferSource bs) {
            bs.endBatch(GlintRenderTypes.BER_GLINT_AMBIENT);
        }

        int extraPasses = ModConfig.glintPasses - 1;
        if (extraPasses > 0) {
            MultiBufferSource counterOnly = GlintMultiBufferSource.wrapGlintOnly(rawSource, GlintRenderTypes.BER_GLINT_COUNTER);
            renderer.render(blockEntity, partialTick, poseStack, counterOnly, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
            if (rawSource instanceof MultiBufferSource.BufferSource bs) {
                bs.endBatch(GlintRenderTypes.BER_GLINT_COUNTER);
            }
            if (extraPasses > 1) {
                MultiBufferSource primaryBright = GlintMultiBufferSource.wrapGlintOnly(rawSource, GlintRenderTypes.BER_GLINT_PRIMARY);
                for (int i = 1; i < extraPasses; i++) {
                    renderer.render(blockEntity, partialTick, poseStack, primaryBright, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
                }
                if (rawSource instanceof MultiBufferSource.BufferSource bs) {
                    bs.endBatch(GlintRenderTypes.BER_GLINT_PRIMARY);
                }
            }
        }
    }

    @Unique
    private <E extends BlockEntity> boolean unopenedindication$shouldRenderGlint(E blockEntity) {
        if (!ModConfig.enabled || !ModConfig.renderForBer || ClientUnopenedCache.isEmpty()) {
            return false;
        }
        BlockPos pos = blockEntity.getBlockPos();
        if (!ClientUnopenedCache.contains(pos.asLong())) {
            return false;
        }
        if (this.camera != null) {
            Vec3 camPos = this.camera.getPosition();
            double dx = (pos.getX() + 0.5) - camPos.x;
            double dy = (pos.getY() + 0.5) - camPos.y;
            double dz = (pos.getZ() + 0.5) - camPos.z;
            return dx * dx + dy * dy + dz * dz <= ModConfig.maxRenderDistanceSq;
        }
        return true;
    }
}
