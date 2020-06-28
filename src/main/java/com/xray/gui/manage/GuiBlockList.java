package com.xray.gui.manage;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.xray.XRay;
import com.xray.gui.utils.GuiBase;
import com.xray.gui.utils.ScrollingList;
import com.xray.store.GameBlockStore.BlockWithItemStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.AbstractList;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GuiBlockList extends GuiBase {
    private ScrollingBlockList blockList;
    private ArrayList<BlockWithItemStack> blocks;
    private TextFieldWidget search;
    private String lastSearched = "";

    public GuiBlockList() {
        super(false);
        this.blocks = XRay.gameBlockStore.getStore();
    }

    @Override // @mcp: func_231160_c_ = init
    public void func_231160_c_() {
        this.blockList = new ScrollingBlockList((getWidth() / 2) + 1, getHeight() / 2 - 12, 202, 185, this.blocks);
        this.field_230705_e_.add(this.blockList); // @mcp: field_230705_e_ = children

        search = new TextFieldWidget(getFontRender(), getWidth() / 2 - 100, getHeight() / 2 + 85, 140, 18, new StringTextComponent(""));
        search.func_231049_c__(true); // @mcp: func_231049_c__ = changeFocus
        this.func_231035_a_(search);// @mcp: func_231035_a_ = setFocused

        addButton(new Button(getWidth() / 2 + 43, getHeight() / 2 + 84, 60, 20, new TranslationTextComponent("xray.single.cancel"), b -> this.onClose()));
    }

    @Override // @mcp: func_231023_e_ = tick
    public void func_231023_e_() {
        search.tick();
        if (!search.getText().equals(this.lastSearched))
            reloadBlocks();

        super.func_231023_e_();
    }

    private void reloadBlocks() {
        if (this.lastSearched.equals(search.getText()))
            return;

        this.blockList.updateEntries(
                search.getText().length() == 0
                        ? this.blocks
                        : this.blocks.stream() // @fixme: format issue likely
                            .filter(e -> e.getItemStack().getDisplayName().toString().toLowerCase().contains(search.getText().toLowerCase()))
                            .collect(Collectors.toList())
        );

        lastSearched = search.getText();
        this.blockList.func_230932_a_(0); // @mcp: func_230932_a_ = setScrollAmount
    }

    @Override
    public void renderExtra(MatrixStack stack, int x, int y, float partialTicks) {
        search.func_230430_a_(stack, x, y, partialTicks); // @mcp: func_230430_a_ = render
        blockList.func_230430_a_(stack, x, y, partialTicks); // @mcp: func_230430_a_ = render
    }

    @Override // @mcp: func_231044_a_ = mouseClicked
    public boolean func_231044_a_ (double x, double y, int button) {
        if( this.search.func_231044_a_ (x, y, button) )
            this.func_231035_a_(this.search); // @mcp: func_231035_a_ = setFocused

        return super.func_231044_a_ (x, y, button);
    }

    @Override // @mcp: func_231043_a_ = mouseScrolled
    public boolean func_231043_a_(double p_mouseScrolled_1_, double p_mouseScrolled_3_, double p_mouseScrolled_5_) {
        blockList.func_231043_a_(p_mouseScrolled_1_, p_mouseScrolled_3_, p_mouseScrolled_5_);
        return super.func_231043_a_(p_mouseScrolled_1_, p_mouseScrolled_3_, p_mouseScrolled_5_);
    }

    static class ScrollingBlockList extends ScrollingList<ScrollingBlockList.BlockSlot> {
        static final int SLOT_HEIGHT = 35;

        ScrollingBlockList(int x, int y, int width, int height, List<BlockWithItemStack> blocks) {
            super(x, y, width, height, SLOT_HEIGHT);
            this.updateEntries(blocks);
        }

        @Override // @mcp: func_241215_a_ = setSelected
        public void func_241215_a_(@Nullable BlockSlot entry) {
            if (entry == null)
                return;

            Minecraft.getInstance().player.closeScreen();
            Minecraft.getInstance().displayGuiScreen(new GuiAddBlock(entry.getBlock().getBlock()));
        }

        void updateEntries(List<BlockWithItemStack> blocks) {
            this.func_230963_j_(); // @mcp: func_230963_j_ = clearEntries
            blocks.forEach(block -> this.func_230513_b_(new BlockSlot(block, this))); // @mcp: func_230513_b_ = addEntry
        }

        public static class BlockSlot extends AbstractList.AbstractListEntry<ScrollingBlockList.BlockSlot> {
            BlockWithItemStack block;
            ScrollingBlockList parent;

            BlockSlot(BlockWithItemStack block, ScrollingBlockList parent) {
                this.block = block;
                this.parent = parent;
            }

            public BlockWithItemStack getBlock() {
                return block;
            }

            @Override // @mcp; render
            public void func_230432_a_(MatrixStack stack, int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean p_194999_5_, float partialTicks) {
                FontRenderer font = this.parent.field_230668_b_.fontRenderer; // @mcp: field_230668_b_ = minecraft

                ResourceLocation resource = this.block.getItemStack().getItem().getRegistryName();
                font.func_238407_a_(stack,this.block.getItemStack().getItem().getName(), left + 30, top + 7, Color.WHITE.getRGB()); // @mcp: func_238407_a_ = drawString
                font.func_238407_a_(stack, ITextProperties.func_240652_a_(resource != null ? resource.getNamespace() : ""), left + 30, top + 17, Color.WHITE.getRGB()); // @mcp: func_238407_a_ = drawString
                // @mcp: func_240652_a_ = unknown... Code recommendation

                RenderHelper.enableStandardItemLighting();
                this.parent.field_230668_b_.getItemRenderer().renderItemAndEffectIntoGUI(this.block.getItemStack(), left + 5, top + 7); // @mcp: field_230668_b_ = minecraft
                RenderHelper.disableStandardItemLighting();
            }

            @Override // @mcp: func_231044_a_ = mouseClicked
            public boolean func_231044_a_(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
                this.parent.func_241215_a_(this); // @mcp: func_241215_a_ = setSelected
                return false;
            }
        }
    }
}
