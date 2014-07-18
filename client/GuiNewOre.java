package fgtXray.client;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

public class GuiNewOre extends GuiScreen {
	GuiTextField oreName;
	GuiTextField oreIdent;
	GuiSlider redSlider;
	GuiSlider greenSlider;
	GuiSlider blueSlider;
	GuiButton addButton;
	
	boolean oreNameCleared = false;
	boolean oreIdentCleared = false;
	
	@Override
	public void initGui(){ // Called when the gui should be (re)created.
		this.buttonList.add( new GuiButton( 98, this.width-102, this.height-22, 100, 20, "Add" ) ); // Add button
		this.buttonList.add( new GuiButton( 99, 2, this.height-22, 100, 20, "Cancel" ) ); // Cancel button
		
		this.buttonList.add( new GuiSlider( 1, width / 2 - 102, height / 2 - 60, "§cRed", 0, 255 )  );
		this.buttonList.add( new GuiSlider( 2, width / 2 - 102, height / 2 - 40, "§2Green", 0, 255 )  );
		this.buttonList.add( new GuiSlider( 3, width / 2 - 102, height / 2 - 20, "§9Blue", 0, 255 )  );
		
		for( int i = 0; i < buttonList.size(); i++ ){
			GuiButton btn = (GuiButton)buttonList.get( i );
			switch( btn.id ){
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
		redSlider.sliderValue = 0.0F;
		greenSlider.sliderValue = 1.0F;
		blueSlider.sliderValue = 0.0F;
		
		oreName = new GuiTextField( this.fontRendererObj, width / 2 - 100, height / 2 + 5, 210, 20 );
		oreIdent = new GuiTextField( this.fontRendererObj, width / 2 - 100, height / 2 + 30, 210, 20 );
		oreName.setText( "Name of block.");
		oreIdent.setText( "id:meta" ); // TODO: oreName
	}
	
	@Override
	public void actionPerformed( GuiButton button ){ // Called on left click of GuiButton
	  switch(button.id){
		case 98: // Add
			int color = (int)(redSlider.sliderValue * 255);
			color = (color<<8) + (int)(greenSlider.sliderValue * 255);
			color = (color<<8) + (int)(blueSlider.sliderValue * 255);
			
			OresSearch.add( oreIdent.getText(), oreName.getText(), color );
			mc.thePlayer.closeScreen();
			break;
			
		case 99: // Cancel
			mc.thePlayer.closeScreen();
			mc.displayGuiScreen( new OldGuiSettings() );
			break;
			
		default:
			break;
	  	}
		//this.initGui(); // Redraw the gui.
	}
	
	@Override
	protected void keyTyped( char par1, int par2 ){ // par1 is char typed, par2 is ascii hex (tab=15 return=28)
		//System.out.println( String.format( "keyTyped: %c : %d", par1, par2 ) );
		super.keyTyped( par1, par2 );
		if( oreName.isFocused() ){
			oreName.textboxKeyTyped( par1, par2 );
			if( par2 == 15 ){
				oreName.setFocused( false );
				if( !oreIdentCleared ){
					oreIdent.setText("");
				}
				oreIdent.setFocused( true );
			}
			
		} else if( oreIdent.isFocused() ){
			oreIdent.textboxKeyTyped( par1, par2 );
			if( par2 == 28 ){
				this.actionPerformed( addButton );
			}
			
		} else {
			switch( par2 ){
				case 15: // Change focus to oreName on focus-less tab
					if( !oreNameCleared ){
						oreName.setText("");
					}
					oreName.setFocused( true );
					break;
				case 1: // Exit on escape
					mc.thePlayer.closeScreen();
				default:
					break;
			}
		}
	}
	
	@Override
	public boolean doesGuiPauseGame(){ // Dont pause the game in single player.
		return false;
	}
	
	@Override
	public void updateScreen(){
		oreName.updateCursorCounter();
		oreIdent.updateCursorCounter();
	}
	
	@Override
	public void drawScreen( int x, int y, float f ){
		drawDefaultBackground();  // Draws the opaque black background
		oreName.drawTextBox();
		oreIdent.drawTextBox();
		super.drawScreen(x, y, f);
		
		
		
		GL11.glDisable( GL11.GL_TEXTURE_2D );
		GL11.glEnable( GL11.GL_BLEND );
		GL11.glDepthMask( false );
		GL11.glBlendFunc( GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA );
		
		GL11.glBegin( GL11.GL_QUADS );
		GL11.glColor3f( redSlider.sliderValue, greenSlider.sliderValue, blueSlider.sliderValue );
			GL11.glVertex2d( width / 2 + 48, height / 2 - 60 ); // TL
			GL11.glVertex2d( width / 2 + 48, height / 2 - 0 ); // BL
			GL11.glVertex2d( width / 2 + 110, height / 2 - 0 ); // BR
			GL11.glVertex2d( width / 2 + 110, height / 2 - 60 ); // TR
		GL11.glEnd();
		
		GL11.glDepthMask( true );
		GL11.glDisable( GL11.GL_BLEND );
	}
	
	@Override
	public void mouseClicked( int x, int y, int mouse ){
		super.mouseClicked( x, y, mouse );
		oreName.mouseClicked( x, y, mouse );
		oreIdent.mouseClicked( x, y, mouse );
		
		if( oreName.isFocused() && !oreNameCleared ){
			oreName.setText( "" );
			oreNameCleared = true;
		}
		if( oreIdent.isFocused() && !oreIdentCleared ){
			oreIdent.setText( "" );
			oreIdentCleared = true;
		}
		
		if( mouse == 1 ){ // Right clicked
			for( int i = 0; i < this.buttonList.size(); i++ ){
				GuiButton button = (GuiButton)this.buttonList.get( i );
				if( button.func_146115_a() ){ //func_146115_a() returns true if the button is being hovered
					/*if( button.id == 99 ){
					}*/
				}
			}
		}
		
		//System.out.println( String.format( "mouseClicked( %d, %d, %d )", x, y, mouse ) );
		//this.initGui();
	}
}
