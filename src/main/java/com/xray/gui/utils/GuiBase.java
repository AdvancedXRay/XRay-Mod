package com.xray.gui.utils;

import com.xray.reference.Reference;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.IOException;

public class GuiBase extends GuiScreen {

    private boolean hasSide;
    private String sideTitle = "";
    private int backgroundWidth = 229;
    private int backgroundHeight = 235;

    public GuiBase(boolean hasSide ) {
        this.hasSide = hasSide;
    }

    @Override
    protected void keyTyped( char par1, int par2 ) throws IOException
    {
        super.keyTyped( par1, par2 );

        if( par2 == 1 )
            mc.player.closeScreen();
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    // this should be moved to some sort of utility package but fuck it :).
    public static void drawTexturedQuadFit(double x, double y, double width, double height, int[] color, float alpha)
    {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder tessellate = tessellator.getBuffer();
        tessellate.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        if ( color != null )
            GlStateManager.color((float) color[0] / 255, (float) color[1] / 255, (float) color[2] / 255, alpha / 255);

        tessellate.pos(x + 0, y + height, (double) 0).tex( 0,1).endVertex();
        tessellate.pos(x + width, y + height, (double) 0).tex( 1, 1).endVertex();
        tessellate.pos(x + width, y + 0, (double) 0).tex( 1,0).endVertex();
        tessellate.pos(x + 0, y + 0, (double) 0).tex( 0, 0).endVertex();
        tessellator.draw();
    }

    public static void drawTexturedQuadFit(double x, double y, double width, double height, int[] color) {
        drawTexturedQuadFit(x, y, width, height, color, 255f);
    }

    public static void drawTexturedQuadFit(double x, double y, double width, double height, @Nullable Color color) {
        drawTexturedQuadFit(x, y, width, height, color == null ? null : new int[]{color.getRed(), color.getGreen(), color.getBlue()}, 255f);
    }


    @Override
    public void drawScreen( int x, int y, float f ) {
        drawDefaultBackground();

        FontRenderer fr = this.mc.fontRenderer;

        if( colorBackground() == null )
            mc.renderEngine.bindTexture(new ResourceLocation(Reference.PREFIX_GUI + "bg.png"));
        else
            GlStateManager.disableTexture2D();

        if( this.hasSide ) {
            drawTexturedQuadFit((double) width / 2 + 60, (float) height / 2 -((float) 180/2), 150, 180, colorBackground());
            drawTexturedQuadFit((float) width / 2 - 150, (float) height / 2 - 118, this.backgroundWidth, this.backgroundHeight, colorBackground());

            if( hasSideTitle() )
                fr.drawStringWithShadow(this.sideTitle, (float) width / 2 + 80, (float) height / 2 - 77, 0xffff00);

        }

        if( !this.hasSide )
            drawTexturedQuadFit((float) width / 2 - ((float) this.backgroundWidth / 2) + 1, (float) height / 2 - ((float) this.backgroundHeight / 2), this.backgroundWidth, this.backgroundHeight, colorBackground());

        if( colorBackground() != null )
            GlStateManager.enableTexture2D();

        if( hasTitle() ) {
            if( this.hasSide )
                fr.drawStringWithShadow(title(), (float) width / 2 - 138, (float) height / 2 - 105, 0xffff00);
            else
                fr.drawStringWithShadow(title(), (float) width / 2 - ((float) this.backgroundWidth / 2 ) + 14, (float) height / 2 - ((float) this.backgroundHeight / 2) + 13, 0xffff00);
        }

        super.drawScreen(x, y, f);
    }

    public Color colorBackground() {
        return null;
    }

    @Override
    public void mouseClicked( int x, int y, int mouse ) throws IOException
    {
        super.mouseClicked( x, y, mouse );
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
        return this.mc.fontRenderer;
    }
}
