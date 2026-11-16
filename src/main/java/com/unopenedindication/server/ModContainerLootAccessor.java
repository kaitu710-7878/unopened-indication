package com.unopenedindication.server;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.Set;

/**
 * Lock-free ClassValue cache that locates loot-table fields on modded BlockEntities
 * that do not extend RandomizableContainerBlockEntity, avoiding NBT serialization.
 */
public final class ModContainerLootAccessor {
    private static final Set<String> CANDIDATE_FIELD_NAMES = Set.of(
            "lootTable",
            "f_59605_",
            "lootTableId",
            "loot_table",
            "chestLootTable",
            "barrelLootTable",
            "lootTableLocation"
    );

    private static final MethodHandle NO_HANDLE = MethodHandles.constant(Object.class, null);

    private static final ClassValue<MethodHandle> FIELD_CACHE = new ClassValue<>() {
        @Override
        protected MethodHandle computeValue(Class<?> type) {
            return resolveLootFieldHandle(type);
        }
    };

    private ModContainerLootAccessor() {
    }

    public static boolean supportsAndHasLootTable(BlockEntity be) {
        MethodHandle handle = FIELD_CACHE.get(be.getClass());
        if (handle == NO_HANDLE) {
            return false;
        }
        try {
            Object value = handle.invoke(be);
            if (value instanceof Optional<?> opt) {
                return opt.isPresent();
            }
            return value != null;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean isModLootTrackable(BlockEntity be) {
        return FIELD_CACHE.get(be.getClass()) != NO_HANDLE;
    }

    private static MethodHandle resolveLootFieldHandle(Class<?> type) {
        if (!isContainerLike(type)) {
            return NO_HANDLE;
        }

        MethodHandles.Lookup lookup = MethodHandles.lookup();
        for (Class<?> cls = type; cls != null && cls != BlockEntity.class && cls != Object.class; cls = cls.getSuperclass()) {
            for (Field field : cls.getDeclaredFields()) {
                if (CANDIDATE_FIELD_NAMES.contains(field.getName()) && isLootFieldType(field.getType())) {
                    try {
                        field.setAccessible(true);
                        MethodHandle getter = lookup.unreflectGetter(field);
                        return getter.asType(MethodType.methodType(Object.class, Object.class));
                    } catch (Throwable ignored) {
                    }
                }
            }
        }
        return NO_HANDLE;
    }

    private static boolean isContainerLike(Class<?> type) {
        return Container.class.isAssignableFrom(type)
                || type.getName().toLowerCase().contains("chest")
                || type.getName().toLowerCase().contains("barrel")
                || type.getName().toLowerCase().contains("crate")
                || type.getName().toLowerCase().contains("loot");
    }

    private static boolean isLootFieldType(Class<?> fieldType) {
        return fieldType == ResourceLocation.class
                || fieldType == String.class
                || fieldType == Optional.class;
    }
}
