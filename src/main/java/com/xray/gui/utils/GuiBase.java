package com.xray.gui.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import com.xray.XRay;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.opengl.GL11;

public abstract class GuiBase extends Screen {

    public static final ResourceLocation BG_NORMAL = new ResourceLocation(XRay.PREFIX_GUI + "bg.png");
    public static final ResourceLocation BG_LARGE = new ResourceLocation(XRay.PREFIX_GUI + "bg-help.png");

    private boolean hasSide;
    private String sideTitle = "";
    private int backgroundWidth = 229;
    private int backgroundHeight = 235;

    public abstract void renderExtra(int x, int y, float partialTicks);

    public GuiBase(boolean hasSide ) {
        super(new StringTextComponent(""));
        this.hasSide = hasSide;
    }

    @Override
    public boolean charTyped(char keyTyped, int __unknown) {
        super.charTyped(keyTyped, __unknown);

        if( keyTyped == 1 && getMinecraft().player != null )
            getMinecraft().player.closeScreen();

        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // this should be moved to some sort of utility package but fuck it :).
    public static void drawTexturedQuadFit(double x, double y, double width, double height, int[] color, float alpha)
    {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder tessellate = tessellator.getBuffer();

        RenderSystem.pushMatrix();
        tessellate.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        if ( color != null )
            RenderSystem.color4f((float) color[0] / 255, (float) color[1] / 255, (float) color[2] / 255, alpha / 255);

        tessellate.pos(x + 0, y + height, (double) 0).tex( 0,1).endVertex();
        tessellate.pos(x + width, y + height, (double) 0).tex( 1, 1).endVertex();
        tessellate.pos(x + width, y + 0, (double) 0).tex( 1,0).endVertex();
        tessellate.pos(x + 0, y + 0, (double) 0).tex( 0, 0).endVertex();
        tessellator.draw();

        RenderSystem.popMatrix();
    }

    public static void drawTexturedQuadFit(double x, double y, double width, double height, int[] color) {
        drawTexturedQuadFit(x, y, width, height, color, 255f);
    }

    public static void drawTexturedQuadFit(double x, double y, double width, double height, int color) {
        drawTexturedQuadFit(x, y, width, height, new int[]{color >> 16 & 0xff, color >> 8 & 0xff,color & 0xff }, 255f);
    }

    @Override
    public void render(int x, int y, float partialTicks) {
        renderBackground();
        RenderSystem.pushMatrix();

        getMinecraft().getTextureManager().bindTexture(getBackground());
        if( this.hasSide ) {
            drawTexturedQuadFit((double) width / 2 + 60, (float) height / 2 -((float) 180/2), 150, 180, 0xffffff);
            drawTexturedQuadFit((float) width / 2 - 150, (float) height / 2 - 118, this.backgroundWidth, this.backgroundHeight, 0xffffff);

            if( hasSideTitle() )
                getFontRender().drawStringWithShadow(this.sideTitle, (float) width / 2 + 80, (float) height / 2 - 77, 0xffff00);
        }

        if( !this.hasSide )
            drawTexturedQuadFit((float) width / 2 - ((float) this.backgroundWidth / 2) + 1, (float) height / 2 - ((float) this.backgroundHeight / 2), this.backgroundWidth, this.backgroundHeight, 0xffffff);

        RenderSystem.enableTexture();
        if( hasTitle() ) {
            if( this.hasSide )
                getFontRender().drawStringWithShadow(title(), (float) width / 2 - 138, (float) height / 2 - 105, 0xffff00);
            else
                getFontRender().drawStringWithShadow(title(), (float) width / 2 - ((float) this.backgroundWidth / 2 ) + 14, (float) height / 2 - ((float) this.backgroundHeight / 2) + 13, 0xffff00);
        }

        RenderSystem.popMatrix();

        renderExtra(x, y, partialTicks);

        for (Widget button : this.buttons) {
            button.render(x, y, partialTicks);
        }

        for(Widget button : this.buttons) {
            if (button instanceof SupportButton && button.isHovered())
                renderTooltip(((SupportButton) button).getSupport(), x, y);
        }
    }

    public ResourceLocation getBackground() {
        return BG_NORMAL;
    }

    public boolean hasTitle() {
        return false;
    }

    public String title() {
        return "";
    }

    private boolean hasSideTitle() { return !this.sideTitle.isEmpty(); }
    protected void setSideTitle(String title) { this.sideTitle = title; }

    public void setSize( int width, int height ) {
        this.backgroundWidth = width;
        this.backgroundHeight = height;
    }

    public FontRenderer getFontRender() {
        return getMinecraft().fontRenderer;
    }
}
