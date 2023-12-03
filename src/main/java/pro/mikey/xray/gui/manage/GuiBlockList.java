package pro.mikey.xray.gui.manage;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import pro.mikey.xray.ClientController;
import pro.mikey.xray.gui.GuiSelectionScreen;
import pro.mikey.xray.gui.utils.GuiBase;
import pro.mikey.xray.gui.utils.ScrollingList;
import pro.mikey.xray.store.GameBlockStore;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GuiBlockList extends GuiBase {
    private ScrollingBlockList blockList;
    private ArrayList<GameBlockStore.BlockWithItemStack> blocks;
    private EditBox search;
    private String lastSearched = "";

    public GuiBlockList() {
        super(false);
        this.blocks = ClientController.gameBlockStore.getStore();
    }

    @Override
    public void init() {
        this.blockList = new ScrollingBlockList((getWidth() / 2) + 1, getHeight() / 2 - 12, 202, 185, this.blocks);
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
                search.getValue().length() == 0
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

    static class ScrollingBlockList extends ScrollingList<ScrollingBlockList.BlockSlot> {
        static final int SLOT_HEIGHT = 35;

        ScrollingBlockList(int x, int y, int width, int height, List<GameBlockStore.BlockWithItemStack> blocks) {
            super(x, y, width, height, SLOT_HEIGHT);
            this.updateEntries(blocks);
        }

        @Override
        public void setSelected(@Nullable BlockSlot entry) {
            if (entry == null)
                return;

            Minecraft.getInstance().player.closeContainer();
            Minecraft.getInstance().setScreen(new GuiAddBlock(entry.getBlock().getBlock(), GuiBlockList::new));
        }

        void updateEntries(List<GameBlockStore.BlockWithItemStack> blocks) {
            this.clearEntries(); // @mcp: clearEntries = clearEntries
            blocks.forEach(block -> this.addEntry(new BlockSlot(block, this)));
        }

        public static class BlockSlot extends AbstractSelectionList.Entry<ScrollingBlockList.BlockSlot> {
            GameBlockStore.BlockWithItemStack block;
            ScrollingBlockList parent;

            BlockSlot(GameBlockStore.BlockWithItemStack block, ScrollingBlockList parent) {
                this.block = block;
                this.parent = parent;
            }

            public GameBlockStore.BlockWithItemStack getBlock() {
                return block;
            }

            @Override
            public void render(GuiGraphics graphics, int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean p_194999_5_, float partialTicks) {
                Font font = this.parent.minecraft.font;

                ResourceLocation resource = BuiltInRegistries.ITEM.getKey(this.block.getItemStack().getItem());
                graphics.drawString(font, this.block.getItemStack().getItem().getDescription().getString(), left + 35, top + 7, Color.WHITE.getRGB());
                graphics.drawString(font, resource != null ? resource.getNamespace() : "", left + 35, top + 17, Color.WHITE.getRGB());

                graphics.renderItem(this.block.getItemStack(), left + 8, top + 7);
                graphics.renderItemDecorations(font, this.block.getItemStack(), left + 8, top + 7);

//                Lighting.setupFor3DItems();
//                this.parent.minecraft.getItemRenderer().renderAndDecorateItem(graphics, this.block.getItemStack(), left + 8, top + 7);
//                Lighting.setupForFlatItems();
            }

            @Override
            public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
                this.parent.setSelected(this);
                return false;
            }
        }
    }
}
