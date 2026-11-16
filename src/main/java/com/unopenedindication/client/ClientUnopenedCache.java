package com.unopenedindication.client;

import com.unopenedindication.UnopenedIndicationMod;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Zero-allocation client-side cache for unopened loot container coordinates using FastUtil primitive collections.
 */
@Mod.EventBusSubscriber(modid = UnopenedIndicationMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientUnopenedCache {
    private static final LongOpenHashSet ALL_UNOPENED = new LongOpenHashSet();
    private static final LongOpenHashSet STATIC_UNOPENED = new LongOpenHashSet();
    private static final LongOpenHashSet PENDING_CLASSIFY = new LongOpenHashSet();
    private static final Long2ObjectOpenHashMap<LongOpenHashSet> BY_CHUNK = new Long2ObjectOpenHashMap<>();

    private static final BlockPos.MutableBlockPos MUTABLE_POS = new BlockPos.MutableBlockPos();

    private ClientUnopenedCache() {
    }

    public static boolean isEmpty() {
        return ALL_UNOPENED.isEmpty();
    }

    public static boolean contains(long posLong) {
        return ALL_UNOPENED.contains(posLong);
    }

    public static LongOpenHashSet getStaticUnopened() {
        return STATIC_UNOPENED;
    }

    public static void handleChunkSync(int chunkX, int chunkZ, long[] positions) {
        long chunkKey = ChunkPos.asLong(chunkX, chunkZ);
        removeChunkInternal(chunkKey);

        if (positions.length == 0) {
            return;
        }

        LongOpenHashSet chunkSet = new LongOpenHashSet(positions.length);
        for (long posLong : positions) {
            chunkSet.add(posLong);
            ALL_UNOPENED.add(posLong);
            PENDING_CLASSIFY.add(posLong);
        }
        BY_CHUNK.put(chunkKey, chunkSet);
    }

    public static void handleSingleUpdate(long posLong, boolean unopened) {
        int chunkX = BlockPos.getX(posLong) >> 4;
        int chunkZ = BlockPos.getZ(posLong) >> 4;
        long chunkKey = ChunkPos.asLong(chunkX, chunkZ);

        if (unopened) {
            ALL_UNOPENED.add(posLong);
            PENDING_CLASSIFY.add(posLong);
            LongOpenHashSet chunkSet = BY_CHUNK.get(chunkKey);
            if (chunkSet == null) {
                chunkSet = new LongOpenHashSet(4);
                BY_CHUNK.put(chunkKey, chunkSet);
            }
            chunkSet.add(posLong);
        } else {
            ALL_UNOPENED.remove(posLong);
            STATIC_UNOPENED.remove(posLong);
            PENDING_CLASSIFY.remove(posLong);
            LongOpenHashSet chunkSet = BY_CHUNK.get(chunkKey);
            if (chunkSet != null) {
                chunkSet.remove(posLong);
                if (chunkSet.isEmpty()) {
                    BY_CHUNK.remove(chunkKey);
                }
            }
        }
    }

    /**
     * Lazily classifies newly synced positions into BER-rendered vs static-block-rendered once the chunk's BlockEntities arrive.
     */
    public static void classifyPending(ClientLevel level, BlockEntityRenderDispatcher dispatcher) {
        if (PENDING_CLASSIFY.isEmpty()) {
            return;
        }

        LongIterator it = PENDING_CLASSIFY.iterator();
        while (it.hasNext()) {
            long posLong = it.nextLong();
            MUTABLE_POS.set(posLong);
            BlockEntity be = level.getBlockEntity(MUTABLE_POS);
            if (be != null) {
                if (dispatcher.getRenderer(be) == null) {
                    STATIC_UNOPENED.add(posLong);
                } else {
                    STATIC_UNOPENED.remove(posLong);
                }
                it.remove();
            }
        }
    }

    private static void removeChunkInternal(long chunkKey) {
        LongOpenHashSet oldSet = BY_CHUNK.remove(chunkKey);
        if (oldSet != null && !oldSet.isEmpty()) {
            LongIterator it = oldSet.iterator();
            while (it.hasNext()) {
                long posLong = it.nextLong();
                ALL_UNOPENED.remove(posLong);
                STATIC_UNOPENED.remove(posLong);
                PENDING_CLASSIFY.remove(posLong);
            }
        }
    }

    public static void clear() {
        ALL_UNOPENED.clear();
        STATIC_UNOPENED.clear();
        PENDING_CLASSIFY.clear();
        BY_CHUNK.clear();
    }

    @SubscribeEvent
    public static void onChunkUnload(final ChunkEvent.Unload event) {
        if (event.getLevel().isClientSide()) {
            ChunkPos pos = event.getChunk().getPos();
            removeChunkInternal(pos.toLong());
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(final LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) {
            clear();
        }
    }

    @SubscribeEvent
    public static void onLoggingOut(final ClientPlayerNetworkEvent.LoggingOut event) {
        clear();
    }
}
