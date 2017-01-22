package com.fgtXray.client.gui;

import com.fgtXray.client.OresSearch;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

public class GuiNewOre extends GuiScreen {
	GuiTextField oreName;
	GuiTextField oreIdent;
	GuiSlider redSlider;
	GuiSlider greenSlider;
	GuiSlider blueSlider;
	GuiButton addButton;

	boolean oreNameCleared  = false;
	boolean oreIdentCleared = false;
	
	@Override
	public void initGui()
    {
        // Called when the gui should be (re)created
		this.buttonList.add( new GuiButton( 98, width / 2 + 5, height / 2 + 58, 108, 20, "Add" ) ); // Add button
		this.buttonList.add( new GuiButton( 99, width / 2 - 108, height / 2 + 58, 108, 20, "Cancel" ) ); // Cancel button

        //this.buttonList.add( new GuiButton( 8, width / 2 - 102, height / 2 + 80, 105, 20, "test" ) ); // Cancel button

		this.buttonList.add( new GuiSlider( 1, width / 2 - 108, height / 2 - 63, "Red", 0, 255 )  );
		this.buttonList.add( new GuiSlider( 2, width / 2 - 108, height / 2 - 40, "Green", 0, 255 )  );
		this.buttonList.add( new GuiSlider( 3, width / 2 - 108, height / 2 - 17, "Blue", 0, 255 )  );
		
		for( int i = 0; i < buttonList.size(); i++ )
		{
			GuiButton btn = (GuiButton)buttonList.get( i );
			switch( btn.id )
			{
				case 1: // Red slider
					redSlider = (GuiSlider)btn;
					break;
				case 2: // Green slider
					greenSlider = (GuiSlider)btn;
					break;
				case 3: // Blue slider
					blueSlider = (GuiSlider)btn;
					break;
				case 98: // Add button
					addButton = btn;
					break;
				default:
					break;
			}
		}
		redSlider.sliderValue   = 0.0F;
		greenSlider.sliderValue = 1.0F;
		blueSlider.sliderValue  = 0.0F;
		
		oreName = new GuiTextField( 1, this.fontRendererObj, width / 2 - 108, height / 2 + 8, 220, 20 );
		oreIdent = new GuiTextField( 0, this.fontRendererObj, width / 2 - 108, height / 2 + 32, 220, 20 );
		oreName.setText( "Name of block");
		oreIdent.setText( "ID:META" ); // TODO: oreName
	}
	
	@Override
	public void actionPerformed( GuiButton button ) // Called on left click of GuiButton
	{
		switch(button.id)
		{
			case 98: // Add
				int[] rgb = {(int)(redSlider.sliderValue * 255), (int)(greenSlider.sliderValue * 255), (int)(blueSlider.sliderValue * 255)};

				OresSearch.add(oreIdent.getText(), oreName.getText(), rgb);
				
				mc.thePlayer.closeScreen();
				mc.displayGuiScreen( new GuiSettings() );
				break;
						
			case 99: // Cancel
				mc.thePlayer.closeScreen();
				mc.displayGuiScreen( new GuiSettings() );
				break;
				
			default:
				break;
		}
	}
	
	@Override
	protected void keyTyped( char par1, int par2 ) // par1 is char typed, par2 is ascii hex (tab=15 return=28)
	{
		//System.out.println( String.format( "keyTyped: %c : %d", par1, par2 ) );
		try {
			super.keyTyped( par1, par2 );
		} catch (IOException e) {
			e.printStackTrace();
		}
		if( oreName.isFocused() )
		{
			oreName.textboxKeyTyped( par1, par2 );
			if( par2 == 15 )
			{
				oreName.setFocused( false );
				if( !oreIdentCleared )
				{
					oreIdent.setText("");
				}
				oreIdent.setFocused( true );
			}
			
		}
		else if( oreIdent.isFocused() )
		{
			oreIdent.textboxKeyTyped( par1, par2 );
			if( par2 == 28 )
			{
				this.actionPerformed( addButton );
			}
			
		}
		else
		{
			switch( par2 )
			{
				case 15: // Change focus to oreName on focus-less tab
					if( !oreNameCleared )
					{
						oreName.setText("");
					}
					oreName.setFocused( true );
					break;
				case 1: // Exit on escape
                    mc.displayGuiScreen( new GuiSettings() );
					mc.thePlayer.closeScreen();
				default:
					break;
			}
		}
	}
	
