package com.unopenedindication.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

/**
 * Reusable MultiBufferSource wrappers that apply enchantment glint without per-entity wrapper allocations.
 */
public final class GlintMultiBufferSource implements MultiBufferSource {
    private static final GlintMultiBufferSource COMBINED_INSTANCE = new GlintMultiBufferSource(false);
    private static final GlintMultiBufferSource GLINT_ONLY_INSTANCE = new GlintMultiBufferSource(true);
    private static final VertexConsumer NOOP_CONSUMER = new NoopVertexConsumer();

    private final boolean glintOnly;
    private MultiBufferSource delegate;

    private GlintMultiBufferSource(boolean glintOnly) {
        this.glintOnly = glintOnly;
    }

    public static MultiBufferSource wrap(MultiBufferSource delegate) {
        COMBINED_INSTANCE.delegate = delegate;
        return COMBINED_INSTANCE;
    }

    public static MultiBufferSource wrapGlintOnly(MultiBufferSource delegate) {
        GLINT_ONLY_INSTANCE.delegate = delegate;
        return GLINT_ONLY_INSTANCE;
    }

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        if (renderType.format() == DefaultVertexFormat.NEW_ENTITY && !isGlintRenderType(renderType)) {
            VertexConsumer glint = delegate.getBuffer(RenderType.entityGlintDirect());
            if (glintOnly) {
                return glint;
            }
            VertexConsumer base = delegate.getBuffer(renderType);
            return VertexMultiConsumer.create(glint, base);
        }
        return glintOnly ? NOOP_CONSUMER : delegate.getBuffer(renderType);
    }

    private static boolean isGlintRenderType(RenderType renderType) {
        return renderType == RenderType.entityGlint()
                || renderType == RenderType.entityGlintDirect()
                || renderType == RenderType.glint()
                || renderType == RenderType.glintDirect();
    }

    private static final class NoopVertexConsumer implements VertexConsumer {
        @Override
        public VertexConsumer vertex(double x, double y, double z) {
            return this;
        }

        @Override
        public VertexConsumer color(int r, int g, int b, int a) {
            return this;
        }

        @Override
        public VertexConsumer uv(float u, float v) {
            return this;
        }

        @Override
        public VertexConsumer overlayCoords(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer uv2(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            return this;
        }

        @Override
        public void endVertex() {
        }

        @Override
        public void defaultColor(int r, int g, int b, int a) {
        }

        @Override
        public void unsetDefaultColor() {
        }
    }
}
