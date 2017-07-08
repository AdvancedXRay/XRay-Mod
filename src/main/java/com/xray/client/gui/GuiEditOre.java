package com.xray.client.gui;

import com.xray.client.OresSearch;
import com.xray.client.gui.helper.HelperBlock;
import com.xray.common.reference.OreInfo;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import java.io.IOException;

public class GuiEditOre extends GuiContainer
{
    private GuiTextField oreName;
    private GuiSlider redSlider;
    private GuiSlider greenSlider;
    private GuiSlider blueSlider;
    private HelperBlock selectBlock;
    private OreInfo oreInfo;

    GuiEditOre(OreInfo oreInfo) {
        this.oreInfo = oreInfo;

        NonNullList<ItemStack> tmpStack = NonNullList.create();
        Block tmpBlock = Block.getBlockById(oreInfo.getId());
        tmpBlock.getSubBlocks(tmpBlock.getCreativeTabToDisplayOn(), tmpStack);
        ItemStack stack = tmpStack.get( oreInfo.getMeta() );

        this.selectBlock = new HelperBlock(
                stack.getDisplayName(), Block.getBlockFromItem( stack.getItem() ), stack, stack.getItem(), stack.getItem().getRegistryName()
        );
    }

    @Override
    public void initGui()
    {
        // Called when the gui should be (re)created
        this.buttonList.add( new GuiButton( 98, width / 2 +16, height / 2 + 86, 90, 20, "Save" ));
        this.buttonList.add( new GuiButton( 100, width / 2 -26, height / 2 + 86, 40, 20, "Delete" ));

        this.buttonList.add( redSlider = new GuiSlider( 3, width / 2 - 97, height / 2 + 7, "Red", 0, 255 ));
        this.buttonList.add( greenSlider = new GuiSlider( 2, width / 2 - 97, height / 2 + 30, "Green", 0, 255 ));
        this.buttonList.add( blueSlider = new GuiSlider( 1, width / 2 - 97, height / 2 + 53, "Blue", 0, 255 ) );

        redSlider.sliderValue   = (float)oreInfo.color[0]/255;
        greenSlider.sliderValue = (float)oreInfo.color[1]/255;
        blueSlider.sliderValue  = (float)oreInfo.color[2]/255;

        oreName = new GuiTextField( 1, this.fontRenderer, width / 2 - 97 ,  height / 2 - 63, 202, 20 );
        oreName.setText(this.oreInfo.getOreName());

        this.buttonList.add( new GuiButton( 99, width / 2 - 100, height / 2 + 86, 72, 20, "Cancel" ) ); // Cancel button
    }

    @Override
    public void actionPerformed( GuiButton button ) // Called on left click of GuiButton
    {
        switch(button.id)
        {
            case 98:
                int[] rgb = {(int)(redSlider.sliderValue * 255), (int)(greenSlider.sliderValue * 255), (int)(blueSlider.sliderValue * 255)};

                OresSearch.update(this.oreInfo, oreName.getText(), rgb);

                mc.player.closeScreen();
                mc.displayGuiScreen( new GuiList() );
                break;

            case 99: // Cancel
                mc.player.closeScreen();
                mc.displayGuiScreen( new GuiList() );
                break;

            default:
                break;
        }
    }

    @Override
    protected void keyTyped( char par1, int par2 ) throws IOException // par1 is char typed, par2 is ascii hex (tab=15 return=28)
    {
        super.keyTyped( par1, par2 );

        if( oreName.isFocused() )
            oreName.textboxKeyTyped( par1, par2 );
    }

    @Override
    public void updateScreen()
    {
        oreName.updateCursorCounter();
    }

    @Override
    public void drawScreen( int x, int y, float f )
    {
        super.drawScreen(x, y, f);
        getFontRender().drawStringWithShadow(selectBlock.getName(), width / 2 - 97, height / 2 - 90, 0xffffff);

        oreName.drawTextBox();

        GuiAdd.renderPreview(width, height, redSlider.sliderValue, greenSlider.sliderValue, blueSlider.sliderValue);

        RenderHelper.enableGUIStandardItemLighting();
        this.itemRender.renderItemAndEffectIntoGUI( selectBlock.getItemStack(), width / 2 + 88, height / 2 - 105 );
        RenderHelper.disableStandardItemLighting();
    }

    @Override
    public void mouseClicked( int x, int y, int mouse ) throws IOException
    {
        super.mouseClicked( x, y, mouse );
        oreName.mouseClicked( x, y, mouse );
    }

    @Override
    public boolean hasTitle() {
        return true;
    }

    @Override
    public String title() {
        return "Edit Block";
    }
}
