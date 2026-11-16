package com.unopenedindication.mixin;

import com.unopenedindication.server.UnopenedTracker;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin {
    @Inject(method = "setRemoved", at = @At("HEAD"))
    private void unopenedindication$onSetRemoved(CallbackInfo ci) {
        if ((Object) this instanceof RandomizableContainerBlockEntity container
                && container.getLevel() instanceof ServerLevel serverLevel
                && ((LootTableContainerAccessor) container).unopenedindication$getLootTable() != null) {
            UnopenedTracker.onContainerStateChanged(serverLevel, container, false);
        }
    }
}
