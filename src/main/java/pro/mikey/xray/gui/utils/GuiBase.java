package pro.mikey.xray.gui.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
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

    public abstract void renderExtra(PoseStack stack, int x, int y, float partialTicks);

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
    public void render(PoseStack stack, int x, int y, float partialTicks) {
        renderBackground(stack);

        int width = this.width;
        int height = this.height;
        RenderSystem.setShaderTexture(0, getBackground());
        if( this.hasSide ) {
            blit(stack, width / 2 + 60, height / 2 - 180 / 2, 0, 0, 150, 180, 150, 180);
            blit(stack, width / 2 - 150, height / 2 - 118, 0, 0, this.backgroundWidth, this.backgroundHeight, this.backgroundWidth, this.backgroundHeight);

            if( hasSideTitle() )
                getFontRender().drawShadow(stack, this.sideTitle, (float) width / 2 + 80, (float) height / 2 - 77, 0xffff00);
        }

        if( !this.hasSide )
            blit(stack, width / 2 - this.backgroundWidth / 2 + 1, height / 2 - this.backgroundHeight / 2, 0, 0, this.backgroundWidth, this.backgroundHeight, this.backgroundWidth, this.backgroundHeight);

        RenderSystem.enableTexture();
        if( hasTitle() ) {
            if( this.hasSide )
                getFontRender().drawShadow(stack, title(), (float) width / 2 - 138, (float) height / 2 - 105, 0xffff00);
            else
                getFontRender().drawShadow(stack, title(), (float) width / 2 - ((float) this.backgroundWidth / 2 ) + 14, (float) height / 2 - ((float) this.backgroundHeight / 2) + 13, 0xffff00);
        }

        renderExtra(stack, x, y, partialTicks);
//
//        for (GuiEventListener button : this.children()) {
//            button.render(stack, x, y, partialTicks);
//        }

        for(GuiEventListener button : this.children()) {
            if (button instanceof SupportButton && ((SupportButton) button).isHoveredOrFocused())
                renderTooltip(stack, Language.getInstance().getVisualOrder(((SupportButton) button).getSupport()), x, y);
        }

        super.render(stack, x, y, partialTicks);
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
