package com.unopenedindication.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;

/**
 * Reusable MultiBufferSource wrappers supporting both NEW_ENTITY and BLOCK vertex formats for modded BERs.
 */
public final class GlintMultiBufferSource implements MultiBufferSource {
    private static final GlintMultiBufferSource COMBINED_INSTANCE = new GlintMultiBufferSource(null);
    private static final GlintMultiBufferSource AMBIENT_ONLY_INSTANCE = new GlintMultiBufferSource(GlintRenderTypes.BER_GLINT_AMBIENT);
    private static final GlintMultiBufferSource COUNTER_ONLY_INSTANCE = new GlintMultiBufferSource(GlintRenderTypes.BER_GLINT_COUNTER);
    private static final VertexConsumer NOOP_CONSUMER = new NoopVertexConsumer();

    private final RenderType glintOnlyType;
    private MultiBufferSource delegate;

    private GlintMultiBufferSource(RenderType glintOnlyType) {
        this.glintOnlyType = glintOnlyType;
    }

    public static MultiBufferSource wrap(MultiBufferSource delegate) {
        COMBINED_INSTANCE.delegate = delegate;
        return COMBINED_INSTANCE;
    }

    public static MultiBufferSource wrapGlintOnly(MultiBufferSource delegate, RenderType glintType) {
        if (glintType == GlintRenderTypes.BER_GLINT_AMBIENT) {
            AMBIENT_ONLY_INSTANCE.delegate = delegate;
            return AMBIENT_ONLY_INSTANCE;
        }
        COUNTER_ONLY_INSTANCE.delegate = delegate;
        return COUNTER_ONLY_INSTANCE;
    }

    public MultiBufferSource unwrap() {
        return delegate;
    }

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        if (isGlintRenderType(renderType)) {
            return glintOnlyType != null ? NOOP_CONSUMER : delegate.getBuffer(renderType);
        }

        VertexFormat format = renderType.format();
        if (format == DefaultVertexFormat.NEW_ENTITY) {
            if (glintOnlyType != null) {
                return delegate.getBuffer(glintOnlyType);
            }
            VertexConsumer base = delegate.getBuffer(renderType);
            VertexConsumer glint = delegate.getBuffer(RenderType.entityGlintDirect());
            return VertexMultiConsumer.create(glint, base);
        }

        if (format == DefaultVertexFormat.BLOCK) {
            RenderType targetGlint = glintOnlyType != null ? glintOnlyType : RenderType.entityGlintDirect();
            VertexConsumer glint = new BlockToEntityGlintAdapter(delegate.getBuffer(targetGlint));
            if (glintOnlyType != null) {
                return glint;
            }
            return VertexMultiConsumer.create(glint, delegate.getBuffer(renderType));
        }

        return glintOnlyType != null ? NOOP_CONSUMER : delegate.getBuffer(renderType);
    }

    private static boolean isGlintRenderType(RenderType renderType) {
        return renderType == RenderType.entityGlint()
                || renderType == RenderType.entityGlintDirect()
                || renderType == RenderType.glint()
                || renderType == RenderType.glintDirect()
                || renderType == GlintRenderTypes.BER_GLINT_COUNTER
                || renderType == GlintRenderTypes.BER_GLINT_AMBIENT;
    }

    /**
     * Adapts BLOCK vertex format calls (which omit overlayCoords) into NEW_ENTITY format calls.
     */
    private static final class BlockToEntityGlintAdapter implements VertexConsumer {
        private final VertexConsumer delegate;

        private BlockToEntityGlintAdapter(VertexConsumer delegate) {
            this.delegate = delegate;
        }

        @Override
        public VertexConsumer vertex(double x, double y, double z) {
            delegate.vertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer color(int r, int g, int b, int a) {
            delegate.color(r, g, b, a);
            return this;
        }

        @Override
        public VertexConsumer uv(float u, float v) {
            delegate.uv(u, v);
            delegate.overlayCoords(OverlayTexture.NO_OVERLAY);
            return this;
        }

        @Override
        public VertexConsumer overlayCoords(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer uv2(int u, int v) {
            delegate.uv2(u, v);
            return this;
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            delegate.normal(x, y, z);
            return this;
        }

        @Override
        public void endVertex() {
            delegate.endVertex();
        }

        @Override
        public void defaultColor(int r, int g, int b, int a) {
            delegate.defaultColor(r, g, b, a);
        }

        @Override
        public void unsetDefaultColor() {
            delegate.unsetDefaultColor();
        }
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
