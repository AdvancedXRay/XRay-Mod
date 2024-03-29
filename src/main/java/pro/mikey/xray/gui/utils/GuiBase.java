package pro.mikey.xray.gui.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import pro.mikey.xray.XRay;

public abstract class GuiBase extends Screen {
    public static final ResourceLocation BG_NORMAL = new ResourceLocation(XRay.PREFIX_GUI + "bg.png");
    public static final ResourceLocation BG_LARGE = new ResourceLocation(XRay.PREFIX_GUI + "bg-help.png");

    private boolean hasSide;
    private String sideTitle = "";
    private int backgroundWidth = 229;
    private int backgroundHeight = 235;

    public abstract void renderExtra(GuiGraphics guiGraphics, int x, int y, float partialTicks);

    public GuiBase(boolean hasSide ) {
        super(Component.literal(""));
        this.hasSide = hasSide;

    }

    @Override
    public boolean charTyped(char keyTyped, int __unknown) {
        super.charTyped(keyTyped, __unknown);

        if( keyTyped == 1 && getMinecraft().player != null )
            getMinecraft().player.closeContainer();

        return false;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int x, int y, float partialTicks) {
        renderBackground(guiGraphics);

        int width = this.width;
        int height = this.height;
        if( this.hasSide ) {
            guiGraphics.blit(getBackground(), width / 2 + 60, height / 2 - 180 / 2, 0, 0, 150, 180, 150, 180);
            guiGraphics.blit(getBackground(), width / 2 - 150, height / 2 - 118, 0, 0, this.backgroundWidth, this.backgroundHeight, this.backgroundWidth, this.backgroundHeight);

            if( hasSideTitle() )
                guiGraphics.drawString(getFontRender(), this.sideTitle, width / 2 + 80, height / 2 - 77, 0xffff00);
        }

        if( !this.hasSide )
            guiGraphics.blit(getBackground(), width / 2 - this.backgroundWidth / 2 + 1, height / 2 - this.backgroundHeight / 2, 0, 0, this.backgroundWidth, this.backgroundHeight, this.backgroundWidth, this.backgroundHeight);

//        RenderSystem.enableTexture();
        if( hasTitle() ) {
            if( this.hasSide )
                guiGraphics.drawString(getFontRender(), title(), width / 2 - 138, height / 2 - 105, 0xffff00);
            else
                guiGraphics.drawString(getFontRender(), title(), width / 2 - (this.backgroundWidth / 2 ) + 14, height / 2 - (this.backgroundHeight / 2) + 13, 0xffff00);
        }

        renderExtra(guiGraphics, x, y, partialTicks);
//
//        for (GuiEventListener button : this.children()) {
//            button.render(stack, x, y, partialTicks);
//        }

        for(GuiEventListener button : this.children()) {
            if (button instanceof SupportButton && ((SupportButton) button).isHoveredOrFocused())
                guiGraphics.renderTooltip(getFontRender(), Language.getInstance().getVisualOrder(((SupportButton) button).getSupport()), x, y);
        }

        super.render(guiGraphics, x, y, partialTicks);
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

    public Font getFontRender() {
        return getMinecraft().font;
    }

    public int getWidth() { return this.width; }
    public int getHeight() { return this.height; }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
