package com.unopenedindication.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.unopenedindication.client.ClientUnopenedCache;
import com.unopenedindication.client.GlintMultiBufferSource;
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
        int extraPasses = ModConfig.glintPasses - 1;
        if (extraPasses <= 0 || !(bufferSource instanceof GlintMultiBufferSource)) {
            return;
        }
        BlockEntityRenderer<E> renderer = this.getRenderer(blockEntity);
        if (renderer == null) {
            return;
        }
        // Extra additive glint passes make unopened containers immediately recognizable at a glance
        MultiBufferSource glintOnly = GlintMultiBufferSource.wrapGlintOnly(bufferSource);
        for (int i = 0; i < extraPasses; i++) {
            renderer.render(blockEntity, partialTick, poseStack, glintOnly, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
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
