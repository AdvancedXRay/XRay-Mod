package com.xray.gui.manage;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.xray.XRay;
import com.xray.gui.GuiSelectionScreen;
import com.xray.gui.utils.GuiBase;
import com.xray.utils.BlockData;
import com.xray.xray.Controller;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.client.gui.widget.Slider;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.UUID;

public class GuiEdit extends GuiBase {
    private TextFieldWidget oreName;
    private Slider redSlider;
    private Slider greenSlider;
    private Slider blueSlider;
    private BlockData block;

    public GuiEdit(BlockData block) {
        super(true); // Has a sidebar
        this.setSideTitle(I18n.format("xray.single.tools"));

        this.block = block;
    }

    @Override
    public void init() {
        addButton(new Button((getWidth() / 2) + 78, getHeight() / 2 - 60, 120, 20, new TranslationTextComponent("xray.single.delete"), b -> {
            Controller.getBlockStore().remove(block.getBlockName());
            XRay.blockStore.write(new ArrayList<>(Controller.getBlockStore().getStore().values()));

            this.closeScreen();
            getMinecraft().displayGuiScreen(new GuiSelectionScreen());
        }));

        addButton(new Button((getWidth() / 2) + 78, getHeight() / 2 + 58, 120, 20, new TranslationTextComponent("xray.single.cancel"), b -> {
            this.closeScreen();
            this.getMinecraft().displayGuiScreen(new GuiSelectionScreen());
        }));
        addButton(new Button(getWidth() / 2 - 138, getHeight() / 2 + 83, 202, 20, new TranslationTextComponent("xray.single.save"), b -> {
            BlockData block = new BlockData(
                    this.oreName.getText(),
                    this.block.getBlockName(),
                    (((int) (redSlider.getValue()) << 16) + ((int) (greenSlider.getValue()) << 8) + (int) (blueSlider.getValue())),
                    this.block.getItemStack(),
                    this.block.isDrawing(),
                    this.block.getOrder()
            );

            Pair<BlockData, UUID> data = Controller.getBlockStore().getStoreByReference(block.getBlockName());
            Controller.getBlockStore().getStore().remove(data.getValue());
            Controller.getBlockStore().getStore().put(data.getValue(), block);

            XRay.blockStore.write(new ArrayList<>(Controller.getBlockStore().getStore().values()));
            this.closeScreen();
        }));

        addButton(redSlider = new Slider(getWidth() / 2 - 138, getHeight() / 2 + 7, 202, 20, new TranslationTextComponent("xray.color.red"), StringTextComponent.EMPTY, 0, 255, (block.getColor() >> 16 & 0xff), false, true, (e) -> {}, (e) -> {}));
        addButton(greenSlider = new Slider(getWidth() / 2 - 138, getHeight() / 2 + 30, 202, 20, new TranslationTextComponent("xray.color.green"), StringTextComponent.EMPTY, 0, 255, (block.getColor() >> 8 & 0xff), false, true, (e) -> {}, (e) -> {}));
        addButton(blueSlider = new Slider(getWidth() / 2 - 138, getHeight() / 2 + 53,202, 20,  new TranslationTextComponent("xray.color.blue"), StringTextComponent.EMPTY, 0, 255, (block.getColor() & 0xff), false, true, (e) -> {}, (e) -> {}));

        oreName = new TextFieldWidget(getMinecraft().fontRenderer, getWidth() / 2 - 138, getHeight() / 2 - 63, 202, 20, new StringTextComponent(""));
        oreName.setText(this.block.getEntryName());
        this.children.add(oreName);
    }

    @Override
    public void tick() {
        super.tick();
        oreName.tick();
    }

    @Override
    public void renderExtra(MatrixStack stack, int x, int y, float partialTicks) {
        getFontRender().drawStringWithShadow(stack, this.block.getItemStack().getDisplayName().getString(), getWidth() / 2f - 138, getHeight() / 2f - 90, 0xffffff);

        oreName.render(stack, x, y, partialTicks);

        GuiAddBlock.renderPreview(getWidth() / 2 - 138, getHeight() / 2 - 40, (float) redSlider.getValue(), (float) greenSlider.getValue(), (float) blueSlider.getValue());

        RenderHelper.enableStandardItemLighting();
        this.itemRenderer.renderItemAndEffectIntoGUI(this.block.getItemStack(), getWidth() / 2 + 50, getHeight() / 2 - 105);
        RenderHelper.disableStandardItemLighting();
    }

    @Override
    public boolean mouseClicked(double x, double y, int mouse) {
        if( oreName.mouseClicked(x, y, mouse) )
            this.setListener(oreName);

        return super.mouseClicked(x, y, mouse);
    }

    @Override
    public boolean mouseReleased(double x, double y, int mouse) {
        if (redSlider.dragging && !redSlider.isFocused())
            redSlider.dragging = false;

        if (greenSlider.dragging && !greenSlider.isFocused())
            greenSlider.dragging = false;

        if (blueSlider.dragging && !blueSlider.isFocused())
            blueSlider.dragging = false;

        return super.mouseReleased(x, y, mouse);
    }

    @Override
    public boolean hasTitle() {
        return true;
    }

    @Override
    public String title() {
        return I18n.format("xray.title.edit");
    }
}
