package com.xray.client.gui;

import com.xray.client.OresSearch;
import com.xray.client.gui.helper.HelperBlock;
import com.xray.common.reference.OreInfo;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
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
        super(true); // Has a sidebar
        this.oreInfo = oreInfo;

        // Get the block for the ore info
        NonNullList<ItemStack> tmpStack = NonNullList.create();
        Block tmpBlock = Block.getBlockById(oreInfo.getId());
        tmpBlock.getSubBlocks(tmpBlock.getCreativeTabToDisplayOn(), tmpStack);
        ItemStack stack = tmpStack.get( oreInfo.getMeta() );

        this.selectBlock = new HelperBlock(
                stack.isEmpty() ? oreInfo.getDisplayName() : stack.getDisplayName(), Block.getBlockFromItem( stack.getItem() ), stack, stack.getItem(), stack.getItem().getRegistryName()
        );
    }

    @Override
    public void initGui()
    {
        // Called when the gui should be (re)created
        // Sidebar buttons for now
        this.buttonList.add( new GuiButton( 98, width / 2 +16, height / 2 + 86, 120, 20, I18n.format("xray.single.save") ));
        this.buttonList.add( new GuiButton( 100, width / 2 -26, height / 2 + 86, 120, 20, I18n.format("xray.single.delete") ));
        this.buttonList.add( new GuiButton( 99, width / 2 - 100, height / 2 + 86, 120, 20, I18n.format("xray.single.cancel") ) ); // Cancel button


        this.buttonList.add( redSlider = new GuiSlider( 3, width / 2 - 140, height / 2 + 7, I18n.format("xray.color.red"), 0, 255 ));
        this.buttonList.add( greenSlider = new GuiSlider( 2, width / 2 - 140, height / 2 + 30, I18n.format("xray.color.green"), 0, 255 ));
        this.buttonList.add( blueSlider = new GuiSlider( 1, width / 2 - 140, height / 2 + 53, I18n.format("xray.color.blue"), 0, 255 ) );

        redSlider.sliderValue   = (float)oreInfo.color[0]/255;
        greenSlider.sliderValue = (float)oreInfo.color[1]/255;
        blueSlider.sliderValue  = (float)oreInfo.color[2]/255;

        oreName = new GuiTextField( 1, this.fontRenderer, width / 2 - 138 ,  height / 2 - 63, 202, 20 );
        oreName.setText(this.oreInfo.getDisplayName());

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

            case 100:
                OresSearch.remove(this.oreInfo);

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
        getFontRender().drawStringWithShadow(selectBlock.getName(), width / 2 - 138, height / 2 - 90, 0xffffff);

        oreName.drawTextBox();

        GuiAdd.renderPreview(width / 2 - 138, height / 2 - 40, 203, 45, redSlider.sliderValue, greenSlider.sliderValue, blueSlider.sliderValue);

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
        return I18n.format("xray.title.edit");
    }
}
