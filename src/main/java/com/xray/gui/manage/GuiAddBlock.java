package com.xray.gui.manage;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.xray.XRay;
import com.xray.gui.GuiSelectionScreen;
import com.xray.gui.utils.GuiBase;
import com.xray.utils.BlockData;
import com.xray.xray.Controller;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;
import net.minecraftforge.fml.client.gui.widget.Slider;

import java.util.ArrayList;
import java.util.Objects;

public class GuiAddBlock extends GuiBase {
    private TextFieldWidget oreName;
    private Button addBtn;
    private CustomSlider redSlider;
    private CustomSlider greenSlider;
    private CustomSlider blueSlider;

    private Block selectBlock;
    private ItemStack itemStack;

    private boolean oreNameCleared = false;

    public GuiAddBlock(Block selectedBlock) {
        super(false);
        this.selectBlock = selectedBlock;
        this.itemStack = new ItemStack(selectBlock, 1);
    }

    @Override
    public void func_231160_c_() {// @mcp: func_231160_c_ = init
        // Called when the gui should be (re)created
        addButton(addBtn = new Button(getWidth() / 2 - 100, getHeight() / 2 + 85, 128, 20, new TranslationTextComponent("xray.single.add"), b -> {
            this.onClose();

            if (selectBlock.getRegistryName() == null)
                return;

            // Push the block to the render stack
            Controller.getBlockStore().put(
                    new BlockData(
                            oreName.getText(),
                            selectBlock.getRegistryName().toString(),
                            (((int) (redSlider.getValue() * 255) << 16) + ((int) (greenSlider.getValue() * 255) << 8) + (int) (blueSlider.getValue() * 255)),
                            this.itemStack,
                            true,
                            Controller.getBlockStore().getStore().size() + 1
                    )
            );

            XRay.blockStore.write(new ArrayList<>(Controller.getBlockStore().getStore().values()));
            getMinecraft().displayGuiScreen(new GuiSelectionScreen());
        }));
        addButton(new Button(getWidth() / 2 + 30, getHeight() / 2 + 85, 72, 20, new TranslationTextComponent("xray.single.cancel"), b -> this.onClose()));

        addButton(redSlider = new CustomSlider(getWidth() / 2 - 100, getHeight() / 2 + 7, new TranslationTextComponent("xray.color.red"), 0, 255, 0, (e) -> {}, (e) -> {}));
        addButton(greenSlider = new CustomSlider(getWidth() / 2 - 100, getHeight() / 2 + 30, new TranslationTextComponent("xray.color.green"), 0, 255, 165, (e) -> {}, (e) -> {}));
        addButton(blueSlider = new CustomSlider(getWidth() / 2 - 100, getHeight() / 2 + 53, new TranslationTextComponent("xray.color.blue"), 0, 255, 255, (e) -> {}, (e) -> {}));

        oreName = new TextFieldWidget(getMinecraft().fontRenderer, getWidth() / 2 - 100, getHeight() / 2 - 70, 202, 20, StringTextComponent.field_240750_d_); // @mcp: field_240750_d_ = empty
        oreName.setText(this.selectBlock.func_235333_g_().getString()); // @mcp: func_235333_g_ = getNameTextComponent
        this.field_230705_e_.add(oreName);// @mcp: field_230705_e_ = children
    }

    @Override // @mcp: func_231023_e_ = tick
    public void func_231023_e_() {
        super.func_231023_e_();
        oreName.tick();
    }

    @Override
    public void renderExtra(MatrixStack stack, int x, int y, float partialTicks) {
        // @mcp: func_238405_a_ = drawStringWithShadow
        // @mcp: func_235333_g_ = getNameTextComponent
        getFontRender().func_238405_a_(stack, selectBlock.func_235333_g_().getString(), getWidth() / 2f - 100, getHeight() / 2f - 90, 0xffffff);

        oreName.func_230430_a_(stack, x, y, partialTicks); // @mcp: func_230430_a_ = render
        renderPreview(getWidth() / 2 - 100, getHeight() / 2 - 40, (float) redSlider.getValue(), (float) greenSlider.getValue(), (float) blueSlider.getValue());

        RenderHelper.enableStandardItemLighting();
        this.field_230707_j_.renderItemAndEffectIntoGUI(this.itemStack, getWidth() / 2 + 85, getHeight() / 2 - 105); // @mcp: field_230707_j_ = itemRender
        RenderHelper.disableStandardItemLighting();
    }

