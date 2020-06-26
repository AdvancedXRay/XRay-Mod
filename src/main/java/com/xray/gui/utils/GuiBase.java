package com.xray.gui.utils;

import com.mojang.blaze3d.matrix.MatrixStack;
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

import java.util.List;

public abstract class GuiBase extends Screen {

    public static final ResourceLocation BG_NORMAL = new ResourceLocation(XRay.PREFIX_GUI + "bg.png");
    public static final ResourceLocation BG_LARGE = new ResourceLocation(XRay.PREFIX_GUI + "bg-help.png");

    private boolean hasSide;
    private String sideTitle = "";
    private int backgroundWidth = 229;
    private int backgroundHeight = 235;

    public abstract void renderExtra(MatrixStack stack, int x, int y, float partialTicks);

    public GuiBase(boolean hasSide ) {
        super(new StringTextComponent(""));
        this.hasSide = hasSide;
    }

    @Override    //charTyped
    public boolean func_231042_a_(char keyTyped, int __unknown) {
        super.func_231042_a_(keyTyped, __unknown);

        if( keyTyped == 1 && getMinecraft().player != null )
            getMinecraft().player.closeScreen();

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

    @Override  // @mcp: func_230430_a_ = render
    public void func_230430_a_(MatrixStack stack, int x, int y, float partialTicks) {
        func_230446_a_(stack); // @mcp: func_230446_a_ = renderBackground();
        RenderSystem.pushMatrix();

        // @mcp: field_230708_k_ = width
        // @mcp: field_230709_l_ = height
        int width = field_230708_k_;
        int height = field_230709_l_;
        getMinecraft().getTextureManager().bindTexture(getBackground());
        if( this.hasSide ) {
            drawTexturedQuadFit((double) width / 2 + 60, (float) height / 2 -((float) 180/2), 150, 180, 0xffffff);
            drawTexturedQuadFit((float) width / 2 - 150, (float) height / 2 - 118, this.backgroundWidth, this.backgroundHeight, 0xffffff);

            // @mcp: func_238405_a_ = drawStringWithShadow
            if( hasSideTitle() )
                getFontRender().func_238405_a_(stack, this.sideTitle, (float) width / 2 + 80, (float) height / 2 - 77, 0xffff00);
        }

        if( !this.hasSide )
            drawTexturedQuadFit((float) width / 2 - ((float) this.backgroundWidth / 2) + 1, (float) height / 2 - ((float) this.backgroundHeight / 2), this.backgroundWidth, this.backgroundHeight, 0xffffff);

        RenderSystem.enableTexture();
        if( hasTitle() ) {
            if( this.hasSide )
                getFontRender().func_238405_a_(stack, title(), (float) width / 2 - 138, (float) height / 2 - 105, 0xffff00);
            else
                getFontRender().func_238405_a_(stack, title(), (float) width / 2 - ((float) this.backgroundWidth / 2 ) + 14, (float) height / 2 - ((float) this.backgroundHeight / 2) + 13, 0xffff00);
        }

        RenderSystem.popMatrix();

        renderExtra(stack, x, y, partialTicks);

        List<Widget> buttons = this.field_230710_m_; // this.buttons
        for (Widget button : buttons) {
            button.func_230430_a_(stack, x, y, partialTicks); // @mcp: func_230430_a_ = render
        }

        for(Widget button : buttons) {
            if (button instanceof SupportButton && button.func_230449_g_()) // @mcp: func_230449_g_ = isHovered
                func_238654_b_(stack, ((SupportButton) button).getSupport(), x, y); // @mcp: func_230457_a_ = renderTooltip
        }

        super.func_230430_a_(stack, x, y, partialTicks);
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

    // temp helpers
    protected <T extends Widget> T addButton(T button) {
        return this.func_230480_a_(button); // @mcp: func_230480_a_ = Widget::addButton
    }

    public int getWidth() { return this.field_230708_k_; }
    public int getHeight() { return this.field_230709_l_; }

    public void onClose() {
        this.func_231175_as__(); // @mcp: Screen::onClose
    }
}
