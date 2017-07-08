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
    // this removes the stupid power of 2 rule that comes with minecraft.
    private static void drawTexturedQuadFit(double x, double y)
    {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder tessellate = tessellator.getBuffer();
        tessellate.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        tessellate.pos(x + 0, y + (double) 235, (double) 0).tex( 0,1).endVertex();
        tessellate.pos(x + (double) 229, y + (double) 235, (double) 0).tex( 1, 1).endVertex();
        tessellate.pos(x + (double) 229, y + 0, (double) 0).tex( 1,0).endVertex();
        tessellate.pos(x + 0, y + 0, (double) 0).tex( 0, 0).endVertex();
        Tessellator.getInstance().draw();
    }

    @Override
    public void drawScreen( int x, int y, float f ) {
        drawDefaultBackground();

        mc.renderEngine.bindTexture(new ResourceLocation(Reference.PREFIX_GUI + "bg.png"));
        drawTexturedQuadFit(width / 2 - 110, height / 2 - 118);

        if( hasTitle() ) {
            FontRenderer fr = this.mc.fontRenderer;
            fr.drawStringWithShadow(title(), width / 2 - 97, height / 2 - 105, 0xffff00);
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

    FontRenderer getFontRender() {
        return this.mc.fontRenderer;
    }
}
