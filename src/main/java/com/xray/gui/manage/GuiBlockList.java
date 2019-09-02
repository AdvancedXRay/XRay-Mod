package com.xray.gui.manage;

import com.xray.XRay;
import com.xray.gui.utils.GuiBase;
import com.xray.gui.utils.ScrollingList;
import com.xray.reference.block.BlockItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.AbstractList;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GuiBlockList extends GuiBase {
    private ScrollingBlockList blockList;
    private ArrayList<BlockItem> blocks;
    private TextFieldWidget search;
    private String lastSearched = "";

    public GuiBlockList() {
        super(false);
        this.blocks = XRay.gameBlockStore.getStore();
    }

    @Override
    public void init() {
        this.blockList = new ScrollingBlockList((width / 2) + 1, height / 2 - 12, 202, 185, this.blocks);
        this.children.add(this.blockList);

        search = new TextFieldWidget(getFontRender(), width / 2 - 100, height / 2 + 85, 140, 18, "");
        search.changeFocus(true);
        this.setFocused(search);

        addButton(new Button(width / 2 + 43, height / 2 + 84, 60, 20, I18n.format("xray.single.cancel"), b -> this.onClose()));
    }

    @Override
    public void tick() {
        search.tick();
        if (!search.getText().equals(this.lastSearched))
            reloadBlocks();

        super.tick();
    }

    private void reloadBlocks() {
        if (this.lastSearched.equals(search.getText()))
            return;

        this.blockList.updateEntries(
                search.getText().length() == 0
                        ? this.blocks
                        : this.blocks.stream()
                            .filter(e -> e.getItemStack().getDisplayName().getFormattedText().toLowerCase().contains(search.getText().toLowerCase()))
                            .collect(Collectors.toList())
        );

        lastSearched = search.getText();
        this.blockList.setScrollAmount(0);
    }

    @Override
    public void render(int x, int y, float f) {
        super.render(x, y, f);
        search.render(x, y, f);
        blockList.render(x, y, f);
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        return super.mouseClicked(x, y, button);
    }

    @Override
    public boolean mouseScrolled(double p_mouseScrolled_1_, double p_mouseScrolled_3_, double p_mouseScrolled_5_) {
        blockList.mouseScrolled(p_mouseScrolled_1_, p_mouseScrolled_3_, p_mouseScrolled_5_);
        return super.mouseScrolled(p_mouseScrolled_1_, p_mouseScrolled_3_, p_mouseScrolled_5_);
    }

    static class ScrollingBlockList extends ScrollingList<ScrollingBlockList.BlockSlot> {
        static final int SLOT_HEIGHT = 35;

        ScrollingBlockList(int x, int y, int width, int height, List<BlockItem> blocks) {
            super(x, y, width, height, SLOT_HEIGHT);
            this.updateEntries(blocks);
        }

        @Override
        public void setSelected(@Nullable BlockSlot entry) {
            if (entry == null)
                return;

            Minecraft.getInstance().player.closeScreen();
            Minecraft.getInstance().displayGuiScreen(new GuiAddBlock(entry.getBlock(), null));
        }

        void updateEntries(List<BlockItem> blocks) {
            this.clearEntries();
            blocks.forEach(block -> this.addEntry(new BlockSlot(block, this)));
        }

        public static class BlockSlot extends AbstractList.AbstractListEntry<ScrollingBlockList.BlockSlot> {
            BlockItem block;
            ScrollingBlockList parent;

            BlockSlot(BlockItem block, ScrollingBlockList parent) {
                this.block = block;
                this.parent = parent;
            }

            public BlockItem getBlock() {
                return block;
            }

            @Override
            public void render(int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean p_194999_5_, float partialTicks) {
                FontRenderer font = this.parent.minecraft.fontRenderer;

                ResourceLocation resource = this.block.getItemStack().getItem().getRegistryName();
                font.drawString(this.block.getItemStack().getItem().getName().getFormattedText(), left + 30, top + 7, Color.WHITE.getRGB());
                font.drawString(resource != null ? resource.getNamespace() : "", left + 30, top + 17, Color.WHITE.getRGB());

                RenderHelper.enableGUIStandardItemLighting();
                this.parent.minecraft.getItemRenderer().renderItemAndEffectIntoGUI(this.block.getItemStack(), left + 5, top + 7);
                RenderHelper.disableStandardItemLighting();
            }

            @Override
            public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
                this.parent.setSelected(this);
                return false;
            }
        }
    }
}
