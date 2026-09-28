package pro.mikey.xray.screens;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.EqualSpacingLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import pro.mikey.xray.core.scanner.BlockScanType;
import pro.mikey.xray.core.scanner.ScanStore;
import pro.mikey.xray.core.scanner.ScanType;
import pro.mikey.xray.screens.helpers.GuiBase;
import pro.mikey.xray.screens.helpers.SupportButton;
import pro.mikey.xray.utils.Utils;
import pro.mikey.xray.XRay;
import pro.mikey.xray.core.ScanController;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ScanManageScreen extends GuiBase {
    private static final Identifier EDIT_ICON = XRay.id("icon/edit");

    private Button distButtons;
    private EditBox search;

    private String lastSearch = "";

    private ScanEntryScroller scrollList;

    public ScanManageScreen() {
        super(true);

        ScanStore scanStore = ScanController.INSTANCE.scanStore;
        if (scanStore.categories().isEmpty()) {
            // If there are no categories, we need to create the default ones
            scanStore.createDefaultCategories();
        }
    }

    @Override
    public void init() {
        assert minecraft != null;
        if (minecraft.player == null) {
            return;
        }

        this.children().clear();

        this.scrollList = new ScanEntryScroller(getWidth() / 2 - 137, getHeight() / 2 - 82, 201, 185);
        addRenderableWidget(this.scrollList);

        this.search = new EditBox(getFontRender(), getWidth() / 2 - 137, getHeight() / 2 - 105, 202, 18, Component.empty());
        this.search.setCanLoseFocus(true);
        addRenderableWidget(this.search);

        LinearLayout panel = LinearLayout.vertical().spacing(2);

        EqualSpacingLayout icons = panel.addChild(new EqualSpacingLayout(120, 20, EqualSpacingLayout.Orientation.HORIZONTAL));
        icons.addChild(iconButton("reset", "xray.single.reset", "xray.tooltips.reset_defaults", btn -> this.confirmResetToDefaults()));
        icons.addChild(iconButton("clear", "xray.single.clear_all", "xray.tooltips.clear_all", btn -> this.confirmClearAll()));
        icons.addChild(iconButton("close", "xray.single.close", "xray.single.close", btn -> this.onClose()));

        panel.addChild(SpacerElement.height(18));

        panel.addChild(Button.builder(Component.translatable("xray.input.add"), (btn) -> {
                    minecraft.gui.setScreen(new FindBlockScreen());
                })
                .size(120, 20)
                .tooltip(Tooltip.create(Component.translatable("xray.tooltips.add_block")))
                .build());

        panel.addChild(Button.builder(Component.translatable("xray.input.add_hand"), btn -> {
            ItemStack handItem = minecraft.player.getItemInHand(InteractionHand.MAIN_HAND);

            // Check if the hand item is a block or not
            if (!(handItem.getItem() instanceof BlockItem)) {
                minecraft.player.sendSystemMessage(Component.literal("[XRay] " + Component.translatable("xray.message.invalid_hand", Utils.safeItemStackName(handItem).getString())));
                this.onClose();
                return;
            }

            minecraft.gui.setScreen(new ScanConfigureScreen(((BlockItem) handItem.getItem()).getBlock(), ScanManageScreen::new));
        })
                .size(120, 20)
                .tooltip(Tooltip.create(Component.translatable("xray.tooltips.add_block_in_hand")))
                .build());

        panel.addChild(Button.builder(Component.translatable("xray.input.add_look"), btn -> {
            Player player = minecraft.player;
            if (minecraft.level == null || player == null) {
                return;
            }

            try {
                Vec3 look = player.getLookAngle();
                Vec3 start = new Vec3(player.blockPosition().getX(), player.blockPosition().getY() + player.getEyeHeight(), player.blockPosition().getZ());
                Vec3 end = new Vec3(player.blockPosition().getX() + look.x * 100, player.blockPosition().getY() + player.getEyeHeight() + look.y * 100, player.blockPosition().getZ() + look.z * 100);

                ClipContext context = new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player);
                BlockHitResult result = minecraft.level.clip(context);

                if (result.getType() == HitResult.Type.BLOCK) {
                    Block lookingAt = minecraft.level.getBlockState(result.getBlockPos()).getBlock();

                    minecraft.gui.setScreen(new ScanConfigureScreen(lookingAt, ScanManageScreen::new));
                } else {
                    player.sendSystemMessage(Component.literal("[XRay] " + I18n.get("xray.message.nothing_infront")));
                    this.onClose();
                }
            } catch (NullPointerException ex) {
                player.sendSystemMessage(Component.literal("[XRay] " + I18n.get("xray.message.thats_odd")));
                this.onClose();
            }
        })
                .size(120, 20)
                .tooltip(Tooltip.create(Component.translatable("xray.tooltips.add_block_looking_at")))
                .build());

        panel.addChild(Button.builder(Component.translatable("xray.input.show-lava", ScanController.INSTANCE.isLavaActive()), btn -> {
                    ScanController.INSTANCE.toggleLava();
                    btn.setMessage(Component.translatable("xray.input.show-lava", ScanController.INSTANCE.isLavaActive()));
                })
                .size(120, 20)
                .tooltip(Tooltip.create(Component.translatable("xray.tooltips.show_lava")))
                .build());

        this.distButtons = panel.addChild(Button.builder(Component.translatable("xray.input.distance", ScanController.INSTANCE.getVisualRadius()), btn -> {
                    ScanController.INSTANCE.incrementCurrentDist();
                    btn.setMessage(Component.translatable("xray.input.distance", ScanController.INSTANCE.getVisualRadius()));
                })
                .size(120, 20)
                .tooltip(Tooltip.create(Component.translatable("xray.tooltips.distance")))
                .build(), s -> s.paddingTop(8));

        panel.setPosition(getWidth() / 2 + 79, getHeight() / 2 - 80);
        panel.arrangeElements();
        panel.visitWidgets(this::addRenderableWidget);
    }

    private static SpriteIconButton iconButton(String icon, String labelKey, String tooltipKey, Button.OnPress onPress) {
        SpriteIconButton button = SpriteIconButton.builder(Component.translatable(labelKey), onPress, true)
                .sprite(XRay.id("icon/" + icon), 12, 12)
                .size(38, 20)
                .build();

        button.setTooltip(Tooltip.create(Component.translatable(tooltipKey)));
        return button;
    }

    private void confirmResetToDefaults() {
        minecraft.gui.setScreen(new ConfirmScreen(confirmed -> {
            if (confirmed) {
                ScanController.INSTANCE.scanStore.resetToDefaults();
                ScanController.INSTANCE.requestBlockFinder(true);
            }

            minecraft.gui.setScreen(new ScanManageScreen());
        },
                Component.translatable("xray.confirm.reset_defaults.title"),
                Component.translatable("xray.confirm.reset_defaults.body"),
                Component.translatable("xray.confirm.reset_defaults.confirm"),
                CommonComponents.GUI_CANCEL
        ));
    }

    private void confirmClearAll() {
        minecraft.gui.setScreen(new ConfirmScreen(confirmed -> {
            if (confirmed) {
                ScanController.INSTANCE.scanStore.clearEntries();
                ScanController.INSTANCE.requestBlockFinder(true);
            }

            minecraft.gui.setScreen(new ScanManageScreen());
        },
                Component.translatable("xray.confirm.clear_all.title"),
                Component.translatable("xray.confirm.clear_all.body"),
                Component.translatable("xray.confirm.clear_all.confirm"),
                CommonComponents.GUI_CANCEL
        ));
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        if (!search.isFocused() && keyEvent.key() == XRay.OPEN_GUI_KEY.key.getValue()) {
            this.onClose();
            return true;
        }

        return super.keyPressed(keyEvent);
    }

    private void updateSearch() {
        if (lastSearch.equals(search.getValue())) {
            return;
        }

        this.scrollList.updateEntries();
        lastSearch = search.getValue();
    }

    @Override
    public void tick() {
        super.tick();

        updateSearch();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
        if (search.mouseClicked(event, bl))
            this.setFocused(search);

        // Shift action!
        if (event.button() == InputConstants.MOUSE_BUTTON_RIGHT && distButtons.isMouseOver(event.x(), event.y())) {
            ScanController.INSTANCE.decrementCurrentDist();
            distButtons.setMessage(Component.translatable("xray.input.distance", ScanController.INSTANCE.getVisualRadius()));
            distButtons.playDownSound(Minecraft.getInstance().getSoundManager());
        }

        return super.mouseClicked(event, bl);
    }

    @Override
    public void renderExtra(GuiGraphicsExtractor graphics, int x, int y, float partialTicks) {
        if (!search.isFocused() && search.getValue().isEmpty()) {
            graphics.text(getFontRender(), I18n.get("xray.single.search"), getWidth() / 2 - 130, getHeight() / 2 - 101, Color.GRAY.getRGB());
        }
    }

    @Override
    public void removed() {
        ScanController.INSTANCE.requestBlockFinder(true);
        super.removed();
    }

    static final class SupportButtonInner extends SupportButton {
        public SupportButtonInner(int widthIn, int heightIn, int width, int height, Component text, String i18nKey, OnPress onPress) {
            super(widthIn, heightIn, width, height, text, Component.translatable(i18nKey), onPress);
        }
    }

    class ScanEntryScroller extends ContainerObjectSelectionList<ScanEntryScroller.ScanSlot> {
        static final int SLOT_HEIGHT = 24;

        ScanEntryScroller(int x, int y, int width, int height) {
            super(ScanManageScreen.this.minecraft, width, height, y, SLOT_HEIGHT);
            this.setX(x);
            this.updateEntries();
        }

        @Override
        public int getRowLeft() {
            return this.getX();
        }

        @Override
        public int getRowWidth() {
            return this.getWidth() - this.scrollbarWidth() - 2;
        }

        @Override
        protected int scrollBarX() {
            return this.getRowRight() + 2;
        }

        @Override
        protected void extractListBackground(GuiGraphicsExtractor graphics) {
        }

        @Override
        protected void extractListSeparators(GuiGraphicsExtractor graphics) {
        }

        void updateEntries() {
            this.clearEntries();

            var searchString = search == null ? "" : search.getValue().toLowerCase();

            ScanStore scanStore = ScanController.INSTANCE.scanStore;
            var entries = scanStore.categories().stream().findFirst();
            if (entries.isEmpty()) {
                return;
            }

            List<ScanType> scanTargets = new ArrayList<>(entries.get().entries());
            scanTargets.sort(Comparator.comparing(ScanType::order));

            for (ScanType category : scanTargets) {
                if (!searchString.isEmpty() && !category.name().toLowerCase().contains(searchString)) {
                    continue;
                }

                this.addEntry(new ScanSlot(category));
            }
        }

        class ScanSlot extends ContainerObjectSelectionList.Entry<ScanSlot> {
            // Layout, left to right: [colour square + block] name ... [edit] [checkbox]
            private static final int INSET = 3;
            private static final int SWATCH_SIZE = 20;

            private final ScanType entry;
            private final ItemStack icon;
            private final Checkbox enabledBox;
            private final SpriteIconButton editButton;

            ScanSlot(ScanType entry) {
                this.entry = entry;
                this.icon = entry instanceof BlockScanType blockScanType ? new ItemStack(blockScanType.block) : ItemStack.EMPTY;

                // Built with an empty label so only the box renders; the checkbox copies its label at
                // construction, so setting the message afterwards only changes what the narrator reads.
                this.enabledBox = Checkbox.builder(Component.empty(), minecraft.font)
                        .maxWidth(Checkbox.getBoxSize(minecraft.font))
                        .selected(entry.enabled())
                        .onValueChange((box, enabled) -> {
                            this.entry.enabled = enabled;
                            ScanController.INSTANCE.scanStore.save();
                        })
                        .build();
                this.enabledBox.setMessage(Component.literal(entry.name()));

                this.editButton = SpriteIconButton.builder(Component.translatable("xray.tooltips.edit_entry"), btn -> this.edit(), true)
                        .sprite(EDIT_ICON, 12, 12)
                        .size(18, 18)
                        .narration(narration -> Component.translatable("xray.narration.edit_entry", entry.name()))
                        .build();
                this.editButton.setTooltip(Tooltip.create(Component.translatable("xray.tooltips.edit_entry")));
            }

            private void edit() {
                minecraft.gui.setScreen(new ScanConfigureScreen(this.entry, ScanManageScreen::new));
            }

            @Override
            public void extractContent(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, boolean hovering, float partialTicks) {
                int left = this.getContentX();
                int right = this.getContentRight() - INSET;
                int middle = this.getContentYMiddle();

                // Highlight colour as a square behind the block
                int swatchY = middle - SWATCH_SIZE / 2;
                guiGraphics.fill(left, swatchY, left + SWATCH_SIZE, swatchY + SWATCH_SIZE, 0xFF000000);
                guiGraphics.fill(left + 1, swatchY + 1, left + SWATCH_SIZE - 1, swatchY + SWATCH_SIZE - 1, 0xFF000000 | this.entry.colorInt());
                guiGraphics.item(this.icon, left + 2, swatchY + 2);

                // Widgets live in the row, so they're repositioned every frame as the list scrolls
                this.enabledBox.setPosition(right - this.enabledBox.getWidth(), middle - this.enabledBox.getHeight() / 2);
                this.editButton.setPosition(this.enabledBox.getX() - 3 - this.editButton.getWidth(), middle - this.editButton.getHeight() / 2);

                // Name, scrolling like vanilla button labels when it's too long to fit
                int nameLeft = left + SWATCH_SIZE + 5;
                int nameRight = this.editButton.getX() - 4;
                Component name = Component.literal(this.entry.name()).withStyle(this.entry.enabled() ? ChatFormatting.WHITE : ChatFormatting.GRAY);
                guiGraphics.textRenderer().acceptScrolling(name, nameLeft, nameLeft, nameRight, this.getContentY(), this.getContentBottom());

                this.editButton.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
                this.enabledBox.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
            }

            @Override
            public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
                // The checkbox and edit button get first go
                if (super.mouseClicked(event, doubleClick)) {
                    return true;
                }

                if (event.button() != InputConstants.MOUSE_BUTTON_LEFT) {
                    return false;
                }

                AbstractWidget.playButtonClickSound(minecraft.getSoundManager());

                if (event.hasShiftDown()) {
                    this.edit();
                } else {
                    this.enabledBox.onPress(event);
                }

                return true;
            }

            @Override
            public List<? extends GuiEventListener> children() {
                return List.of(this.editButton, this.enabledBox);
            }

            @Override
            public List<? extends NarratableEntry> narratables() {
                return List.of(this.editButton, this.enabledBox);
            }
        }
    }
}
