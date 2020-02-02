package com.xray.gui.manage;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.xray.XRay;
import com.xray.gui.GuiSelectionScreen;
import com.xray.gui.utils.GuiBase;
import com.xray.gui.utils.GuiSlider;
import com.xray.utils.BlockData;
import com.xray.xray.Controller;
import net.minecraft.block.Block;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Objects;

public class GuiAddBlock extends GuiBase {
    private TextFieldWidget oreName;
    private Button addBtn;
    private GuiSlider redSlider;
    private GuiSlider greenSlider;
    private GuiSlider blueSlider;

    private Block selectBlock;
    private ItemStack itemStack;

    private boolean oreNameCleared = false;

    public GuiAddBlock(Block selectedBlock) {
        super(false);
        this.selectBlock = selectedBlock;
        this.itemStack = new ItemStack(selectBlock, 1);
    }

    @Override
    public void init() {
        // Called when the gui should be (re)created
        addButton(addBtn = new Button(width / 2 - 100, height / 2 + 85, 128, 20, I18n.format("xray.single.add"), b -> {
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
        addButton(new Button(width / 2 + 30, height / 2 + 85, 72, 20, I18n.format("xray.single.cancel"), b -> this.onClose()));

        addButton(redSlider = new GuiSlider(width / 2 - 100, height / 2 + 7, I18n.format("xray.color.red"), 0, 255));
        addButton(greenSlider = new GuiSlider(width / 2 - 100, height / 2 + 30, I18n.format("xray.color.green"), 0, 255));
        addButton(blueSlider = new GuiSlider(width / 2 - 100, height / 2 + 53, I18n.format("xray.color.blue"), 0, 255));

        redSlider.setValue(0.0F);
        greenSlider.setValue(0.654F);
        blueSlider.setValue(1.0F);

        oreName = new TextFieldWidget(getMinecraft().fontRenderer, width / 2 - 100, height / 2 - 70, 202, 20, "");
        oreName.setText(this.selectBlock.getNameTextComponent().getFormattedText());
        this.children.add(oreName);
    }

    @Override
    public void tick() {
        super.tick();
        oreName.tick();
    }

    @Override
    public void renderExtra(int x, int y, float partialTicks) {
        getFontRender().drawStringWithShadow(selectBlock.getNameTextComponent().getFormattedText(), width / 2f - 100, height / 2f - 90, 0xffffff);

        oreName.render(x, y, partialTicks);
        renderPreview(width / 2 - 100, height / 2 - 40, (float) redSlider.getValue(), (float) greenSlider.getValue(), (float) blueSlider.getValue());

        RenderHelper.enableStandardItemLighting();
        this.itemRenderer.renderItemAndEffectIntoGUI(this.itemStack, width / 2 + 85, height / 2 - 105);
        RenderHelper.disableStandardItemLighting();
    }

    static void renderPreview(int x, int y, float r, float g, float b) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder tessellate = tessellator.getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.color4f(r, g, b, 1);
        tessellate.begin(7, DefaultVertexFormats.POSITION);
        tessellate.pos(x, y, 0.0D).endVertex();
        tessellate.pos(x, y + 45, 0.0D).endVertex();
        tessellate.pos(x + 202, y + 45, 0.0D).endVertex();
        tessellate.pos(x + 202, y, 0.0D).endVertex();
        tessellator.draw();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    @Override
    public boolean mouseClicked(double x, double y, int mouse) {
        if (oreName.mouseClicked(x, y, mouse))
            this.setFocused(oreName);

        if (oreName.isFocused() && !oreNameCleared) {
            oreName.setText("");
            oreNameCleared = true;
        }

        if (!oreName.isFocused() && oreNameCleared && Objects.equals(oreName.getText(), "")) {
            oreNameCleared = false;
            oreName.setText(I18n.format("xray.input.gui"));
        }

        return super.mouseClicked(x, y, mouse);
    }

    @Override
    public boolean hasTitle() {
        return true;
    }

    @Override
    public String title() {
        return I18n.format("xray.title.config");
    }
}
