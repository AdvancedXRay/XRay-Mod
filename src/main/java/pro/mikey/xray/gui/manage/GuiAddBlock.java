package pro.mikey.xray.gui.manage;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.gui.widget.ExtendedSlider;
import pro.mikey.xray.gui.GuiSelectionScreen;
import pro.mikey.xray.gui.utils.GuiBase;
import pro.mikey.xray.utils.BlockData;
import pro.mikey.xray.xray.Controller;

import java.util.Objects;
import java.util.function.Supplier;

public class GuiAddBlock extends GuiBase {
    private EditBox oreName;
    private ExtendedSlider redSlider;
    private ExtendedSlider greenSlider;
    private ExtendedSlider blueSlider;

    private final Block selectBlock;
    private final ItemStack itemStack;

    private boolean oreNameCleared = false;

    private final Supplier<GuiBase> previousScreenCallback;

    public GuiAddBlock(Block selectedBlock, Supplier<GuiBase> previousScreenCallback) {
        super(false);
        this.selectBlock = selectedBlock;
        this.previousScreenCallback = previousScreenCallback;
        this.itemStack = new ItemStack(selectBlock, 1);
    }

    @Override
    public void init() {
        // Called when the gui should be (re)created
        addRenderableWidget(Button.builder(Component.translatable("xray.single.add"), b -> {
            this.onClose();

            ResourceLocation key = BuiltInRegistries.BLOCK.getKey(selectBlock);
            if (key == null)
                return;

            // Push the block to the render stack
            Controller.getBlockStore().put(
                    new BlockData(
                            oreName.getValue(),
                            key.toString(),
                            (((int) (redSlider.getValue()) << 16) + ((int) (greenSlider.getValue()) << 8) + (int) (blueSlider.getValue() )),
                            this.itemStack,
                            true,
                            Controller.getBlockStore().getStore().size() + 1
                    )
            );

            Controller.getBlockStore().persistBlockStore();
            getMinecraft().setScreen(new GuiSelectionScreen());
        })
                .pos(getWidth() / 2 - 100, getHeight() / 2 + 85)
                .size(128, 20)
                .build());

        addRenderableWidget(Button.builder(Component.translatable("xray.single.cancel"), b -> {
            this.onClose();
            Minecraft.getInstance().setScreen(this.previousScreenCallback.get());
        })
                .pos(getWidth() / 2 + 30, getHeight() / 2 + 85)
                .size(72, 20)
                .build());

        addRenderableWidget(redSlider = new ExtendedSlider(getWidth() / 2 - 100, getHeight() / 2 + 7, 202, 20, Component.translatable("xray.color.red"), Component.empty(), 0, 255, 0, true));
        addRenderableWidget(greenSlider = new ExtendedSlider(getWidth() / 2 - 100, getHeight() / 2 + 30, 202, 20, Component.translatable("xray.color.green"), Component.empty(), 0, 255, 165, true));
        addRenderableWidget(blueSlider = new ExtendedSlider(getWidth() / 2 - 100, getHeight() / 2 + 53,202, 20,  Component.translatable("xray.color.blue"), Component.empty(), 0, 255, 255, true));

        oreName = new EditBox(getMinecraft().font, getWidth() / 2 - 100, getHeight() / 2 - 70, 202, 20, Component.empty());
        oreName.setValue(this.selectBlock.getName().getString());
        addRenderableWidget(oreName);
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void renderExtra(GuiGraphics graphics, int x, int y, float partialTicks) {
        graphics.drawString(font, selectBlock.getName().getString(), getWidth() / 2 - 100, getHeight() / 2 - 90, 0xffffff);

        int color = (255 << 24) | ((int) (this.redSlider.getValue()) << 16) | ((int) (this.greenSlider.getValue()) << 8) | (int) (this.blueSlider.getValue());
        graphics.fill(this.getWidth() / 2 - 100, this.getHeight() / 2 - 45, (this.getWidth() / 2 + 2) + 100, (this.getHeight() / 2 - 45) + 45, color);

        oreName.render(graphics, x, y, partialTicks);

        graphics.renderItem(this.itemStack, this.getWidth() / 2 + 85, this.getHeight() / 2 - 105);
        graphics.renderItemDecorations(font, this.itemStack, this.getWidth() / 2 + 85, this.getHeight() / 2 - 105); // TODO: Verify

//        Lighting.setupForFlatItems();
//        this.itemRenderer.renderAndDecorateItem(stack, this.itemStack, getWidth() / 2 + 85, getHeight() / 2 - 105);
//        Lighting.setupFor3DItems();
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
    public boolean hasTitle() {
        return true;
    }

    @Override
    public String title() {
        return I18n.get("xray.title.config");
    }
}
