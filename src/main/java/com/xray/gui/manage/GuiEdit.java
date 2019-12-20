package com.xray.gui.manage;

import com.xray.XRay;
import com.xray.gui.utils.GuiBase;
import com.xray.gui.utils.GuiSlider;
import com.xray.utils.BlockData;
import com.xray.store.BlockStore;
import com.xray.utils.TempMapping;
import com.xray.xray.Controller;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.*;
import java.util.ArrayList;
import java.util.UUID;

public class GuiEdit extends GuiBase {
    private TextFieldWidget oreName;
    private GuiSlider redSlider;
    private GuiSlider greenSlider;
    private GuiSlider blueSlider;
    private BlockData block;

    public GuiEdit(BlockData block) {
        super(true); // Has a sidebar
        this.setSideTitle(I18n.format("xray.single.tools"));

        this.block = block;
    }

    @Override
    public void init() {
        addButton(new Button((width / 2) + 78, height / 2 - 60, 120, 20, I18n.format("xray.single.delete"), b -> {
            Controller.getBlockStore().remove(block.getBlockName());
            XRay.blockStore.write(new ArrayList<>(Controller.getBlockStore().getStore().values()));

            this.onClose();
        }));

        addButton(new Button((width / 2) + 78, height / 2 + 58, 120, 20, I18n.format("xray.single.cancel"), b -> this.onClose()));
        addButton(new Button(width / 2 - 138, height / 2 + 83, 202, 20, I18n.format("xray.single.save"), b -> {
            BlockData block = new BlockData(
                    this.oreName.getText(),
                    this.block.getBlockName(),
                    new Color((int) (redSlider.getValue() * 255), (int) (greenSlider.getValue() * 255), (int) (blueSlider.getValue() * 255)),
                    this.block.getItemStack(),
                    this.block.isDrawing(),
                    this.block.getOrder()
            );

            Pair<BlockData, UUID> data = Controller.getBlockStore().getStoreByReference(block.getBlockName());
            Controller.getBlockStore().getStore().remove(data.getValue());
            Controller.getBlockStore().getStore().put(data.getValue(), block);

            XRay.blockStore.write(new ArrayList<>(Controller.getBlockStore().getStore().values()));
            this.onClose();
        }));

        addButton(redSlider = new GuiSlider(width / 2 - 138, height / 2 + 7, I18n.format("xray.color.red"), 0, 255));
        addButton(greenSlider = new GuiSlider(width / 2 - 138, height / 2 + 30, I18n.format("xray.color.green"), 0, 255));
        addButton(blueSlider = new GuiSlider(width / 2 - 138, height / 2 + 53, I18n.format("xray.color.blue"), 0, 255));

        redSlider.setValue((float) block.getColor().getRed() / 255);
        greenSlider.setValue((float) block.getColor().getGreen() / 255);
        blueSlider.setValue((float) block.getColor().getBlue() / 255);

        oreName = new TextFieldWidget(getMinecraft().fontRenderer, width / 2 - 138, height / 2 - 63, 202, 20, "");
        oreName.setText(this.block.getEntryName());
        this.children.add(oreName);
    }

    @Override
    public void tick() {
        super.tick();
        oreName.tick();
    }

    @Override
    public void render(int x, int y, float f) {
        super.render(x, y, f);
        getFontRender().drawStringWithShadow(this.block.getItemStack().getDisplayName().getFormattedText(), width / 2f - 138, height / 2f - 90, 0xffffff);

        oreName.render(x, y, f);

        GuiAddBlock.renderPreview(width / 2 - 138, height / 2 - 40, (float) redSlider.getValue(), (float) greenSlider.getValue(), (float) blueSlider.getValue());

        TempMapping.Render.enableGUIStandardItemLighting();
        this.itemRenderer.renderItemAndEffectIntoGUI(this.block.getItemStack(), width / 2 + 50, height / 2 - 105);
        RenderHelper.disableStandardItemLighting();
    }

    @Override
    public boolean mouseClicked(double x, double y, int mouse) {
        if( oreName.mouseClicked(x, y, mouse) )
            this.setFocused(oreName);

        return super.mouseClicked(x, y, mouse);
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
