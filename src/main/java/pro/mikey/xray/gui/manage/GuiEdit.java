package pro.mikey.xray.gui.manage;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.widget.ForgeSlider;
import org.apache.commons.lang3.tuple.Pair;
import pro.mikey.xray.ClientController;
import pro.mikey.xray.gui.GuiSelectionScreen;
import pro.mikey.xray.gui.utils.GuiBase;
import pro.mikey.xray.utils.BlockData;
import pro.mikey.xray.xray.Controller;

import java.util.ArrayList;
import java.util.UUID;

public class GuiEdit extends GuiBase {
    private EditBox oreName;
    private ForgeSlider redSlider;
    private ForgeSlider greenSlider;
    private ForgeSlider blueSlider;
    private final BlockData block;

    public GuiEdit(BlockData block) {
        super(true); // Has a sidebar
        this.setSideTitle(I18n.get("xray.single.tools"));

        this.block = block;
    }

    @Override
    public void init() {
        addRenderableWidget(new Button((getWidth() / 2) + 78, getHeight() / 2 - 60, 120, 20, Component.translatable("xray.single.delete"), b -> {
            Controller.getBlockStore().remove(block.getBlockName());
            ClientController.blockStore.write(new ArrayList<>(Controller.getBlockStore().getStore().values()));

            this.onClose();
            getMinecraft().setScreen(new GuiSelectionScreen());
        }));

        addRenderableWidget(new Button((getWidth() / 2) + 78, getHeight() / 2 + 58, 120, 20, Component.translatable("xray.single.cancel"), b -> {
            this.onClose();
            this.getMinecraft().setScreen(new GuiSelectionScreen());
        }));
        addRenderableWidget(new Button(getWidth() / 2 - 138, getHeight() / 2 + 83, 202, 20, Component.translatable("xray.single.save"), b -> {
            BlockData block = new BlockData(
                    this.oreName.getValue(),
                    this.block.getBlockName(),
                    (((int) (redSlider.getValue()) << 16) + ((int) (greenSlider.getValue()) << 8) + (int) (blueSlider.getValue())),
                    this.block.getItemStack(),
                    this.block.isDrawing(),
                    this.block.getOrder()
            );

            Pair<BlockData, UUID> data = Controller.getBlockStore().getStoreByReference(block.getBlockName());
            Controller.getBlockStore().getStore().remove(data.getValue());
            Controller.getBlockStore().getStore().put(data.getValue(), block);

            ClientController.blockStore.write(new ArrayList<>(Controller.getBlockStore().getStore().values()));
            this.onClose();
            getMinecraft().setScreen(new GuiSelectionScreen());
        }));

        addRenderableWidget(redSlider = new ForgeSlider(getWidth() / 2 - 138, getHeight() / 2 + 7, 202, 20, Component.translatable("xray.color.red"), Component.empty(), 0, 255, (block.getColor() >> 16 & 0xff), true));
        addRenderableWidget(greenSlider = new ForgeSlider(getWidth() / 2 - 138, getHeight() / 2 + 30, 202, 20, Component.translatable("xray.color.green"), Component.empty(), 0, 255, (block.getColor() >> 8 & 0xff), true));
        addRenderableWidget(blueSlider = new ForgeSlider(getWidth() / 2 - 138, getHeight() / 2 + 53,202, 20,  Component.translatable("xray.color.blue"), Component.empty(), 0, 255, (block.getColor() & 0xff), true));

        oreName = new EditBox(getMinecraft().font, getWidth() / 2 - 138, getHeight() / 2 - 63, 202, 20, Component.literal(""));
        oreName.setValue(this.block.getEntryName());
        addRenderableWidget(oreName);
    }

    @Override
    public void tick() {
        super.tick();
        oreName.tick();
    }

    @Override
    public void renderExtra(PoseStack stack, int x, int y, float partialTicks) {
        getFontRender().drawShadow(stack, this.block.getItemStack().getHoverName().getString(), getWidth() / 2f - 138, getHeight() / 2f - 90, 0xffffff);

        oreName.render(stack, x, y, partialTicks);

        int color = (255 << 24) | ((int) (this.redSlider.getValue()) << 16) | ((int) (this.greenSlider.getValue()) << 8) | (int) (this.blueSlider.getValue());
        fill(stack, this.getWidth() / 2 - 138, this.getHeight() / 2 - 40, (this.getWidth() / 2 - 36) + 100, (this.getHeight() / 2 - 40) + 45, color);

        Lighting.setupForFlatItems();
        this.itemRenderer.renderAndDecorateItem(this.block.getItemStack(), getWidth() / 2 + 50, getHeight() / 2 - 105);
        Lighting.setupFor3DItems();
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
        return I18n.get("xray.title.edit");
    }
}
