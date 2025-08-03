package pro.mikey.xray.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import pro.mikey.xray.XRay;
import pro.mikey.xray.core.ScanController;
import pro.mikey.xray.core.scanner.BlockScanType;
import pro.mikey.xray.core.scanner.ScanStore;
import pro.mikey.xray.core.scanner.ScanType;
import pro.mikey.xray.screens.helpers.GuiBase;
import pro.mikey.xray.screens.helpers.ImageButton;
import pro.mikey.xray.screens.helpers.SliderWidget;

import java.util.Objects;
import java.util.function.Supplier;

public class ScanConfigureScreen extends GuiBase {
    private static final ResourceLocation TRASH_ICON = XRay.assetLocation("gui/trash.png");

    private EditBox oreName;

    private SliderWidget redSlider;
    private SliderWidget greenSlider;
    private SliderWidget blueSlider;

    private final Block selectBlock;
    private final ItemStack icon;

    private boolean oreNameCleared = false;
    private final Supplier<GuiBase> previousScreenCallback;

    @Nullable
    private ScanType editingType = null;

    public ScanConfigureScreen(Block selectedBlock, Supplier<GuiBase> previousScreenCallback) {
        super(false);
        this.previousScreenCallback = previousScreenCallback;

        this.selectBlock = selectedBlock;
        this.icon = new ItemStack(selectBlock, 1);
    }

    public ScanConfigureScreen(ScanType editingType, Supplier<GuiBase> previousScreenCallback) {
        super(false);
        this.previousScreenCallback = previousScreenCallback;

        if (!(editingType instanceof BlockScanType blockScanType)) {
            throw new IllegalArgumentException("Editing type must be an instance of BlockScanType");
        }

        this.editingType = blockScanType;
        this.selectBlock = blockScanType.block;

        this.icon = new ItemStack(selectBlock, 1);
    }

    @Override
    public void init() {
        // Called when the gui should be (re)created
        GridLayout layout = new GridLayout();
        layout.columnSpacing(3);
        layout.setPosition(getWidth() / 2 - 100, getHeight() / 2 + 85);
        GridLayout.RowHelper rowHelper = layout.createRowHelper(3);

        rowHelper.addChild(ImageButton.builder(b -> {
            removeBlock();
        })
                .image(XRay.assetLocation("gui/trash.png"), 16, 16)
                .size(20, 20)
                .build());

        rowHelper.addChild(Button.builder(Component.translatable("xray.single.cancel"), b -> Minecraft.getInstance().setScreen(this.previousScreenCallback.get()))
                .size(60, 20)
                .build());

        rowHelper.addChild(Button.builder(Component.translatable(editingType != null ? "xray.title.edit" : "xray.single.add"), b -> {
                            if (editingType != null) {
                                editBlock();
                            } else {
                                addBlock();
                            }
                        })
                        .size(117, 20)
                        .build());

        layout.arrangeElements();
        layout.visitWidgets(this::addRenderableWidget);

        int defaultColor = 0x00A8FF; // Default color (Blue)
        if (editingType != null) {
            defaultColor = editingType.colorInt;
        }

        double red = (defaultColor >> 16 & 0xFF) / 255.0;
        double green = (defaultColor >> 8 & 0xFF) / 255.0;
        double blue = (defaultColor & 0xFF) / 255.0;

        addRenderableWidget(redSlider = new SliderWidget(getWidth() / 2 - 100, getHeight() / 2 + 7, 202, 20, "xray.color.red", red));
        addRenderableWidget(greenSlider = new SliderWidget(getWidth() / 2 - 100, getHeight() / 2 + 30, 202, 20, "xray.color.green", green));
        addRenderableWidget(blueSlider = new SliderWidget(getWidth() / 2 - 100, getHeight() / 2 + 53,202, 20,  "xray.color.blue", blue));

        oreName = new EditBox(minecraft.font, getWidth() / 2 - 100, getHeight() / 2 - 70, 202, 20, Component.empty());
        if (editingType != null) {
            oreName.setValue(editingType.name);
        } else {
            oreName.setValue(selectBlock.getName().getString());
        }

        addRenderableWidget(oreName);
    }

    private void editBlock() {
        if (editingType == null) {
            throw new IllegalStateException("Editing type is not set");
        }

        int color = (int) (redSlider.getValue() * 255) << 16
                | (int) (greenSlider.getValue() * 255) << 8
                | (int) (blueSlider.getValue() * 255);

        editingType.updateColor(color);
        editingType.name = oreName.getValue();
        ScanController.INSTANCE.scanStore.save();
        ScanController.INSTANCE.requestBlockFinder(true);
        minecraft.setScreen(this.previousScreenCallback.get());
    }

    private void removeBlock() {
        if (editingType == null) {
            throw new IllegalStateException("Editing type is not set");
        }

        ScanStore scanStore = ScanController.INSTANCE.scanStore;
        scanStore.removeEntry(editingType);
        ScanController.INSTANCE.requestBlockFinder(true);
        minecraft.setScreen(this.previousScreenCallback.get());
    }

    private void addBlock() {
        if (editingType != null) {
            throw new IllegalStateException("Editing type is already set");
        }

        ScanStore scanStore = ScanController.INSTANCE.scanStore;
        scanStore.addEntry(new BlockScanType(
                selectBlock,
                oreName.getValue(),
                // Save as RGB by default
                "rgb(" + (int) (redSlider.getValue() * 255) + ", " + (int) (greenSlider.getValue() * 255) + ", " + (int) (blueSlider.getValue() * 255) + ")",
                scanStore.getNextOrder()
        ));

        ScanController.INSTANCE.requestBlockFinder(true);
        minecraft.setScreen(new ScanManageScreen());
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void renderExtra(GuiGraphics graphics, int x, int y, float partialTicks) {
        graphics.drawString(font, selectBlock.getName().getString(), getWidth() / 2 - 100, getHeight() / 2 - 90, 0xffffffff);

        int color = (255 << 24) | ((int) (this.redSlider.getValue() * 255) << 16) | ((int) (this.greenSlider.getValue() * 255) << 8) | (int) (this.blueSlider.getValue() * 255);
        graphics.fill(this.getWidth() / 2 - 100, this.getHeight() / 2 - 45, (this.getWidth() / 2 + 2) + 100, (this.getHeight() / 2 - 45) + 45, color);

        oreName.render(graphics, x, y, partialTicks);

        graphics.renderItem(this.icon, this.getWidth() / 2 + 85, this.getHeight() / 2 - 105);
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
