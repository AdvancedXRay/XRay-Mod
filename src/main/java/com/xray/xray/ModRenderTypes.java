package com.xray.xray;

import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;

import java.util.OptionalDouble;

public class ModRenderTypes extends RenderType {
    public ModRenderTypes(String nameIn, VertexFormat formatIn, int drawModeIn, int bufferSizeIn, boolean useDelegateIn, boolean needsSortingIn, Runnable setupTaskIn, Runnable clearTaskIn) {
        super(nameIn, formatIn, drawModeIn, bufferSizeIn, useDelegateIn, needsSortingIn, setupTaskIn, clearTaskIn);
    }

    public static final RenderType LINES = makeType(
            "lines",
            DefaultVertexFormats.POSITION_COLOR, 1, 256,
            RenderType.State.getBuilder()
                    .line(new LineState(OptionalDouble.empty()))
                    .layer(PROJECTION_LAYERING)
                    .transparency(TRANSLUCENT_TRANSPARENCY)
                    .writeMask(COLOR_WRITE)
                    .cull(CULL_DISABLED)
                    .depthTest(DEPTH_EQUAL)
                    .build(false));
}
