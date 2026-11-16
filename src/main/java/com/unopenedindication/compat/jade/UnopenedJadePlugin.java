package com.unopenedindication.compat.jade;

import net.minecraft.world.level.block.Block;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Optional Jade integration plugin. Loaded only when Jade is present at runtime.
 */
@WailaPlugin
public class UnopenedJadePlugin implements IWailaPlugin {
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(UnopenedContainerProvider.INSTANCE, Block.class);
        registration.addConfig(UnopenedContainerProvider.DETAILS_ONLY_UID, true);
        registration.markAsClientFeature(UnopenedContainerProvider.UID);
        registration.markAsClientFeature(UnopenedContainerProvider.DETAILS_ONLY_UID);
    }
}
