package pro.mikey.xray.gui.manage;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import pro.mikey.xray.ClientController;
import pro.mikey.xray.gui.GuiSelectionScreen;
import pro.mikey.xray.gui.utils.GuiBase;
import pro.mikey.xray.utils.BlockData;
import pro.mikey.xray.xray.Controller;
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
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.client.gui.widget.Slider;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Supplier;

public class GuiAddBlock extends GuiBase {
    private TextFieldWidget oreName;
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
        addButton(addBtn = new Button(getWidth() / 2 - 100, getHeight() / 2 + 85, 128, 20, new TranslationTextComponent("xray.single.add"), b -> {
            this.closeScreen();

            if (selectBlock.getRegistryName() == null)
                return;

            // Push the block to the render stack
            Controller.getBlockStore().put(
                    new BlockData(
                            oreName.getText(),
                            selectBlock.getRegistryName().toString(),
                            (((int) (redSlider.getValue()) << 16) + ((int) (greenSlider.getValue()) << 8) + (int) (blueSlider.getValue() )),
                            this.itemStack,
                            true,
                            Controller.getBlockStore().getStore().size() + 1
                    )
            );

            ClientController.blockStore.write(new ArrayList<>(Controller.getBlockStore().getStore().values()));
            getMinecraft().displayGuiScreen(new GuiSelectionScreen());
        }));
        addButton(new Button(getWidth() / 2 + 30, getHeight() / 2 + 85, 72, 20, new TranslationTextComponent("xray.single.cancel"), b -> {
            this.closeScreen();
            Minecraft.getInstance().displayGuiScreen(this.previousScreenCallback.get());
        }));

        addButton(redSlider = new Slider(getWidth() / 2 - 100, getHeight() / 2 + 7, 202, 20, new TranslationTextComponent("xray.color.red"), StringTextComponent.EMPTY, 0, 255, 0, false, true, (e) -> {}, (e) -> {}));
        addButton(greenSlider = new Slider(getWidth() / 2 - 100, getHeight() / 2 + 30, 202, 20, new TranslationTextComponent("xray.color.green"), StringTextComponent.EMPTY, 0, 255, 165, false, true, (e) -> {}, (e) -> {}));
        addButton(blueSlider = new Slider(getWidth() / 2 - 100, getHeight() / 2 + 53,202, 20,  new TranslationTextComponent("xray.color.blue"), StringTextComponent.EMPTY, 0, 255, 255, false, true, (e) -> {}, (e) -> {}));

        oreName = new TextFieldWidget(getMinecraft().fontRenderer, getWidth() / 2 - 100, getHeight() / 2 - 70, 202, 20, StringTextComponent.EMPTY);
        oreName.setText(this.selectBlock.getTranslatedName().getString());
        this.children.add(oreName);
    }

    @Override
    public void tick() {
        super.tick();
        oreName.tick();
    }

    @Override
    public void renderExtra(MatrixStack stack, int x, int y, float partialTicks) {
        getFontRender().drawStringWithShadow(stack, selectBlock.getTranslatedName().getString(), getWidth() / 2f - 100, getHeight() / 2f - 90, 0xffffff);

        oreName.render(stack, x, y, partialTicks);
        renderPreview(getWidth() / 2 - 100, getHeight() / 2 - 40, (float) redSlider.getValue(), (float) greenSlider.getValue(), (float) blueSlider.getValue());

        RenderHelper.enableStandardItemLighting();
        this.itemRenderer.renderItemAndEffectIntoGUI(this.itemStack, getWidth() / 2 + 85, getHeight() / 2 - 105);
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

    @Override
    public boolean mouseClicked(double x, double y, int mouse) {
        if (oreName.mouseClicked(x, y, mouse))
            this.setListener(oreName);

        if (oreName.isFocused() && !oreNameCleared) {
            oreName.setText("");
            oreNameCleared = true;
        }

        if (!oreName.isFocused() && oreNameCleared && Objects.equals(oreName.getText(), "")) {
            oreNameCleared = false;
            oreName.setText(this.selectBlock.getTranslatedName().getString());
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
        return I18n.format("xray.title.config");
    }
}
