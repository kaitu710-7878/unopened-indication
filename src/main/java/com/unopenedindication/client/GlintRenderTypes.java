package com.unopenedindication.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import org.joml.Matrix4f;

/**
 * Custom glint RenderTypes that eliminate dark wave troughs and support LEQUAL depth testing for static blocks.
 */
public final class GlintRenderTypes extends RenderStateShard {
    private static final Matrix4f MATRIX_CACHE = new Matrix4f();

    private static final TextureStateShard GLINT_TEXTURE = new TextureStateShard(
            ItemRenderer.ENCHANTED_GLINT_ENTITY, true, false
    );

    // Counter-rotating / phase-shifted texturing so wave troughs overlap with peaks
    private static final TexturingStateShard BER_COUNTER_TEXTURING = new TexturingStateShard(
            "unopenedindication_ber_counter_texturing",
            () -> setupTexturing(8.0F, 90.0F, 0.5F, 1.15F),
            RenderSystem::resetTextureMatrix
    );

    // Wide-scale slow texturing to maintain a steady base glow (>= top-quarter brightness even in troughs)
    private static final TexturingStateShard BER_AMBIENT_TEXTURING = new TexturingStateShard(
            "unopenedindication_ber_ambient_texturing",
            () -> setupTexturing(0.35F, -45.0F, 0.25F, 0.45F),
            RenderSystem::resetTextureMatrix
    );

    // Static blocks use block-atlas UVs (smaller UV span), so scale up the glint matrix accordingly
    private static final TexturingStateShard STATIC_PRIMARY_TEXTURING = new TexturingStateShard(
            "unopenedindication_static_primary_texturing",
            () -> setupTexturing(24.0F, 10.0F, 0.0F, 1.0F),
            RenderSystem::resetTextureMatrix
    );

    private static final TexturingStateShard STATIC_COUNTER_TEXTURING = new TexturingStateShard(
            "unopenedindication_static_counter_texturing",
            () -> setupTexturing(24.0F, 100.0F, 0.5F, 1.15F),
            RenderSystem::resetTextureMatrix
    );

    private static final TexturingStateShard STATIC_AMBIENT_TEXTURING = new TexturingStateShard(
            "unopenedindication_static_ambient_texturing",
            () -> setupTexturing(0.6F, -45.0F, 0.25F, 0.45F),
            RenderSystem::resetTextureMatrix
    );

    private static final TransparencyStateShard AMBIENT_GLINT_TRANSPARENCY = new TransparencyStateShard(
            "unopenedindication_ambient_glint",
            () -> {
                GLINT_TRANSPARENCY.setupRenderState();
                RenderSystem.setShaderColor(0.55F, 0.55F, 0.55F, 1.0F);
            },
            () -> {
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                GLINT_TRANSPARENCY.clearRenderState();
            }
    );

    public static final RenderType BER_GLINT_COUNTER = RenderType.create(
            "unopenedindication_ber_glint_counter",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS,
            256,
            false,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_ENTITY_GLINT_DIRECT_SHADER)
                    .setTextureState(GLINT_TEXTURE)
                    .setWriteMaskState(COLOR_WRITE)
                    .setCullState(NO_CULL)
                    .setDepthTestState(EQUAL_DEPTH_TEST)
                    .setTransparencyState(GLINT_TRANSPARENCY)
                    .setTexturingState(BER_COUNTER_TEXTURING)
                    .createCompositeState(false)
    );

    public static final RenderType BER_GLINT_AMBIENT = RenderType.create(
            "unopenedindication_ber_glint_ambient",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS,
            256,
            false,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_ENTITY_GLINT_DIRECT_SHADER)
                    .setTextureState(GLINT_TEXTURE)
                    .setWriteMaskState(COLOR_WRITE)
                    .setCullState(NO_CULL)
                    .setDepthTestState(EQUAL_DEPTH_TEST)
                    .setTransparencyState(AMBIENT_GLINT_TRANSPARENCY)
                    .setTexturingState(BER_AMBIENT_TEXTURING)
                    .createCompositeState(false)
    );

    public static final RenderType STATIC_GLINT_PRIMARY = createStaticGlint(
            "unopenedindication_static_glint_primary", GLINT_TRANSPARENCY, STATIC_PRIMARY_TEXTURING
    );

    public static final RenderType STATIC_GLINT_COUNTER = createStaticGlint(
            "unopenedindication_static_glint_counter", GLINT_TRANSPARENCY, STATIC_COUNTER_TEXTURING
    );

    public static final RenderType STATIC_GLINT_AMBIENT = createStaticGlint(
            "unopenedindication_static_glint_ambient", AMBIENT_GLINT_TRANSPARENCY, STATIC_AMBIENT_TEXTURING
    );

    private GlintRenderTypes(String name, Runnable setupState, Runnable clearState) {
        super(name, setupState, clearState);
    }

    private static RenderType createStaticGlint(String name, TransparencyStateShard transparency, TexturingStateShard texturing) {
        return RenderType.create(
                name,
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS,
                256,
                false,
                false,
                RenderType.CompositeState.builder()
                        .setShaderState(RENDERTYPE_ENTITY_GLINT_DIRECT_SHADER)
                        .setTextureState(GLINT_TEXTURE)
                        .setWriteMaskState(COLOR_WRITE)
                        .setCullState(NO_CULL)
                        .setDepthTestState(LEQUAL_DEPTH_TEST)
                        .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                        .setTransparencyState(transparency)
                        .setTexturingState(texturing)
                        .createCompositeState(false)
        );
    }

    private static void setupTexturing(float scale, float angleDeg, float phaseOffset, float speedMultiplier) {
        long millis = (long) (Util.getMillis() * speedMultiplier);
        float u = ((millis % 110000L) / 110000.0F) + phaseOffset;
        float v = ((millis % 30000L) / 30000.0F) + phaseOffset;
        MATRIX_CACHE.identity()
                .translate(-u, v, 0.0F)
                .rotateZ((float) Math.toRadians(angleDeg))
                .scale(scale);
        RenderSystem.setTextureMatrix(MATRIX_CACHE);
    }
}
