package com.xray.client.gui;

import com.xray.client.gui.helper.HelperBlock;
import com.xray.common.reference.OreInfo;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import org.apache.commons.lang3.math.NumberUtils;
import org.lwjgl.Sys;

import java.io.IOException;

public class GuiChangeMeta extends GuiContainer {

    private HelperBlock selectedBlock;
    private OreInfo info;
    private GuiTextField oreMeta;

    GuiChangeMeta(HelperBlock selectedBlock, OreInfo info) {
        super(false);
        setSize( 218, 108);

        this.selectedBlock = selectedBlock;
        this.info = info;
    }

    @Override
    public void initGui()
    {
        oreMeta = new GuiTextField( 1, this.fontRenderer, width / 2 - 95 ,  height / 2 - 5, 190, 20 );
        oreMeta.setText( String.valueOf(info.getMeta()) );

        this.buttonList.add( new GuiButton( 2, (width / 2) - 95, height / 2 + 20, 119, 20, I18n.format("xray.single.save") ));
        this.buttonList.add( new GuiButton( 1, width / 2 + 26, height / 2 + 20, 70, 20, I18n.format("xray.single.cancel") ) ); // Cancel button
    }

    @Override
    public void actionPerformed( GuiButton button ) // Called on left click of GuiButton
    {
        switch(button.id)
        {
            case 1: // Cancel
                mc.player.closeScreen();
                mc.displayGuiScreen( new GuiEditOre( this.info, this.selectedBlock ) );
                break;

            case 2: // edit meta
                if( !oreMeta.getText().isEmpty() && NumberUtils.isDigits( oreMeta.getText() )) {

                    int newMeta = Integer.valueOf( oreMeta.getText() );

                    // Get all possible blocks and see if our new meta exists
                    NonNullList<ItemStack> tmpStack = NonNullList.create();
                    Block tmpBlock = this.selectedBlock.block;
                    tmpBlock.getSubBlocks(tmpBlock.getCreativeTabToDisplayOn(), tmpStack);

                    // Hacky way to check if our index exists in our subBlocks
                    try {
                        ItemStack stack = tmpStack.get( newMeta );

                        // Did it work? Cool, Lets move on to update the config and oreList
                        System.out.println("Looks Like we're all good. Lets change it");
                    } catch ( IndexOutOfBoundsException e ) {
                        System.out.println("That index doesn't exist");
                    }
                } else {
                    System.out.println("Fuck");
                }
//
//                mc.player.closeScreen();
//                mc.displayGuiScreen( new GuiEditOre( this.info, this.selectedBlock ) );
                break;

            default:
                break;
        }
    }

    @Override
    protected void keyTyped( char par1, int par2 ) throws IOException // par1 is char typed, par2 is ascii hex (tab=15 return=28)
    {
        super.keyTyped( par1, par2 );

        if( oreMeta.isFocused() )
            oreMeta.textboxKeyTyped( par1, par2 );
    }

    @Override
    public void updateScreen()
    {
        oreMeta.updateCursorCounter();
    }

    @Override
    public void drawScreen( int x, int y, float f )
    {
        super.drawScreen(x, y, f);
        getFontRender().drawStringWithShadow(this.selectedBlock.getName(), width / 2 - 95, height / 2 - 27, 0xffffff);

        oreMeta.drawTextBox();

        RenderHelper.enableGUIStandardItemLighting();
        this.itemRender.renderItemAndEffectIntoGUI( this.selectedBlock.getItemStack(), width / 2 + 79, height / 2 - 40 );
        RenderHelper.disableStandardItemLighting();
    }

    @Override
    public void mouseClicked( int x, int y, int mouse ) throws IOException
    {
        super.mouseClicked( x, y, mouse );
        oreMeta.mouseClicked( x, y, mouse );
    }

    @Override
    public boolean hasTitle() {
        return true;
    }

    @Override
    public String title() {
        return I18n.format("xray.title.edit_meta");
    }
}
