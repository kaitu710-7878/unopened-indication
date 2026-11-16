package com.unopenedindication.network;

import com.unopenedindication.UnopenedIndicationMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

/**
 * Lightweight Forge SimpleChannel network handler.
 * Accepts missing mod on either side gracefully to prevent connection rejections.
 */
public final class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(UnopenedIndicationMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            NetworkRegistry.acceptMissingOr(PROTOCOL_VERSION::equals),
            NetworkRegistry.acceptMissingOr(PROTOCOL_VERSION::equals)
    );

    private NetworkHandler() {
    }

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(
                id++,
                SyncChunkUnopenedPacket.class,
                SyncChunkUnopenedPacket::encode,
                SyncChunkUnopenedPacket::decode,
                SyncChunkUnopenedPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        CHANNEL.registerMessage(
                id++,
                UpdateUnopenedPosPacket.class,
                UpdateUnopenedPosPacket::encode,
                UpdateUnopenedPosPacket::decode,
                UpdateUnopenedPosPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
    }
}
