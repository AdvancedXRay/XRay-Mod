package com.fgtxray.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

import java.io.IOException;

public class GuiEditOre extends GuiScreen
{
	GuiTextField oreName; // Human readable name
	GuiTextField oreIdent; // oredict oreName or id:meta
	
	@Override
	public void initGui() // Called when the gui should be (re)created.
	{
		this.buttonList.add( new GuiButton( 97, 2, 2, 100, 20, "Delete" ) ); // Delete button
		this.buttonList.add( new GuiButton( 98, this.width-102, this.height-22, 100, 20, "Save" ) ); // Save button
		this.buttonList.add( new GuiButton( 99, 2, this.height-22, 100, 20, "Cancel" ) ); // Cancel button
		oreName = new GuiTextField( 1, this.fontRendererObj, 104, this.height-22, 200, 20 );
	}
	
	@Override
	public void actionPerformed( GuiButton button ) // Called on left click of GuiButton
	{
		switch(button.id)
		{
			case 98: // Save
				break;
		
			case 99: // Cancel
				mc.player.closeScreen();
				break;

			default:
				break;
		}
	}
	
	@Override
	protected void keyTyped( char par1, int par2 )
	{
		try {
			super.keyTyped( par1, par2 );
		} catch (IOException e) {
			e.printStackTrace();
		}
		if( oreName.isFocused() )
		{
			oreName.textboxKeyTyped( par1, par2 );
			
		}
		else if( par2 == 1 ) // Close on esc
		{
			mc.player.closeScreen();
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
	}
	
	@Override
	public void drawScreen( int x, int y, float f )
	{
		drawDefaultBackground();  // Draws the opaque black background
		oreName.drawTextBox();
		super.drawScreen(x, y, f);
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
		if( mouse == 1 ) // Right clicked
		{
			for( int i = 0; i < this.buttonList.size(); i++ )
			{
				GuiButton button = (GuiButton)this.buttonList.get( i );
				if( button.isMouseOver() ) //func_146115_a() returns true if the button is being hovered
				{
					/* TODO: Allow editing of ores
					 * if( button.id == 99 ){ */
				}
			}
		}
	}
}
