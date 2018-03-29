package com.xray.client.gui;

import com.xray.client.xray.XrayController;
import com.xray.common.reference.OreInfo;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;

import java.io.IOException;

public class GuiEditOre extends GuiContainer
{
    private GuiTextField oreName;
    private GuiSlider redSlider;
    private GuiSlider greenSlider;
    private GuiSlider blueSlider;
    private OreInfo oreInfo;

    private static final int BUTTON_DELETE = 100;
    private static final int BUTTON_OREDICT = 101;
    private static final int BUTTON_SAVE = 98;
    private static final int BUTTON_CANCEL = 99;

    GuiEditOre(OreInfo oreInfo) {
        super(true); // Has a sidebar
        this.setSideTitle( I18n.format("xray.single.tools") );

        this.oreInfo = OreInfo.duplicate( oreInfo );
    }

    @Override
    public void initGui()
    {
        // Called when the gui should be (re)created
        // Sidebar buttons for now
        this.buttonList.add( new GuiButton( BUTTON_DELETE, (width / 2) + 78, height / 2 - 60, 120, 20, I18n.format("xray.single.delete") ));
        this.buttonList.add( new GuiButton( BUTTON_OREDICT, (width / 2) + 78, height / 2 - 38, 120, 20, I18n.format("xray.input.toggle_oredict") + ": " + (oreInfo.useOredict() ? "On" : "Off") ));

        this.buttonList.add( new GuiButton( BUTTON_SAVE, (width / 2) + 78, height / 2 + 58, 120, 20, I18n.format("xray.single.save") ));

        // Bottom buttons
        this.buttonList.add( new GuiButton( BUTTON_CANCEL, width / 2 - 138, height / 2 + 83, 202, 20, I18n.format("xray.single.cancel") ) ); // Cancel button

        this.buttonList.add( redSlider = new GuiSlider( 3, width / 2 - 138, height / 2 + 7, I18n.format("xray.color.red"), 0, 255 ));
        this.buttonList.add( greenSlider = new GuiSlider( 2, width / 2 - 138, height / 2 + 30, I18n.format("xray.color.green"), 0, 255 ));
        this.buttonList.add( blueSlider = new GuiSlider( 1, width / 2 - 138, height / 2 + 53, I18n.format("xray.color.blue"), 0, 255 ) );

        redSlider.sliderValue   = (float)oreInfo.getColor()[0]/255;
        greenSlider.sliderValue = (float)oreInfo.getColor()[1]/255;
        blueSlider.sliderValue  = (float)oreInfo.getColor()[2]/255;

        oreName = new GuiTextField( 1, this.fontRenderer, width / 2 - 138 ,  height / 2 - 63, 202, 20 );
        oreName.setText(this.oreInfo.getDisplayName());

    }

    @Override
    public void actionPerformed( GuiButton button ) // Called on left click of GuiButton
    {
        switch(button.id)
        {
            case BUTTON_SAVE:
                int[] rgb = {(int)(redSlider.sliderValue * 255), (int)(greenSlider.sliderValue * 255), (int)(blueSlider.sliderValue * 255)};
		oreInfo.setColor( rgb );
                XrayController.searchList.updateOre( oreInfo );

                mc.player.closeScreen();
                mc.displayGuiScreen( new GuiList() );
                break;

            case BUTTON_DELETE:
                XrayController.searchList.removeOre( oreInfo );

                mc.player.closeScreen();
                mc.displayGuiScreen( new GuiList() );
                break;

            case BUTTON_CANCEL:
                mc.player.closeScreen();
                mc.displayGuiScreen( new GuiList() );
                break;

            case BUTTON_OREDICT:
		//XRayController.searchList.toggleOreDictionary( oreInfo );
		oreInfo.toggleOredict();
		button.displayString = I18n.format("xray.input.toggle_oredict") + ": " + (oreInfo.useOredict() ? "On" : "Off");
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
        getFontRender().drawStringWithShadow(oreInfo.getDisplayName(), width / 2 - 138, height / 2 - 90, 0xffffff);

        oreName.drawTextBox();

        GuiAdd.renderPreview(width / 2 - 138, height / 2 - 40, 202, 45, redSlider.sliderValue, greenSlider.sliderValue, blueSlider.sliderValue);

        RenderHelper.enableGUIStandardItemLighting();
        this.itemRender.renderItemAndEffectIntoGUI( oreInfo.getItemStack(), width / 2 + 50, height / 2 - 105 ); // Blocks with no stack will display an empty image. TODO GLDraw image?
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
