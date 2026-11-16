package com.unopenedindication.network;

import com.unopenedindication.client.ClientUnopenedCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 9-byte incremental update packet when a single container is opened, broken, or assigned a loot table.
 */
public record UpdateUnopenedPosPacket(long posLong, boolean unopened) {

    public static void encode(UpdateUnopenedPosPacket msg, FriendlyByteBuf buf) {
        buf.writeLong(msg.posLong);
        buf.writeBoolean(msg.unopened);
    }

    public static UpdateUnopenedPosPacket decode(FriendlyByteBuf buf) {
        return new UpdateUnopenedPosPacket(buf.readLong(), buf.readBoolean());
    }

    public static void handle(UpdateUnopenedPosPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientUnopenedCache.handleSingleUpdate(msg.posLong, msg.unopened)
        ));
        ctx.setPacketHandled(true);
    }
}
