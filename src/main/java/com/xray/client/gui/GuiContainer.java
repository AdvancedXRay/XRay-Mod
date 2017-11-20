package com.xray.client.gui;

import com.xray.common.reference.Reference;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

/**
 * Created by MiKeY on 08/07/17.
 */
public class GuiContainer extends GuiScreen {

    private boolean hasTitle = false;
    private String title = "";
    private boolean hasSide = false;
    private String sideTitle = "";
    private int backgroundWidth = 229;
    private int backgroundHeight = 235;

    GuiContainer( boolean hasSide ) {
        this.hasSide = hasSide;
    }

    @Override
    public void initGui() {

    }

    @Override
    protected void keyTyped( char par1, int par2 ) throws IOException
    {
        super.keyTyped( par1, par2 );

        // Close on esc, inventory key or keybind
        if( par2 == 1 )
            mc.player.closeScreen();
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    // this should be moved to some sort of utility package but fuck it :).
    private static void drawTexturedQuadFit(double x, double y, double width, double height)
    {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder tessellate = tessellator.getBuffer();
        tessellate.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        tessellate.pos(x + 0, y + height, (double) 0).tex( 0,1).endVertex();
        tessellate.pos(x + width, y + height, (double) 0).tex( 1, 1).endVertex();
        tessellate.pos(x + width, y + 0, (double) 0).tex( 1,0).endVertex();
        tessellate.pos(x + 0, y + 0, (double) 0).tex( 0, 0).endVertex();
        Tessellator.getInstance().draw();
    }

    @Override
    public void drawScreen( int x, int y, float f ) {
        drawDefaultBackground();

        FontRenderer fr = this.mc.fontRenderer;

        if( this.hasSide ) {
            mc.renderEngine.bindTexture(new ResourceLocation(Reference.PREFIX_GUI + "bg.png"));
            drawTexturedQuadFit(width / 2 + 60, height / 2 -(180/2), 150, 180);

            drawTexturedQuadFit(width / 2 - 150, height / 2 - 118, this.backgroundWidth, this.backgroundHeight);

            if( hasSideTitle() )
                fr.drawStringWithShadow(this.sideTitle, width / 2 + 80, height / 2 - 77, 0xffff00);

        }

        if( !this.hasSide ) {
            mc.renderEngine.bindTexture(new ResourceLocation(Reference.PREFIX_GUI + "bg.png"));
            drawTexturedQuadFit(width / 2 - (this.backgroundWidth / 2) + 1, height / 2 - (this.backgroundHeight / 2), this.backgroundWidth, this.backgroundHeight);
        }

        if( hasTitle() ) {
            if( this.hasSide )
                fr.drawStringWithShadow(title(), width / 2 - 138, height / 2 - 105, 0xffff00);
            else
                fr.drawStringWithShadow(title(), width / 2 - 100, height / 2 - 105, 0xffff00);
        }


        super.drawScreen(x, y, f);
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

    public boolean hasSideTitle() { return !this.sideTitle.isEmpty(); }
    public void setSideTitle( String title ) { this.sideTitle = title; }
    public boolean hasSide() { return this.hasSide; }

    public void setSize( int width, int height ) {
        this.backgroundWidth = width;
        this.backgroundHeight = height;
    }

    FontRenderer getFontRender() {
        return this.mc.fontRenderer;
    }
}
