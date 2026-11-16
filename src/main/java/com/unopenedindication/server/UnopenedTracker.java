package com.unopenedindication.server;

import com.unopenedindication.UnopenedIndicationMod;
import com.unopenedindication.config.ModConfig;
import com.unopenedindication.mixin.LootTableContainerAccessor;
import com.unopenedindication.network.NetworkHandler;
import com.unopenedindication.network.SyncChunkUnopenedPacket;
import com.unopenedindication.network.UpdateUnopenedPosPacket;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.level.ChunkWatchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.Map;

/**
 * Server-side event-driven tracker for unopened loot containers.
 * Relies directly on vanilla/modded lootTable fields without adding extra NBT or ticking overhead.
 */
@Mod.EventBusSubscriber(modid = UnopenedIndicationMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class UnopenedTracker {

    private UnopenedTracker() {
    }

    @SubscribeEvent
    public static void onChunkWatch(final ChunkWatchEvent.Watch event) {
        LevelChunk chunk = event.getChunk();
        Map<BlockPos, BlockEntity> blockEntities = chunk.getBlockEntities();
        if (blockEntities.isEmpty()) {
            return;
        }

        LongArrayList unopenedPositions = null;
        for (BlockEntity be : blockEntities.values()) {
            if (isUnopenedLootContainer(be)) {
                if (unopenedPositions == null) {
                    unopenedPositions = new LongArrayList(4);
                }
                unopenedPositions.add(be.getBlockPos().asLong());
            }
        }

        if (unopenedPositions != null && !unopenedPositions.isEmpty()) {
            ChunkPos chunkPos = chunk.getPos();
            NetworkHandler.CHANNEL.send(
                    PacketDistributor.PLAYER.with(event::getPlayer),
                    new SyncChunkUnopenedPacket(chunkPos.x, chunkPos.z, unopenedPositions.toLongArray())
            );
        }
    }

    public static void onContainerStateChanged(ServerLevel level, RandomizableContainerBlockEntity be, boolean unopened) {
        if (unopened && ModConfig.isBlacklisted(be.getType())) {
            return;
        }
        BlockPos pos = be.getBlockPos();
        if (!level.isLoaded(pos)) {
            return;
        }
        LevelChunk chunk = level.getChunkAt(pos);
        NetworkHandler.CHANNEL.send(
                PacketDistributor.TRACKING_CHUNK.with(() -> chunk),
                new UpdateUnopenedPosPacket(pos.asLong(), unopened)
        );
    }

    public static boolean isUnopenedLootContainer(BlockEntity be) {
        if (be instanceof RandomizableContainerBlockEntity container) {
            if (ModConfig.isBlacklisted(be.getType())) {
                return false;
            }
            return ((LootTableContainerAccessor) container).unopenedindication$getLootTable() != null;
        }
        return false;
    }
}
