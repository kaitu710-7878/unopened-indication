package com.unopenedindication.compat.jade;

import com.unopenedindication.UnopenedIndicationMod;
import com.unopenedindication.client.ClientUnopenedCache;
import com.unopenedindication.config.ModConfig;
import com.unopenedindication.server.ModContainerLootAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/**
 * Displays whether a targeted container is unopened or opened in Jade's tooltip.
 */
public enum UnopenedContainerProvider implements IBlockComponentProvider {
    INSTANCE;

    public static final ResourceLocation UID = new ResourceLocation(UnopenedIndicationMod.MOD_ID, "loot_status");
    public static final ResourceLocation DETAILS_ONLY_UID = new ResourceLocation(UnopenedIndicationMod.MOD_ID, "details_only");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (config.get(DETAILS_ONLY_UID) && !accessor.showDetails()) {
            return;
        }
        BlockEntity be = accessor.getBlockEntity();
        if (be == null || ModConfig.isBlacklisted(be.getType()) || !isContainerBlockEntity(be)) {
            return;
        }

        boolean unopened = ClientUnopenedCache.contains(accessor.getPosition().asLong());
        Component stateText = unopened
                ? Component.translatable("unopenedindication.jade.unopened").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)
                : Component.translatable("unopenedindication.jade.opened").withStyle(ChatFormatting.GRAY);

        tooltip.add(Component.translatable("unopenedindication.jade.status", stateText));
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    private static boolean isContainerBlockEntity(BlockEntity be) {
        return be instanceof Container
                || ModContainerLootAccessor.isModLootTrackable(be)
                || be.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent();
    }
}
