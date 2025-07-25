package pro.mikey.xray.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import pro.mikey.xray.core.ScanController;
import pro.mikey.xray.screens.helpers.GuiBase;

import java.awt.*;
import java.util.List;
import java.util.function.Predicate;

public class FindBlockScreen extends GuiBase {
    private ScrollingBlockList blockList;
    private EditBox search;
    private String lastSearched = "";

    public FindBlockScreen() {
        super(false);
    }

    @Override
    public void init() {
        this.blockList = new ScrollingBlockList((getWidth() / 2) + 1, getHeight() / 2 - 12, 202, 182);
        addRenderableWidget(this.blockList);

        search = new EditBox(getFontRender(), getWidth() / 2 - 100, getHeight() / 2 + 85, 140, 18, Component.literal(""));
        search.setFocused(true);
        this.setFocused(search);

        addRenderableWidget(Button.builder(Component.translatable("xray.single.cancel"), b -> Minecraft.getInstance().setScreen(new ScanManageScreen()))
                .pos(getWidth() / 2 + 43, getHeight() / 2 + 84)
                .size(60, 20)
                .build());

        reloadBlocks();
    }

    @Override
    public void tick() {
        if (!search.getValue().equals(this.lastSearched)) {
            reloadBlocks();
        }

        super.tick();
    }

    private void reloadBlocks() {
        this.blockList.updateEntries(
                search.getValue().isEmpty()
                        ? BuiltInRegistries.BLOCK.stream()
                            .filter(removeIgnoredBlocks())
                            .toList()
                        : BuiltInRegistries.BLOCK.stream()
                            .filter(removeIgnoredBlocks())
                            .filter(e -> e.getName().getString().toLowerCase().contains(search.getValue().toLowerCase()))
                            .toList()
        );

        lastSearched = search.getValue();
        this.blockList.setScrollAmount(0);
    }

    private static Predicate<Block> removeIgnoredBlocks() {
        return (block) -> !ScanController.blackList.contains(block);
    }

    @Override
    public void renderExtra(GuiGraphics graphics, int x, int y, float partialTicks) {
        search.render(graphics, x, y, partialTicks);
        blockList.render(graphics, x, y, partialTicks);
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if( this.search.mouseClicked (x, y, button) )
            this.setFocused(this.search);

        return super.mouseClicked(x, y, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double mouseXDelta, double mouseYDelta) {
        blockList.mouseScrolled(mouseX, mouseY, mouseXDelta, mouseYDelta);
        return super.mouseScrolled(mouseX, mouseY, mouseXDelta, mouseYDelta);
    }

    public class ScrollingBlockList extends ObjectSelectionList<ScrollingBlockList.BlockSlot> {
        static final int SLOT_HEIGHT = 35;

        ScrollingBlockList(int x, int y, int width, int height) {
            super(FindBlockScreen.this.minecraft, width, height, (FindBlockScreen.this.height / 2) - (height / 2) - 10, SLOT_HEIGHT);
            this.setX((FindBlockScreen.this.getWidth() / 2) - (width / 2) + 1);
        }

        @Override
        public int getRowWidth() {
            return 188;
        }

        @Override
        protected int scrollBarX() {
            return this.getX() + this.getRowWidth() + 7;
        }

        @Override
        public void setSelected(@Nullable BlockSlot entry) {
            if (entry == null) {
                return;
            }

            Minecraft.getInstance().setScreen(new ScanConfigureScreen(entry.getBlock(), FindBlockScreen::new));
        }

        void updateEntries(List<Block> blocks) {
            this.clearEntries();
            blocks.forEach(block -> this.addEntry(new BlockSlot(block, this)));
        }

        @Override
        public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
            super.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        }

        public class BlockSlot extends ObjectSelectionList.Entry<ScrollingBlockList.BlockSlot> {
            Block block;
            ItemStack stack;
            private final ScrollingBlockList parent;

            public BlockSlot(Block block, ScrollingBlockList parent) {
                this.block = block;
                this.parent = parent;
                this.stack = block.asItem().getDefaultInstance();
            }

            public Block getBlock() {
                return block;
            }

            @Override
            public void render(GuiGraphics graphics, int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean p_194999_5_, float partialTicks) {
                Font font = this.parent.minecraft.font;

                var registryName = BuiltInRegistries.BLOCK.getKey(this.block);

                graphics.drawString(font, this.block.getName().getString(), left + 25, top + 7, Color.WHITE.getRGB());
                graphics.drawString(font, registryName.getNamespace(), left + 25, top + 17, Color.GRAY.getRGB());

                graphics.renderItem(this.stack, left, top + 7);
            }

            @Override
            public Component getNarration() {
                return Component.empty();
            }

            @Override
            public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
                this.parent.setSelected(this);
                return false;
            }
        }
    }
}
