package com.xray.gui.manage;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.xray.XRay;
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

    @Override // @mcp: func_231160_c_ = init
    public void func_231160_c_() {
        addButton(new Button((getWidth() / 2) + 78, getHeight() / 2 - 60, 120, 20, new TranslationTextComponent("xray.single.delete"), b -> {
            Controller.getBlockStore().remove(block.getBlockName());
            XRay.blockStore.write(new ArrayList<>(Controller.getBlockStore().getStore().values()));

            this.onClose();
        }));

        addButton(new Button((getWidth() / 2) + 78, getHeight() / 2 + 58, 120, 20, new TranslationTextComponent("xray.single.cancel"), b -> this.onClose()));
        addButton(new Button(getWidth() / 2 - 138, getHeight() / 2 + 83, 202, 20, new TranslationTextComponent("xray.single.save"), b -> {
            BlockData block = new BlockData(
                    this.oreName.getText(),
                    this.block.getBlockName(),
                    (((int) (redSlider.getValue() * 255) << 16) + ((int) (greenSlider.getValue() * 255) << 8) + (int) (blueSlider.getValue() * 255)),
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

        // @fixme
        addButton(redSlider = new Slider(getWidth() / 2 - 138, getHeight() / 2 + 7, 202, 20, new TranslationTextComponent("xray.color.red"), new StringTextComponent(""), 0, 255, 0, false, true, (e) -> {}, (e) -> {}));
        addButton(greenSlider = new Slider(getWidth() / 2 - 138, getHeight() / 2 + 30, 202, 20, new TranslationTextComponent("xray.color.green"), new StringTextComponent(""), 0, 255, 0, false, true, (e) -> {}, (e) -> {}));
        addButton(blueSlider = new Slider(getWidth() / 2 - 138, getHeight() / 2 + 53, 202, 20, new TranslationTextComponent("xray.color.blue"), new StringTextComponent(""), 0, 255, 0, false, true, (e) -> {}, (e) -> {}));

        redSlider.setValue((block.getColor() >> 16 & 0xff) / 255f);
        greenSlider.setValue((block.getColor() >> 8 & 0xff) / 255f);
        blueSlider.setValue((block.getColor() & 0xff) / 255f);

        oreName = new TextFieldWidget(getMinecraft().fontRenderer, getWidth() / 2 - 138, getHeight() / 2 - 63, 202, 20, new StringTextComponent(""));
        oreName.setText(this.block.getEntryName());
        this.field_230705_e_.add(oreName); // @mcp: field_230705_e_ = children
    }

    @Override // @mcp: func_231023_e_ = tick
    public void func_231023_e_() {
        super.func_231023_e_();
        oreName.tick();
    }

    @Override
    public void renderExtra(MatrixStack stack, int x, int y, float partialTicks) {
        getFontRender().func_238405_a_(stack, this.block.getItemStack().getDisplayName().getString(), getWidth() / 2f - 138, getHeight() / 2f - 90, 0xffffff); // @mcp: func_238405_a_ = drawtextwithshadow

        oreName.func_230430_a_(stack, x, y, partialTicks); // @mcp: func_230430_a_ = render

        GuiAddBlock.renderPreview(getWidth() / 2 - 138, getHeight() / 2 - 40, (float) redSlider.getValue(), (float) greenSlider.getValue(), (float) blueSlider.getValue());

        RenderHelper.enableStandardItemLighting();
        this.field_230707_j_.renderItemAndEffectIntoGUI(this.block.getItemStack(), getWidth() / 2 + 50, getHeight() / 2 - 105); // @mcp: field_230707_j_ = itemRenderer
        RenderHelper.disableStandardItemLighting();
    }

    @Override // @mcp: func_231044_a_ = mouseClicked
    public boolean func_231044_a_(double x, double y, int mouse) {
        if( oreName.func_231044_a_(x, y, mouse) )
            this.func_231035_a_(oreName); // @mcp: func_231035_a_ = setFocused

        return super.func_231044_a_(x, y, mouse);
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
