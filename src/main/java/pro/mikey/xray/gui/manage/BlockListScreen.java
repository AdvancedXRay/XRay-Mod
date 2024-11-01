package pro.mikey.xray.gui.manage;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import pro.mikey.xray.gui.GuiSelectionScreen;
import pro.mikey.xray.gui.utils.GuiBase;
import pro.mikey.xray.utils.BlockData;
import pro.mikey.xray.xray.Controller;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BlockListScreen extends GuiBase {
    private ScrollingBlockList blockList;
    private final List<BlockData> blocks;
    private EditBox search;
    private String lastSearched = "";

    public BlockListScreen() {
        super(false);
        this.blocks = new ArrayList<>(Controller.getBlockStore().getStore().values());
    }

    @Override
    public void init() {
        this.blockList = new ScrollingBlockList((getWidth() / 2) + 1, getHeight() / 2 - 12, 202, 182, this.blocks);
        addRenderableWidget(this.blockList);

        search = new EditBox(getFontRender(), getWidth() / 2 - 100, getHeight() / 2 + 85, 140, 18, Component.literal(""));
        search.setFocused(true);
        this.setFocused(search);

        addRenderableWidget(Button.builder(Component.translatable("xray.single.cancel"), b -> {
                    this.onClose();
                    Minecraft.getInstance().setScreen(new GuiSelectionScreen());
                })
                .pos(getWidth() / 2 + 43, getHeight() / 2 + 84)
                .size(60, 20)
                .build());
    }

    @Override
    public void tick() {
        if (!search.getValue().equals(this.lastSearched))
            reloadBlocks();

        super.tick();
    }

    private void reloadBlocks() {
        if (this.lastSearched.equals(search.getValue()))
            return;

        this.blockList.updateEntries(
                search.getValue().isEmpty()
                        ? this.blocks
                        : this.blocks.stream()
                        .filter(e -> e.getItemStack().getHoverName().getString().toLowerCase().contains(search.getValue().toLowerCase()))
                        .collect(Collectors.toList())
        );

        lastSearched = search.getValue();
        this.blockList.setScrollAmount(0);
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

        ScrollingBlockList(int x, int y, int width, int height, List<BlockData> blocks) {
            super(BlockListScreen.this.minecraft, width, height, (BlockListScreen.this.height / 2) - (height / 2) - 10, SLOT_HEIGHT);
//            super(x, y, width, height, SLOT_HEIGHT);
            this.updateEntries(blocks);
            this.setX((BlockListScreen.this.getWidth() / 2) - (width / 2) + 1);
        }

        @Override
        public int getRowWidth() {
            return 188;
        }

        @Override
        protected int getScrollbarPosition() {
            return this.getX() + this.getRowWidth() + 7;
        }

        @Override
        public void setSelected(@Nullable BlockSlot entry) {
            if (entry == null)
                return;

            Minecraft.getInstance().player.closeContainer();

            Minecraft.getInstance().setScreen(new GuiAddBlock(entry.getBlock().getBlock(), BlockListScreen::new));
        }

        void updateEntries(List<BlockData> blocks) {
            this.clearEntries(); // @mcp: clearEntries = clearEntries
            blocks.forEach(block -> this.addEntry(new BlockSlot(block, this)));
        }

        @Override
        public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
            super.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        }

        public class BlockSlot extends ObjectSelectionList.Entry<ScrollingBlockList.BlockSlot> {
            BlockData block;
            private final ScrollingBlockList parent;

            public BlockSlot(BlockData block, ScrollingBlockList parent) {
                this.block = block;
                this.parent = parent;
            }

            public BlockData getBlock() {
                return block;
            }

            @Override
            public void render(GuiGraphics graphics, int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean p_194999_5_, float partialTicks) {
                Font font = this.parent.minecraft.font;

                ResourceLocation resource = BuiltInRegistries.ITEM.getKey(this.block.getItemStack().getItem());
                graphics.drawString(font, this.block.getItemStack().getItem().getDescription().getString(), left + 25, top + 7, Color.WHITE.getRGB());
                graphics.drawString(font, resource != null ? resource.getNamespace() : "", left + 25, top + 17, Color.GRAY.getRGB());

                graphics.renderItem(this.block.getItemStack(), left, top + 7);
                graphics.renderItemDecorations(font, this.block.getItemStack(), left, top + 7);
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
