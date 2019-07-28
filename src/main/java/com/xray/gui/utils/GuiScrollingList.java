package com.xray.gui.utils;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.IRenderable;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class GuiScrollingList implements IGuiEventListener, IRenderable {
    private int x;
    private int y;
    private int width;
    private int height;

    private int slotHeight;
    private float scrollAmount = 0;

    public GuiScrollingList(int x, int y, int width, int height, int slotHeight) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.slotHeight = slotHeight;
    }

    @Override
    public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
        this.scrollAmount += 10f;
        System.out.println("Scrolling: " + this.scrollAmount);
        return false;
    }

    @Override
    public boolean mouseDragged(double p_mouseDragged_1_, double p_mouseDragged_3_, int p_mouseDragged_5_, double p_mouseDragged_6_, double p_mouseDragged_8_) {
//        System.out.
        return false;
    }

    @Override
    public boolean mouseScrolled(double p_mouseScrolled_1_, double p_mouseScrolled_3_, double p_mouseScrolled_5_) {
        return false;
    }

    public void render(int mouseX, int mouseY, float partialTicks) {
        boolean isHovering = mouseX >= this.x && mouseX <= this.x + this.width &&
                             mouseY >= this.x && mouseY <= this.x + this.height;

        if( this.scrollAmount > ((20 * this.slotHeight) - this.height) )
            this.scrollAmount = ((20 * this.slotHeight) - this.height);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.getBuffer();

        // Remove anything outside of this grid
        double scale = Minecraft.getInstance().mainWindow.getGuiScaleFactor();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor((int)(x * scale), (int)(Minecraft.getInstance().mainWindow.getHeight() - ((y + height) * scale)),
                (int)(width * scale), (int)(height * scale));

        GlStateManager.disableTexture();

        // Draw a border
        GlStateManager.color4f(.5f, .5f, .5f, 1f);
        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        builder.pos(x, y + height, 0).tex( 0,1).endVertex();
        builder.pos(x + width, y + height, 0).tex( 1, 1).endVertex();
        builder.pos(x + width, y, 0).tex( 1,0).endVertex();
        builder.pos(x, y, 0).tex( 0, 0).endVertex();
        tessellator.draw();

        // Draw our background
        GlStateManager.color4f(0f, 0f, 0f, 1f);
        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        builder.pos(x + 1, y + height - 1, 0).tex( 0,1).endVertex();
        builder.pos(x + width - 1, y + height - 1, 0).tex( 1, 1).endVertex();
        builder.pos(x + width - 1, y + 1, 0).tex( 1,0).endVertex();
        builder.pos(x + 1, y + 1, 0).tex( 0, 0).endVertex();
        tessellator.draw();

        GlStateManager.enableTexture();

        for( int i = 0; i < 20; i ++  ) {
            int top = (this.y - (int)this.scrollAmount) + i * this.slotHeight;

            this.renderSlot(i, top, this.x, tessellator);
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    public void renderSlot(int index, int top, int left, Tessellator tessellator) {
        Minecraft.getInstance().fontRenderer.drawStringWithShadow("Hi " + index, left, top, Color.WHITE.getRGB());
    }
}
