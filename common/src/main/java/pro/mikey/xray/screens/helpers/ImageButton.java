package pro.mikey.xray.screens.helpers;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ImageButton extends Button {
    protected final ResourceLocation image;
    protected final int imageWidth;
    protected final int imageHeight;

    ImageButton(int width, int height, int imageWidth, int imageHeight, ResourceLocation resourceLocation, Button.OnPress onPress) {
        super(0, 0, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.image = resourceLocation;
    }

    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);
        int k = this.getX() + this.getWidth() / 2 - this.imageWidth / 2;
        int l = this.getY() + this.getHeight() / 2 - this.imageHeight / 2;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, this.image, k, l, 0f, 0f, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight, 0xFFFFFFFF);
    }

    public static ImageButton.Builder builder(Button.OnPress onPress) {
        return new ImageButton.Builder(onPress);
    }

    public static class Builder {
        private final Button.OnPress onPress;
        private int width = 20;
        private int height = 20;
        private ResourceLocation image;
        private int imageWidth;
        private int imageHeight;

        public Builder(Button.OnPress onPress) {
            this.onPress = onPress;
        }

        public ImageButton.Builder width(int i) {
            this.width = i;
            return this;
        }

        public ImageButton.Builder size(int i, int j) {
            this.width = i;
            this.height = j;
            return this;
        }

        public ImageButton.Builder image(ResourceLocation resourceLocation, int i, int j) {
            this.image = resourceLocation;
            this.imageWidth = i;
            this.imageHeight = j;
            return this;
        }

        public ImageButton build() {
            if (this.image == null) {
                throw new IllegalStateException("Sprite not set");
            } else {
                return new ImageButton(this.width, this.height, this.imageWidth, this.imageHeight, this.image, this.onPress);
            }
        }
    }
}