    // FIXME: 28/06/2020 replace with matrix system instead of the tess
    static void renderPreview(int x, int y, float r, float g, float b) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder tessellate = tessellator.getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.color4f(r/255, g/255, b/255, 1);
        tessellate.begin(7, DefaultVertexFormats.POSITION);
        tessellate.pos(x, y, 0.0D).endVertex();
        tessellate.pos(x, y + 45, 0.0D).endVertex();
        tessellate.pos(x + 202, y + 45, 0.0D).endVertex();
        tessellate.pos(x + 202, y, 0.0D).endVertex();
        tessellator.draw();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    @Override // @mcp: func_231044_a_ = mouseClicked
    public boolean func_231044_a_(double x, double y, int mouse) {
        if (oreName.func_231044_a_(x, y, mouse))
            this.func_231035_a_(oreName); // @mcp: func_231035_a_ = setFocused

        if (oreName.func_230999_j_() && !oreNameCleared) { // @mcp: func_230999_j_ = isFocused
            oreName.setText("");
            oreNameCleared = true;
        }

        if (!oreName.func_230999_j_() && oreNameCleared && Objects.equals(oreName.getText(), "")) { // @mcp: func_230999_j_ = isFocused
            oreNameCleared = false;
            oreName.setText(I18n.format("xray.input.gui"));
        }

        return super.func_231044_a_(x, y, mouse);
    }

    @Override // @mcp: func_231048_c_ = mouseReleased
    public boolean func_231048_c_(double x, double y, int mouse) {
        if (redSlider.dragging && !redSlider.func_230999_j_())
            redSlider.dragging = false;

        if (greenSlider.dragging && !greenSlider.func_230999_j_())
            greenSlider.dragging = false;

        if (blueSlider.dragging && !blueSlider.func_230999_j_())
            blueSlider.dragging = false;

        return super.func_231048_c_(x, y, mouse);
    }

    @Override
    public boolean hasTitle() {
        return true;
    }

    @Override
    public String title() {
        return I18n.format("xray.title.config");
    }

    public static class CustomSlider extends Slider {
        public CustomSlider(int xPos, int yPos, ITextComponent displayStr, double minVal, double maxVal, double currentVal, IPressable handler, ISlider par) {
            super(xPos, yPos, 202, 20, displayStr, new StringTextComponent(""), minVal, maxVal, currentVal, false, true, handler, par);
        }

        // note: overriding this because the forge one has a bug in it causing the title to have %s button after it...
        @Override
        public void func_230431_b_(MatrixStack mStack, int mouseX, int mouseY, float partial)
        {
            if (this.field_230694_p_)
            {
                Minecraft mc = Minecraft.getInstance();
                this.field_230692_n_ = mouseX >= this.field_230690_l_ && mouseY >= this.field_230691_m_ && mouseX < this.field_230690_l_ + this.field_230688_j_ && mouseY < this.field_230691_m_ + this.field_230689_k_;
                int k = this.func_230989_a_(this.func_230449_g_());
                GuiUtils.drawContinuousTexturedBox(field_230687_i_, this.field_230690_l_, this.field_230691_m_, 0, 46 + k * 20, this.field_230688_j_, this.field_230689_k_, 200, 20, 2, 3, 2, 2, this.func_230927_p_());
                this.func_230441_a_(mStack, mc, mouseX, mouseY);

                ITextComponent buttonText = this.func_230458_i_();
                int strWidth = mc.fontRenderer.func_238414_a_(buttonText);
                int ellipsisWidth = mc.fontRenderer.getStringWidth("...");

                if (strWidth > field_230688_j_ - 6 && strWidth > ellipsisWidth)
                    //TODO, srg names make it hard to figure out how to append to an ITextProperties from this trim operation, wraping this in StringTextComponent is kinda dirty.
                    buttonText = new StringTextComponent(mc.fontRenderer.func_238417_a_(buttonText, field_230688_j_ - 6 - ellipsisWidth).getString() + "...");

                this.func_238472_a_(mStack, mc.fontRenderer, buttonText, this.field_230690_l_ + this.field_230688_j_ / 2, this.field_230691_m_ + (this.field_230689_k_ - 8) / 2, getFGColor());
            }
        }
    }
}
