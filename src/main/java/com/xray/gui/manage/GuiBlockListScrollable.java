package com.xray.gui.manage;

import com.xray.XRay;
import com.xray.gui.utils.GuiBase;
import com.xray.reference.block.BlockItem;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;

import java.util.ArrayList;

public class GuiBlockListScrollable extends GuiBase {
    private GuiBlocksList blockList;
    private ArrayList<BlockItem> blocks;
    private TextFieldWidget search;
    private String lastSearched = "";
    private int selected = -1;

    public GuiBlockListScrollable() {
        super(false);
        this.blocks = XRay.gameBlockStore.getStore();
    }

    boolean blockSelected(int index) {
        return index == this.selected;
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
        this.blockList = new GuiBlocksList( this, this.blocks );

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

//    @Override
//    public void updateScreen()
//    {
//        search.updateCursorCounter();
//
//        if(!search.getText().equals(lastSearched))
//            reloadBlocks();
//    }

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

//        this.blockList.( x,  y,  f );
    }

    @Override
    public boolean mouseClicked( double x, double y, int button ) {
        search.mouseClicked(x, y, button);
        return super.mouseClicked(x, y, button);
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
