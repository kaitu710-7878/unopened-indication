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
 * Custom glint RenderTypes with boosted intensity (~+50%), wave-trough elimination, and LEQUAL support for static blocks.
 */
public final class GlintRenderTypes extends RenderStateShard {
    private static final Matrix4f MATRIX_CACHE = new Matrix4f();

    private static final TextureStateShard GLINT_TEXTURE = new TextureStateShard(
            ItemRenderer.ENCHANTED_GLINT_ENTITY, true, false
    );

    private static final TexturingStateShard BER_PRIMARY_TEXTURING = new TexturingStateShard(
            "unopenedindication_ber_primary_texturing",
            () -> setupTexturing(8.0F, 10.0F, 0.0F, 1.0F),
            RenderSystem::resetTextureMatrix
    );

    private static final TexturingStateShard BER_COUNTER_TEXTURING = new TexturingStateShard(
            "unopenedindication_ber_counter_texturing",
            () -> setupTexturing(8.0F, 100.0F, 0.5F, 1.15F),
            RenderSystem::resetTextureMatrix
    );

    private static final TexturingStateShard BER_AMBIENT_TEXTURING = new TexturingStateShard(
            "unopenedindication_ber_ambient_texturing",
            () -> setupTexturing(0.35F, -45.0F, 0.25F, 0.45F),
            RenderSystem::resetTextureMatrix
    );

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

    private static final TransparencyStateShard BRIGHT_GLINT_TRANSPARENCY = createScaledTransparency(
            "unopenedindication_bright_glint", 1.35F
    );

    private static final TransparencyStateShard AMBIENT_GLINT_TRANSPARENCY = createScaledTransparency(
            "unopenedindication_ambient_glint", 0.90F
    );

    public static final RenderType BER_GLINT_PRIMARY = createBerGlint(
            "unopenedindication_ber_glint_primary", BRIGHT_GLINT_TRANSPARENCY, BER_PRIMARY_TEXTURING
    );

    public static final RenderType BER_GLINT_COUNTER = createBerGlint(
            "unopenedindication_ber_glint_counter", BRIGHT_GLINT_TRANSPARENCY, BER_COUNTER_TEXTURING
    );

    public static final RenderType BER_GLINT_AMBIENT = createBerGlint(
            "unopenedindication_ber_glint_ambient", AMBIENT_GLINT_TRANSPARENCY, BER_AMBIENT_TEXTURING
    );

    public static final RenderType STATIC_GLINT_PRIMARY = createStaticGlint(
            "unopenedindication_static_glint_primary", BRIGHT_GLINT_TRANSPARENCY, STATIC_PRIMARY_TEXTURING
    );

    public static final RenderType STATIC_GLINT_COUNTER = createStaticGlint(
            "unopenedindication_static_glint_counter", BRIGHT_GLINT_TRANSPARENCY, STATIC_COUNTER_TEXTURING
    );

    public static final RenderType STATIC_GLINT_AMBIENT = createStaticGlint(
            "unopenedindication_static_glint_ambient", AMBIENT_GLINT_TRANSPARENCY, STATIC_AMBIENT_TEXTURING
    );

    private GlintRenderTypes(String name, Runnable setupState, Runnable clearState) {
        super(name, setupState, clearState);
    }

    private static TransparencyStateShard createScaledTransparency(String name, float multiplier) {
        return new TransparencyStateShard(
                name,
                () -> {
                    GLINT_TRANSPARENCY.setupRenderState();
                    RenderSystem.setShaderColor(multiplier, multiplier, multiplier, 1.0F);
                },
                () -> {
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    GLINT_TRANSPARENCY.clearRenderState();
                }
        );
    }

    private static RenderType createBerGlint(String name, TransparencyStateShard transparency, TexturingStateShard texturing) {
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
                        .setDepthTestState(EQUAL_DEPTH_TEST)
                        .setTransparencyState(transparency)
                        .setTexturingState(texturing)
                        .createCompositeState(false)
        );
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