	@Override
	public boolean doesGuiPauseGame() // Dont pause the game in single player.
	{
		return false;
	}
	
	@Override
	public void updateScreen()
    {
		oreName.updateCursorCounter();
		oreIdent.updateCursorCounter();
	}

	@Override
	public void drawScreen( int x, int y, float f )
    {
        drawDefaultBackground();
        mc.renderEngine.bindTexture( new ResourceLocation("fgtxray:textures/gui/oreAddBackground.png") );
        drawTexturedModalRect(width / 2 - 125, height / 2 - 95, 0, 0, 256, 205);

        FontRenderer fr = this.mc.fontRendererObj;
        fr.drawString("Add an Ore", width / 2 - 108, height / 2 - 80, 0x404040);

		oreName.drawTextBox();
		oreIdent.drawTextBox();

		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer vertexbuffer = tessellator.getBuffer();
		GlStateManager.enableBlend();
		GlStateManager.disableTexture2D();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.color(redSlider.sliderValue, greenSlider.sliderValue, blueSlider.sliderValue, 1);
		vertexbuffer.begin(7, DefaultVertexFormats.POSITION);
		vertexbuffer.pos(width / 2 + 46, height / 2 - 63, 0.0D).endVertex();
		vertexbuffer.pos(width / 2 + 46, height / 2 + 3, 0.0D).endVertex();
		vertexbuffer.pos(width / 2 + 113, height / 2 + 3, 0.0D).endVertex();
		vertexbuffer.pos(width / 2 + 113, height / 2 - 63, 0.0D).endVertex();
		tessellator.draw();
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();

		super.drawScreen(x, y, f);
        // new
        // I want to render the item here but i am unsure on how to
        // do it so i am leaving it for now. :)
        // RenderItem renderItem = new RenderItem();
        // IIcon icon = net.minecraft.block.Block.getBlockById(3).getIcon( 1, 2 );
        // renderItem.renderIcon(50, 50, icon, 16, 16);
	}
	
	@Override
	public void mouseClicked( int x, int y, int mouse )
	{
		try {
			super.mouseClicked( x, y, mouse );
		} catch (IOException e) {
			e.printStackTrace();
		}
		oreName.mouseClicked( x, y, mouse );
		oreIdent.mouseClicked( x, y, mouse );
		
		if( oreName.isFocused() && !oreNameCleared )
		{
			oreName.setText( "" );
			oreNameCleared = true;
		}
		if( oreIdent.isFocused() && !oreIdentCleared )
		{
			oreIdent.setText( "" );
			oreIdentCleared = true;
		}

        // TODO: fix bug where if you type then remove it the text will not be put back.
        if( !oreName.isFocused() && oreNameCleared && oreName.getText() == "" )
        {
            oreNameCleared = false;
            oreName.setText( "Name of block");
        }

        if( !oreIdent.isFocused() && oreIdentCleared && oreIdent.getText() == "" )
        {
            oreIdentCleared = false;
            oreIdent.setText( "ID:META");
        }
//
//		if( mouse == 1 ) // Right clicked
//		{
//			for( int i = 0; i < this.buttonList.size(); i++ )
//			{
//				GuiButton button = (GuiButton)this.buttonList.get( i );
//				if( button.func_146115_a() ) //func_146115_a() returns true if the button is being hovered
//				{
//					/*if( button.id == 99 ){
//					}*/
//				}
//			}
//		}
	}
}
