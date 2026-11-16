package com.unopenedindication.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import javax.annotation.Nullable;

@Mixin(RandomizableContainerBlockEntity.class)
public interface LootTableContainerAccessor {
    @Accessor("lootTable")
    @Nullable
    ResourceLocation unopenedindication$getLootTable();
}
