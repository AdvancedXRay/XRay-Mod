package pro.mikey.xray.gui.manage;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraftforge.client.gui.widget.Slider;
import pro.mikey.xray.ClientController;
import pro.mikey.xray.gui.GuiSelectionScreen;
import pro.mikey.xray.gui.utils.GuiBase;
import pro.mikey.xray.utils.BlockData;
import pro.mikey.xray.xray.Controller;
import net.minecraft.world.level.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Button;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Supplier;

public class GuiAddBlock extends GuiBase {
    private EditBox oreName;
    private Button addBtn;
    private Slider redSlider;
    private Slider greenSlider;
    private Slider blueSlider;

    private Block selectBlock;
    private ItemStack itemStack;

    private boolean oreNameCleared = false;
    private Supplier<GuiBase> previousScreenCallback;

    public GuiAddBlock(Block selectedBlock, Supplier<GuiBase> previousScreenCallback) {
        super(false);
        this.selectBlock = selectedBlock;
        this.previousScreenCallback = previousScreenCallback;
        this.itemStack = new ItemStack(selectBlock, 1);
    }

    @Override
    public void init() {
        // Called when the gui should be (re)created
        addRenderableWidget(addBtn = new Button(getWidth() / 2 - 100, getHeight() / 2 + 85, 128, 20, new TranslatableComponent("xray.single.add"), b -> {
            this.onClose();

            if (selectBlock.getRegistryName() == null)
                return;

            // Push the block to the render stack
            Controller.getBlockStore().put(
                    new BlockData(
                            oreName.getValue(),
                            selectBlock.getRegistryName().toString(),
                            (((int) (redSlider.getValue()) << 16) + ((int) (greenSlider.getValue()) << 8) + (int) (blueSlider.getValue() )),
                            this.itemStack,
                            true,
                            Controller.getBlockStore().getStore().size() + 1
                    )
            );

            ClientController.blockStore.write(new ArrayList<>(Controller.getBlockStore().getStore().values()));
            getMinecraft().setScreen(new GuiSelectionScreen());
        }));
        addRenderableWidget(new Button(getWidth() / 2 + 30, getHeight() / 2 + 85, 72, 20, new TranslatableComponent("xray.single.cancel"), b -> {
            this.onClose();
            Minecraft.getInstance().setScreen(this.previousScreenCallback.get());
        }));

        addRenderableWidget(redSlider = new Slider(getWidth() / 2 - 100, getHeight() / 2 + 7, 202, 20, new TranslatableComponent("xray.color.red"), TextComponent.EMPTY, 0, 255, 0, false, true, (e) -> {}, (e) -> {}));
        addRenderableWidget(greenSlider = new Slider(getWidth() / 2 - 100, getHeight() / 2 + 30, 202, 20, new TranslatableComponent("xray.color.green"), TextComponent.EMPTY, 0, 255, 165, false, true, (e) -> {}, (e) -> {}));
        addRenderableWidget(blueSlider = new Slider(getWidth() / 2 - 100, getHeight() / 2 + 53,202, 20,  new TranslatableComponent("xray.color.blue"), TextComponent.EMPTY, 0, 255, 255, false, true, (e) -> {}, (e) -> {}));

        oreName = new EditBox(getMinecraft().font, getWidth() / 2 - 100, getHeight() / 2 - 70, 202, 20, TextComponent.EMPTY);
        oreName.setValue(this.selectBlock.getName().getString());
        addRenderableWidget(oreName);
    }

    @Override
    public void tick() {
        super.tick();
        oreName.tick();
    }

    @Override
    public void renderExtra(PoseStack stack, int x, int y, float partialTicks) {
        getFontRender().drawShadow(stack, selectBlock.getName().getString(), getWidth() / 2f - 100, getHeight() / 2f - 90, 0xffffff);

        int color = (255 << 24) | ((int) (this.redSlider.getValue()) << 16) | ((int) (this.greenSlider.getValue()) << 8) | (int) (this.blueSlider.getValue());
        fill(stack, (this.getWidth() / 2) - 100, this.getHeight() / 2 - 40, (this.getWidth() / 2) + 102, (this.getHeight() / 2) - 3, color);

        oreName.render(stack, x, y, partialTicks);

        Lighting.setupForFlatItems();
        this.itemRenderer.renderAndDecorateItem(this.itemStack, getWidth() / 2 + 85, getHeight() / 2 - 105);
        Lighting.setupFor3DItems();
    }

    @Override
    public boolean mouseClicked(double x, double y, int mouse) {
        if (oreName.mouseClicked(x, y, mouse))
            this.setFocused(oreName);

        if (oreName.isFocused() && !oreNameCleared) {
            oreName.setValue("");
            oreNameCleared = true;
        }

        if (!oreName.isFocused() && oreNameCleared && Objects.equals(oreName.getValue(), "")) {
            oreNameCleared = false;
            oreName.setValue(this.selectBlock.getName().getString());
        }

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
        return I18n.get("xray.title.config");
    }
}
