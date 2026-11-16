package com.unopenedindication.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

/**
 * Reusable MultiBufferSource wrapper that duplicates entity-formatted geometry into entityGlintDirect()
 * without allocating a new wrapper instance per BlockEntity.
 */
public final class GlintMultiBufferSource implements MultiBufferSource {
    private static final GlintMultiBufferSource INSTANCE = new GlintMultiBufferSource();
    private MultiBufferSource delegate;

    private GlintMultiBufferSource() {
    }

    public static MultiBufferSource wrap(MultiBufferSource delegate) {
        INSTANCE.delegate = delegate;
        return INSTANCE;
    }

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        VertexConsumer base = delegate.getBuffer(renderType);
        if (renderType.format() == DefaultVertexFormat.NEW_ENTITY && !isGlintRenderType(renderType)) {
            VertexConsumer glint = delegate.getBuffer(RenderType.entityGlintDirect());
            return VertexMultiConsumer.create(glint, base);
        }
        return base;
    }

    private static boolean isGlintRenderType(RenderType renderType) {
        return renderType == RenderType.entityGlint()
                || renderType == RenderType.entityGlintDirect()
                || renderType == RenderType.glint()
                || renderType == RenderType.glintDirect();
    }
}
