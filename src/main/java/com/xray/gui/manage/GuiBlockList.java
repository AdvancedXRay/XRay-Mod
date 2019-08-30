package com.xray.gui.manage;

import com.xray.XRay;
import com.xray.gui.utils.AbstractScrollingList;
import com.xray.gui.utils.GuiBase;
import com.xray.reference.block.BlockItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GuiBlockList extends GuiBase {
    private AbstractScrollingList blockList;
    private ArrayList<BlockItem> blocks;
    private TextFieldWidget search;
    private String lastSearched = "";
    private int selected = -1;

    public GuiBlockList() {
        super(false);
        this.blocks = XRay.gameBlockStore.getStore();
    }

    void selectBlock(int index)
    {
        if (index == this.selected)
            return;

        this.selected = index;
        getMinecraft().player.closeScreen();
        getMinecraft().displayGuiScreen( new GuiAddBlock( blocks.get( this.selected ), null ) );
    }

    @Override
    public void init() {
        this.blockList = new ScrollableList(getMinecraft(), 202, 185, height / 2 - 105, (height / 2 - 105) + 185, width / 2 - 100, 30, this.blocks);
        this.children.add(this.blockList);

        search = new TextFieldWidget(getFontRender(), width / 2 - 100, height / 2 + 85, 140, 18, "");
        search.changeFocus(true);
        search.setCanLoseFocus(true);

        addButton( new Button( width / 2 +43, height / 2 + 84, 60, 20, I18n.format("xray.single.cancel"), b -> this.onClose()) );
    }


    @Override
    public boolean charTyped(char keyTyped, int __unknown) {
        search.charTyped(keyTyped, __unknown);

        return super.charTyped(keyTyped, __unknown);
    }

    static class ScrollableList extends AbstractScrollingList {
        ScrollableList(Minecraft client, int width, int height, int top, int bottom, int left, int entryHeight, List<BlockItem> blocks) {
            super(client, width, height, top, bottom, left, entryHeight);

            blocks.forEach(e -> {
                if( e == null )
                    return;

                addEntry(new Entry(e));
            });
        }

        @Override
        protected boolean isSelected(int index) {
            return false;
        }

        private static class Entry extends AbstractScrollingList.AbstractListEntry {
            BlockItem data;

            public Entry(BlockItem data) {
                this.data = data;
            }

            @Override
            public void render(int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean p_194999_5_, float partialTicks) {
                FontRenderer font = Minecraft.getInstance().fontRenderer;

                ResourceLocation domain = data.getItemStack().getItem().getRegistryName();
                if( domain == null )
                    domain = new ResourceLocation("");

                font.drawString(data.getItemStack().getDisplayName().getFormattedText(), left + 30, top + 7, 0xFFFFFF);
                font.drawString(domain.getNamespace(), left + 30, top + 17, 0xFFFFFF);

                RenderHelper.enableGUIStandardItemLighting();
                Minecraft.getInstance().getItemRenderer().renderItemAndEffectIntoGUI(data.getItemStack(), left + 5, top + 7);
                RenderHelper.disableStandardItemLighting();
            }
        }
    }

    @Override
    public void tick() {
        search.tick();
        super.tick();
    }

    private void reloadBlocks() {
        blocks = new ArrayList<>();
        ArrayList<BlockItem> tmpBlocks = new ArrayList<>();
        for( BlockItem block : XRay.gameBlockStore.getStore() ) {
            if( block.getItemStack().getDisplayName().getFormattedText().toLowerCase().contains(search.getText().toLowerCase()) )
                tmpBlocks.add(block);
        }
        blocks = tmpBlocks;
//        this.blockList.updateBlockList( blocks );
        lastSearched = search.getText();
    }

    @Override
    public void render( int x, int y, float f )
    {
        super.render(x, y, f);
        search.render(x, y, f);
        blockList.render(x, y, f);
    }

    @Override
    public boolean mouseClicked( double x, double y, int button ) {
        search.mouseClicked(x, y, button);
        return super.mouseClicked(x, y, button);
    }

    @Override
    public boolean mouseScrolled(double p_mouseScrolled_1_, double p_mouseScrolled_3_, double p_mouseScrolled_5_) {
        blockList.mouseScrolled(p_mouseScrolled_1_, p_mouseScrolled_3_, p_mouseScrolled_5_);
        return super.mouseScrolled(p_mouseScrolled_1_ , p_mouseScrolled_3_ , p_mouseScrolled_5_);
    }

    @Override
    public boolean mouseDragged(double p_mouseDragged_1_, double p_mouseDragged_3_, int p_mouseDragged_5_, double p_mouseDragged_6_, double p_mouseDragged_8_) {
        blockList.mouseDragged(p_mouseDragged_1_, p_mouseDragged_3_, p_mouseDragged_5_, p_mouseDragged_6_, p_mouseDragged_8_);
        return false;
    }

    //    @Override
//    public void handleMouseInput() throws IOException {
//        super.handleMouseInput();
//
//        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
//        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
//        this.blockList.handleMouseInput(mouseX, mouseY);
//    }
}
