package pro.mikey.xray.gui.manage;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.gui.widget.ForgeSlider;
import net.minecraftforge.registries.ForgeRegistries;
import pro.mikey.xray.ClientController;
import pro.mikey.xray.gui.GuiSelectionScreen;
import pro.mikey.xray.gui.utils.GuiBase;
import pro.mikey.xray.utils.BlockData;
import pro.mikey.xray.xray.Controller;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Supplier;

public class GuiAddBlock extends GuiBase {
    private EditBox oreName;
    private ForgeSlider redSlider;
    private ForgeSlider greenSlider;
    private ForgeSlider blueSlider;

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
        addRenderableWidget(new Button(getWidth() / 2 - 100, getHeight() / 2 + 85, 128, 20, Component.translatable("xray.single.add"), b -> {
            this.onClose();

            ResourceLocation key = ForgeRegistries.BLOCKS.getKey(selectBlock);
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

            ClientController.blockStore.write(new ArrayList<>(Controller.getBlockStore().getStore().values()));
            getMinecraft().setScreen(new GuiSelectionScreen());
        }));
        addRenderableWidget(new Button(getWidth() / 2 + 30, getHeight() / 2 + 85, 72, 20, Component.translatable("xray.single.cancel"), b -> {
            this.onClose();
            Minecraft.getInstance().setScreen(this.previousScreenCallback.get());
        }));

        addRenderableWidget(redSlider = new ForgeSlider(getWidth() / 2 - 100, getHeight() / 2 + 7, 202, 20, Component.translatable("xray.color.red"), Component.empty(), 0, 255, 0, true));
        addRenderableWidget(greenSlider = new ForgeSlider(getWidth() / 2 - 100, getHeight() / 2 + 30, 202, 20, Component.translatable("xray.color.green"), Component.empty(), 0, 255, 165, true));
        addRenderableWidget(blueSlider = new ForgeSlider(getWidth() / 2 - 100, getHeight() / 2 + 53,202, 20,  Component.translatable("xray.color.blue"), Component.empty(), 0, 255, 255, true));

        oreName = new EditBox(getMinecraft().font, getWidth() / 2 - 100, getHeight() / 2 - 70, 202, 20, Component.empty());
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
        fill(stack, this.getWidth() / 2 - 100, this.getHeight() / 2 - 45, (this.getWidth() / 2 + 2) + 100, (this.getHeight() / 2 - 45) + 45, color);

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
    public boolean hasTitle() {
        return true;
    }

    @Override
    public String title() {
        return I18n.get("xray.title.config");
    }
}
