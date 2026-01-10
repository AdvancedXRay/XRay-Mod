package pro.mikey.xray.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;
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
    private static final Identifier CIRCLE = XRay.assetLocation("gui/circle.png");

    private Button distButtons;
    private EditBox search;
    public ItemRenderer render;

    private String lastSearch = "";

    private ScanEntryScroller scrollList;

    public ScanManageScreen() {
        super(true);
        this.setSideTitle(I18n.get("xray.single.tools"));

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

        this.render = Minecraft.getInstance().getItemRenderer();
        this.children().clear();

        this.scrollList = new ScanEntryScroller(((getWidth() / 2) - (203 / 2)) - 37, getHeight() / 2 + 10, 203, 185, this);
        addRenderableWidget(this.scrollList);

        this.search = new EditBox(getFontRender(), getWidth() / 2 - 137, getHeight() / 2 - 105, 202, 18, Component.empty());
        this.search.setCanLoseFocus(true);
        addRenderableWidget(this.search);

        addRenderableWidget(
                Button.builder(Component.translatable("xray.input.add"), (btn) -> {
                    minecraft.setScreen(new FindBlockScreen());
                })
                        .pos((getWidth() / 2) + 79, getHeight() / 2 - 60)
                        .size(120, 20)
                        .tooltip(Tooltip.create(Component.translatable("xray.tooltips.add_block")))
                        .build()
        );

        addRenderableWidget(Button.builder(Component.translatable("xray.input.add_hand"), btn -> {
            ItemStack handItem = minecraft.player.getItemInHand(InteractionHand.MAIN_HAND);

            // Check if the hand item is a block or not
            if (!(handItem.getItem() instanceof BlockItem)) {
                minecraft.player.displayClientMessage(Component.literal("[XRay] " + Component.translatable("xray.message.invalid_hand", Utils.safeItemStackName(handItem).getString())), false);
                this.onClose();
                return;
            }

            minecraft.setScreen(new ScanConfigureScreen(((BlockItem) handItem.getItem()).getBlock(), ScanManageScreen::new));
        })
            .pos(getWidth() / 2 + 79, getHeight() / 2 - 38)
            .size(120, 20)
            .tooltip(Tooltip.create(Component.translatable("xray.tooltips.add_block_in_hand")))
            .build()
        );

        addRenderableWidget(Button.builder(Component.translatable("xray.input.add_look"), btn -> {
            Player player = minecraft.player;
            if (minecraft.level == null || player == null) {
                return;
            }

            try {
                Vec3 look = player.getLookAngle();
                Vec3 start = new Vec3(player.position().x(), player.position().y() + player.getEyeHeight(), player.position().z());
                Vec3 end = new Vec3(player.position().x() + look.x * 100, player.position().y() + player.getEyeHeight() + look.y * 100, player.position().z() + look.z * 100);

                ClipContext context = new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player);
                BlockHitResult result = minecraft.level.clip(context);

                if (result.getType() == HitResult.Type.BLOCK) {
                    Block lookingAt = minecraft.level.getBlockState(result.getBlockPos()).getBlock();

                    minecraft.setScreen(new ScanConfigureScreen(lookingAt, ScanManageScreen::new));
                } else {
                    player.displayClientMessage(Component.literal("[XRay] " + I18n.get("xray.message.nothing_infront")), false);
                    this.onClose();
                }
            } catch (NullPointerException ex) {
                player.displayClientMessage(Component.literal("[XRay] " + I18n.get("xray.message.thats_odd")), false);
                this.onClose();
            }
        })
            .pos(getWidth() / 2 + 79, getHeight() / 2 - 16)
            .size(120, 20)
            .tooltip(Tooltip.create(Component.translatable("xray.tooltips.add_block_looking_at"))
        ).build());



        addRenderableWidget(Button.builder(Component.translatable("xray.input.show-lava", ScanController.INSTANCE.isLavaActive()), btn -> {
            ScanController.INSTANCE.toggleLava();
            btn.setMessage(Component.translatable("xray.input.show-lava", ScanController.INSTANCE.isLavaActive()));
        })
                .pos(getWidth() / 2 + 79, getHeight() / 2 + 6)
                .size(120, 20)
                .tooltip(Tooltip.create(Component.translatable("xray.tooltips.show_lava")))
                .build());

        
        AbstractSliderButton radiusSlider = new AbstractSliderButton(
            getWidth() / 2 + 79, getHeight() / 2 + 36, 120, 20,
            Component.translatable("xray.input.distance", ScanController.INSTANCE.getVisualRadius()),
            Mth.map(XRay.config().radius.get(), 0, 8, 0.0, 1.0)
        ) {
            @Override
            protected void updateMessage() {
                int currentRadiusp = Mth.floor(Mth.map(this.value, 0.0, 1.0, 0, 8))    ;
                int currentRadius = Math.max(1, currentRadiusp * 3);
                    this.setMessage(Component.translatable("xray.input.distance", currentRadius));
            }

            @Override
            protected void applyValue() {
                int currentRadius = Mth.floor(Mth.map(this.value, 0.0, 1.0, 0, 8));
                ScanController.INSTANCE.incrementCurrentDist(currentRadius);
            }
        };
        radiusSlider.setTooltip(Tooltip.create(Component.translatable("xray.tooltips.distance")));
        addRenderableWidget(radiusSlider);

        addRenderableWidget(
            Button.builder(Component.translatable("xray.single.help"), button -> {
                minecraft.setScreen(new HelpScreen());
            })
                    .pos(getWidth() / 2 + 79, getHeight() / 2 + 58)
                    .size(60, 20)
                    .build()
        );

        addRenderableWidget(
                Button.builder(Component.translatable("xray.single.close"), button -> {
                    this.onClose();
                })
                        .pos((getWidth() / 2 + 79) + 62, getHeight() / 2 + 58)
                        .size(59, 20)
                        .build()
        );
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
        if (event.button() == 1 && distButtons.isMouseOver(event.x(), event.y())) {
            ScanController.INSTANCE.decrementCurrentDist();
            distButtons.setMessage(Component.translatable("xray.input.distance", ScanController.INSTANCE.getVisualRadius()));
            distButtons.playDownSound(Minecraft.getInstance().getSoundManager());
        }

        return super.mouseClicked(event, bl);
    }

    @Override
    public void renderExtra(GuiGraphics graphics, int x, int y, float partialTicks) {
        if (!search.isFocused() && search.getValue().isEmpty()) {
            graphics.drawString(getFontRender(), I18n.get("xray.single.search"), getWidth() / 2 - 130, getHeight() / 2 - 101, Color.GRAY.getRGB());
        }

        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(this.getWidth() / 2f - 140, ((this.getHeight() / 2f) - 3) + 120);
        pose.scale(0.75f, 0.75f);
        graphics.drawString(this.font, Component.translatable("xray.tooltips.edit1"), 0, 0, Color.GRAY.getRGB());
        pose.translate(0, 12);
        graphics.drawString(this.font, Component.translatable("xray.tooltips.edit2"), 0, 0, Color.GRAY.getRGB());
        pose.popMatrix();
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

    class ScanEntryScroller extends ObjectSelectionList<ScanEntryScroller.ScanSlot> {
        static final int SLOT_HEIGHT = 35;
        public ScanManageScreen parent;

        ScanEntryScroller(int x, int y, int width, int height, ScanManageScreen parent) {
            super(ScanManageScreen.this.minecraft, width - 2, height, (ScanManageScreen.this.height / 2) - (height / 2) + 10, SLOT_HEIGHT);
            this.parent = parent;
            this.setX((parent.getWidth() / 2) - (width / 2) - 36);
            this.updateEntries();
        }

        @Override
        public int getRowWidth() {
            return 188;
        }

        @Override
        protected int scrollBarX() {
            return this.getX() + this.getRowWidth() + 6;
        }

        public void setSelected(@Nullable ScanManageScreen.ScanEntryScroller.ScanSlot entry, MouseButtonEvent mouse) {
            if (entry == null)
                return;

            if (mouse.hasShiftDown()) {
                Minecraft.getInstance().setScreen(new ScanConfigureScreen(entry.entry, ScanManageScreen::new));
                return;
            }

            entry.entry.enabled = !entry.entry.enabled();
            ScanController.INSTANCE.scanStore.save();
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

                this.addEntry(new ScanSlot(category, this));
            }
        }

        public static class ScanSlot extends ObjectSelectionList.Entry<ScanSlot> {
            private final ScanType entry;
            private final ScanEntryScroller parent;
            private final ItemStack icon;

            ScanSlot(ScanType entry, ScanEntryScroller parent) {
                this.entry = entry;
                this.parent = parent;

                if (entry instanceof BlockScanType blockScanType) {
                    this.icon = new ItemStack(blockScanType.block);
                } else {
                    this.icon = ItemStack.EMPTY;
                }
            }

            @Override
            public void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean hovering, float partialTicks) {
                Font font = Minecraft.getInstance().font;

                guiGraphics.drawString(font, this.entry.name(), this.getContentX() + 25, this.getContentY() + 7, 0xFFFFFFFF);
                guiGraphics.drawString(font, this.entry.enabled() ? "Enabled" : "Disabled", this.getContentX() + 25, this.getContentY() + 17, this.entry.enabled() ? Color.GREEN.getRGB() : Color.RED.getRGB());

                guiGraphics.renderItem(this.icon, this.getContentX(), this.getContentY() + 7);

                var stack = guiGraphics.pose();
                stack.pushMatrix();

                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, ScanManageScreen.CIRCLE, (this.getContentX() + this.getWidth()) - 23, (int) (this.getContentY() + (this.getHeight() / 2f) - 9), 0, 0, 14, 14, 14, 14, 0x7F000000);
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, ScanManageScreen.CIRCLE, (this.getContentX() + this.getWidth()) - 21, (int) (this.getContentY() + (this.getHeight() / 2f) - 7), 0, 0, 10, 10, 10, 10, 0xFF000000 | this.entry.colorInt());

                stack.popMatrix();
            }

            @Override
            public boolean mouseClicked(MouseButtonEvent mouse, boolean bl) {
                this.parent.setSelected(this, mouse);
                return false;
            }

            @Override
            public @NotNull Component getNarration() {
                return Component.empty();
            }
        }
    }
}
