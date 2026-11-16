package com.unopenedindication.mixin.client;

import com.unopenedindication.client.ClientUnopenedCache;
import com.unopenedindication.client.GlintMultiBufferSource;
import com.unopenedindication.config.ModConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityRenderDispatcherMixin {
    @Shadow
    public Camera camera;

    @ModifyVariable(
            method = "render(Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V",
            at = @At("HEAD"),
            argsOnly = true
    )
    private <E extends BlockEntity> MultiBufferSource unopenedindication$wrapGlintBuffer(MultiBufferSource bufferSource, E blockEntity) {
        if (!ModConfig.enabled || !ModConfig.renderForBer || ClientUnopenedCache.isEmpty()) {
            return bufferSource;
        }
        BlockPos pos = blockEntity.getBlockPos();
        if (!ClientUnopenedCache.contains(pos.asLong())) {
            return bufferSource;
        }
        if (this.camera != null) {
            Vec3 camPos = this.camera.getPosition();
            double dx = (pos.getX() + 0.5) - camPos.x;
            double dy = (pos.getY() + 0.5) - camPos.y;
            double dz = (pos.getZ() + 0.5) - camPos.z;
            if (dx * dx + dy * dy + dz * dz > ModConfig.maxRenderDistanceSq) {
                return bufferSource;
            }
        }
        return GlintMultiBufferSource.wrap(bufferSource);
    }
}
