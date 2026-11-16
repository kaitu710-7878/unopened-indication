package com.unopenedindication.network;

import com.unopenedindication.client.ClientUnopenedCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Syncs all unopened container positions within a single watched chunk as packed longs.
 */
public record SyncChunkUnopenedPacket(int chunkX, int chunkZ, long[] positions) {

    public static void encode(SyncChunkUnopenedPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.chunkX);
        buf.writeInt(msg.chunkZ);
        buf.writeLongArray(msg.positions);
    }

    public static SyncChunkUnopenedPacket decode(FriendlyByteBuf buf) {
        return new SyncChunkUnopenedPacket(buf.readInt(), buf.readInt(), buf.readLongArray());
    }

    public static void handle(SyncChunkUnopenedPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientUnopenedCache.handleChunkSync(msg.chunkX, msg.chunkZ, msg.positions)
        ));
        ctx.setPacketHandled(true);
    }
}
