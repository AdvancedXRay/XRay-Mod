package com.xray.client.gui.manage;

import com.xray.client.gui.utils.GuiBase;
import com.xray.common.reference.OreInfo;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;

import java.io.IOException;

public class GuiChangeMeta extends GuiBase {

    private OreInfo info;
    private GuiTextField oreMeta;

    private static final int BUTTON_SAVE = 2;
    private static final int BUTTON_CANCEL = 1;

    GuiChangeMeta(OreInfo info) {
        super(false);
        setSize( 218, 108);

        this.info = info;
    }

    @Override
    public void initGui()
    {
        oreMeta = new GuiTextField( 1, this.fontRenderer, width / 2 - 95 ,  height / 2 - 5, 190, 20 );
        oreMeta.setText( "" + info.getMeta() );// TODO temp fix

        this.buttonList.add( new GuiButton( BUTTON_SAVE, (width / 2) - 95, height / 2 + 20, 119, 20, I18n.format("xray.single.save") ));
        this.buttonList.add( new GuiButton( BUTTON_CANCEL, width / 2 + 26, height / 2 + 20, 70, 20, I18n.format("xray.single.cancel") ) ); // Cancel button
    }

    @Override
    public void actionPerformed( GuiButton button ) // Called on left click of GuiButton
    {
        switch(button.id)
        {
            case BUTTON_CANCEL:
                mc.player.closeScreen();
                mc.displayGuiScreen( new GuiEdit( this.info ) );
                break;

            case BUTTON_SAVE: // edit meta
		    /* TODO
                if( !oreMeta.getText().isEmpty() && NumberUtils.isDigits( oreMeta.getText() )) {

                    int newMeta = Integer.valueOf( oreMeta.getText() );

                    // Get all possible blocks and see if our new meta exists
                    NonNullList<ItemStack> tmpStack = NonNullList.create();
                    Block tmpBlock = this.selectedBlock.block;
                    tmpBlock.getSubBlocks(tmpBlock.getCreativeTabToDisplayOn(), tmpStack);

                    // Hacky way to check if our index exists in our subBlocks
                    try {
                        ItemStack stack = tmpStack.get( newMeta );
                        XRayController.searchList.setOreColor( info.getName(), info.color );

                        // Update the selected block so we can give the item back to the edit UI
                        //this.info.meta = newMeta; //TODO fix

                        // This could likely just be reconstructed but for now lets just edit the original
                        this.selectedBlock.setBlock( Block.getBlockFromItem( stack.getItemStack() ) );
                        this.selectedBlock.setItem( stack.getItemStack() );
                        this.selectedBlock.setName( stack.getDisplayName() );
                        this.selectedBlock.setItemStack( stack );
                    } catch ( IndexOutOfBoundsException e ) {
                        mc.player.sendMessage( new TextComponentString("[XRay] "+ I18n.format("xray.message.meta_not_supported", oreMeta.getText(), this.selectedBlock.getName()) ));
                    }
                } else
                    mc.player.sendMessage( new TextComponentString("[XRay] "+ I18n.format("xray.message.not_a_number", oreMeta.getText()) ));

                // No matter what close the UI and start again. Otherwise people can't see that an error may have happened
                mc.player.closeScreen();
                mc.displayGuiScreen( new GuiEdit( this.info, this.selectedBlock ) );
*/
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
        getFontRender().drawStringWithShadow(info.getName(), width / 2 - 95, height / 2 - 27, 0xffffff);

        oreMeta.drawTextBox();

        RenderHelper.enableGUIStandardItemLighting();
        this.itemRender.renderItemAndEffectIntoGUI( info.getItemStack(), width / 2 + 79, height / 2 - 40 );
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
