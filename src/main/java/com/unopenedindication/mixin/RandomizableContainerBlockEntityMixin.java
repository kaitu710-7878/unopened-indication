package com.unopenedindication.mixin;

import com.unopenedindication.server.UnopenedTracker;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(RandomizableContainerBlockEntity.class)
public abstract class RandomizableContainerBlockEntityMixin {
    @Shadow
    @Nullable
    protected ResourceLocation lootTable;

    @Inject(
            method = "unpackLootTable",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/level/block/entity/RandomizableContainerBlockEntity;lootTable:Lnet/minecraft/resources/ResourceLocation;",
                    opcode = Opcodes.PUTFIELD,
                    shift = At.Shift.AFTER
            )
    )
    private void unopenedindication$onLootTableCleared(@Nullable Player player, CallbackInfo ci) {
        if (this.lootTable == null) {
            RandomizableContainerBlockEntity self = (RandomizableContainerBlockEntity) (Object) this;
            if (self.getLevel() instanceof ServerLevel serverLevel) {
                UnopenedTracker.onContainerStateChanged(serverLevel, self, false);
            }
        }
    }

    @Inject(method = "setLootTable(Lnet/minecraft/resources/ResourceLocation;J)V", at = @At("TAIL"))
    private void unopenedindication$onSetLootTable(@Nullable ResourceLocation lootTable, long seed, CallbackInfo ci) {
        RandomizableContainerBlockEntity self = (RandomizableContainerBlockEntity) (Object) this;
        if (self.getLevel() instanceof ServerLevel serverLevel) {
            UnopenedTracker.onContainerStateChanged(serverLevel, self, lootTable != null);
        }
    }
}
