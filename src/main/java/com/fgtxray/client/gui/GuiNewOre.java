package com.fgtxray.client.gui;

import com.fgtxray.client.OresSearch;
import com.fgtxray.reference.Ref;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.Objects;

public class GuiNewOre extends GuiScreen {
	private GuiTextField oreName;
	private GuiTextField oreMeta;
	private GuiTextField oreIdent;
	private GuiSlider redSlider;
	private GuiSlider greenSlider;
	private GuiSlider blueSlider;
	private GuiButton addButton;

	private boolean oreNameCleared  = false;
	private boolean oreIdentCleared = false;
	private boolean oreMetaCleared = false;

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

		for (GuiButton aButtonList : buttonList) {
			switch (aButtonList.id) {
				case 1: // Red slider
					redSlider = (GuiSlider) aButtonList;
					break;
				case 2: // Green slider
					greenSlider = (GuiSlider) aButtonList;
					break;
				case 3: // Blue slider
					blueSlider = (GuiSlider) aButtonList;
					break;
				case 98: // Add button
					addButton = aButtonList;
					break;
				default:
					break;
			}
		}
		redSlider.sliderValue   = 0.0F;
		greenSlider.sliderValue = 1.0F;
		blueSlider.sliderValue  = 0.0F;

		oreName = new GuiTextField( 1, this.fontRenderer, width / 2 - 108, height / 2 + 8, 220, 20 );
		oreIdent = new GuiTextField( 0, this.fontRenderer, width / 2 - 108, height / 2 + 32, 185, 20 );
		oreMeta = new GuiTextField( 3, this.fontRenderer, width / 2 + 82, height / 2 + 32, 30, 20 );
		oreName.setText( "Gui Name");
		oreIdent.setText( "minecraft:grass" );
		oreMeta.setText( "Meta" );
	}

	@Override
	public void actionPerformed( GuiButton button ) // Called on left click of GuiButton
	{
		switch(button.id)
		{
			case 98: // Add
				int[] rgb = {(int)(redSlider.sliderValue * 255), (int)(greenSlider.sliderValue * 255), (int)(blueSlider.sliderValue * 255)};

				OresSearch.add(oreIdent.getText(), oreMeta.getText(), oreName.getText(), rgb);

				mc.player.closeScreen();
				mc.displayGuiScreen( new GuiSettings() );
				break;

			case 99: // Cancel
				mc.player.closeScreen();
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
					oreIdent.setText("");
				oreIdent.setFocused( true );
			}
		}
		else if( oreIdent.isFocused() )
		{
			oreIdent.textboxKeyTyped( par1, par2 );
			if( par2 == 15 )
			{
				oreIdent.setFocused( false );
				if( !oreMetaCleared )
					oreMeta.setText("");
				oreMeta.setFocused( true );
			}
		}
		else if( oreMeta.isFocused() )
		{
			oreMeta.textboxKeyTyped( par1, par2 );
			if( par2 == 28 )
				this.actionPerformed( addButton );
		}
		else
		{
			switch( par2 )
			{
				case 15: // Change focus to oreName on focus-less tab
					if( !oreNameCleared )
						oreName.setText("");
					oreName.setFocused( true );
					break;
				case 1: // Exit on escape
					mc.displayGuiScreen( new GuiSettings() );
					mc.player.closeScreen();
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
		oreMeta.updateCursorCounter();
	}

	@Override
	public void drawScreen( int x, int y, float f )
    {
        drawDefaultBackground();
        mc.renderEngine.bindTexture( new ResourceLocation(Ref.PREFIX_GUI+"addorebg.png") );
        drawTexturedModalRect(width / 2 - 125, height / 2 - 95, 0, 0, 256, 205);

        FontRenderer fr = this.mc.fontRenderer;
        fr.drawString("Add an Ore", width / 2 - 108, height / 2 - 80, 0x404040);

		oreName.drawTextBox();
		oreIdent.drawTextBox();
		oreMeta.drawTextBox();

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder tessellate = tessellator.getBuffer();
		GlStateManager.enableBlend();
		GlStateManager.disableTexture2D();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.color(redSlider.sliderValue, greenSlider.sliderValue, blueSlider.sliderValue, 1);
		tessellate.begin(7, DefaultVertexFormats.POSITION);
		tessellate.pos(width / 2 + 46, height / 2 - 63, 0.0D).endVertex();
		tessellate.pos(width / 2 + 46, height / 2 + 3, 0.0D).endVertex();
		tessellate.pos(width / 2 + 113, height / 2 + 3, 0.0D).endVertex();
		tessellate.pos(width / 2 + 113, height / 2 - 63, 0.0D).endVertex();
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
		oreMeta.mouseClicked( x, y, mouse );

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
		if( oreMeta.isFocused() && !oreMetaCleared )
		{
			oreMeta.setText( "" );
			oreMetaCleared = true;
		}

		if( !oreName.isFocused() && oreNameCleared && Objects.equals(oreName.getText(), ""))
		{
			oreNameCleared = false;
			oreName.setText( "Gui Name");
		}

		if( !oreIdent.isFocused() && oreIdentCleared && Objects.equals(oreIdent.getText(), ""))
		{
			oreIdentCleared = false;
			oreIdent.setText( "minecraft:grass");
		}

		if( !oreMeta.isFocused() && oreMetaCleared && Objects.equals(oreMeta.getText(), ""))
		{
			oreMetaCleared = false;
			oreMeta.setText( "Meta");
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
