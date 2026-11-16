package com.unopenedindication.config;

import com.unopenedindication.UnopenedIndicationMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Configuration holder with cached primitive values to avoid ForgeConfigSpec overhead on hot render paths.
 */
public final class ModConfig {
    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final ForgeConfigSpec COMMON_SPEC;

    private static final ForgeConfigSpec.BooleanValue ENABLED;
    private static final ForgeConfigSpec.IntValue MAX_RENDER_DISTANCE;
    private static final ForgeConfigSpec.BooleanValue RENDER_FOR_BER;
    private static final ForgeConfigSpec.BooleanValue RENDER_FOR_STATIC;

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> BLACKLISTED_BLOCK_ENTITIES;

    // Hot-path cached values
    public static volatile boolean enabled = true;
    public static volatile double maxRenderDistanceSq = 48.0 * 48.0;
    public static volatile boolean renderForBer = true;
    public static volatile boolean renderForStatic = true;
    private static volatile Set<BlockEntityType<?>> blacklistedTypes = Collections.emptySet();

    static {
        ForgeConfigSpec.Builder clientBuilder = new ForgeConfigSpec.Builder();
        clientBuilder.push("rendering");

        ENABLED = clientBuilder
                .comment("Enable enchantment glint rendering on unopened loot containers.")
                .translation("unopenedindication.config.enabled")
                .define("enabled", true);

        MAX_RENDER_DISTANCE = clientBuilder
                .comment("Maximum distance (in blocks) to render the enchantment glint effect.")
                .translation("unopenedindication.config.max_render_distance")
                .defineInRange("maxRenderDistance", 48, 8, 256);

        RENDER_FOR_BER = clientBuilder
                .comment("Render glint on containers with a BlockEntityRenderer (e.g., vanilla & modded chests, shulker boxes).")
                .translation("unopenedindication.config.render_for_ber")
                .define("renderForBlockEntityRenderers", true);

        RENDER_FOR_STATIC = clientBuilder
                .comment("Render glint on static block containers without a BlockEntityRenderer (e.g., barrels, modded crates).")
                .translation("unopenedindication.config.render_for_static")
                .define("renderForStaticBlocks", true);

        clientBuilder.pop();
        CLIENT_SPEC = clientBuilder.build();

        ForgeConfigSpec.Builder commonBuilder = new ForgeConfigSpec.Builder();
        commonBuilder.push("filtering");

        BLACKLISTED_BLOCK_ENTITIES = commonBuilder
                .comment("List of BlockEntity registry IDs (e.g., 'minecraft:barrel') to exclude from unopened indication.")
                .translation("unopenedindication.config.blacklisted_block_entities")
                .defineListAllowEmpty("blacklistedBlockEntities", Collections.emptyList(), obj -> obj instanceof String s && ResourceLocation.isValidResourceLocation(s));

        commonBuilder.pop();
        COMMON_SPEC = commonBuilder.build();
    }

    private ModConfig() {
    }

    public static void onConfigLoad(final ModConfigEvent.Loading event) {
        refreshCache(event.getConfig().getSpec());
    }

    public static void onConfigReload(final ModConfigEvent.Reloading event) {
        refreshCache(event.getConfig().getSpec());
    }

    private static void refreshCache(net.minecraftforge.fml.config.IConfigSpec<?> spec) {
        if (spec == CLIENT_SPEC) {
            enabled = ENABLED.get();
            int dist = MAX_RENDER_DISTANCE.get();
            maxRenderDistanceSq = (double) dist * (double) dist;
            renderForBer = RENDER_FOR_BER.get();
            renderForStatic = RENDER_FOR_STATIC.get();
        } else if (spec == COMMON_SPEC) {
            List<? extends String> rawList = BLACKLISTED_BLOCK_ENTITIES.get();
            if (rawList.isEmpty()) {
                blacklistedTypes = Collections.emptySet();
            } else {
                Set<BlockEntityType<?>> set = new HashSet<>(rawList.size());
                for (String id : rawList) {
                    ResourceLocation rl = ResourceLocation.tryParse(id);
                    if (rl != null) {
                        BlockEntityType<?> type = ForgeRegistries.BLOCK_ENTITY_TYPES.getValue(rl);
                        if (type != null) {
                            set.add(type);
                        }
                    }
                }
                blacklistedTypes = Collections.unmodifiableSet(set);
            }
        }
    }

    public static boolean isBlacklisted(BlockEntityType<?> type) {
        return !blacklistedTypes.isEmpty() && blacklistedTypes.contains(type);
    }
}
