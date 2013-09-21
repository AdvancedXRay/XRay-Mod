package fgtXray.client;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraftforge.oredict.OreDictionary;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import fgtXray.BlockInfo;
import fgtXray.FgtXRay;
import fgtXray.OreInfo;

public class ClientTick implements ITickHandler, Runnable {
	private final Minecraft mc = Minecraft.getMinecraft();
	private long nextTimeMs = System.currentTimeMillis();
	private final int delayMs = 200;
	private Thread thread = null;
	
	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
		if ( type.contains(TickType.PLAYER) ){
			EntityPlayer player = (EntityPlayer)tickData[0];
			FgtXRay.localPlyX = MathHelper.floor_double(player.posX); // Set the x,y,z variables to be used by the thread later.
			FgtXRay.localPlyY = MathHelper.floor_double(player.posY);
			FgtXRay.localPlyZ = MathHelper.floor_double(player.posZ);

			if( FgtXRay.drawOres && ((this.thread == null) || !this.thread.isAlive()) &&
			( (mc.theWorld != null) && (mc.thePlayer != null) ) ){ // If we're in a world and want to start drawing create the thread.
				//System.out.println(" --- Starting New Thread!!! ");
				this.thread = new Thread( this );
				this.thread.setDaemon(false);
				this.thread.setPriority( Thread.MAX_PRIORITY );
				this.thread.start();
			}
		}
	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.PLAYER);
	}

	@Override
	public String getLabel() {
		return "clienttick";
	}

	@Override
	public void run() { // Our thread code for finding ores near the player.
		try{
			while(!this.thread.isInterrupted()){ // Check the internal interrupt flag. Exit thread if set.
				if ( FgtXRay.drawOres && !OresSearch.searchList.isEmpty() && (mc.theWorld != null) && (mc.thePlayer != null) ){
					if ( nextTimeMs > System.currentTimeMillis() ) { continue; } // Delay to avoid spamming ore updates.
					List temp = new ArrayList();
					int radius = FgtXRay.distNumbers[ FgtXRay.distIndex ]; // Get the radius around the player to search.
					int px = FgtXRay.localPlyX;
					int py = FgtXRay.localPlyY;
					int pz = FgtXRay.localPlyZ;
					for (int y = Math.max( 0, py - 96 ); y < py + 32; y++){ // Check the y axis. from 0 or the players y-96 to the players y + 32
						for (int x = px - radius; x < px + radius; x++) { // Iterate the x axis around the player in radius.
							for (int z = pz - radius; z < pz + radius; z++) { // z axis.
								int id = mc.theWorld.getBlockId(x, y, z);
								int meta = mc.theWorld.getBlockMetadata(x, y, z);
								
								if(FgtXRay.skipGenericBlocks){ // Dont iterate through the searchList if its a common block.
									if( (id == 0) ||
										(id == Block.stone.blockID) ||
										(id == Block.grass.blockID) ||
										(id == Block.dirt.blockID) ||
										(id == Block.waterStill.blockID) ||
										(id == Block.cobblestone.blockID) ||
										(id == Block.planks.blockID) ||
										(id == Block.sand.blockID) ||
										(id == Block.sandStone.blockID) ||
										(id == Block.gravel.blockID) ||
										(id == Block.bedrock.blockID) ||
										(id == Block.mycelium.blockID) ){
											continue;
									}
									if (mc.theWorld.provider.dimensionId == -1){ // Common Nether blocks.
										if( (id == Block.netherrack.blockID) ||
											(id == Block.netherBrick.blockID) ||
											(id == Block.slowSand.blockID) ||
											(id == Block.lavaStill.blockID) ){
											continue;
										}
									} else if (mc.theWorld.provider.dimensionId == 1){ // Cheese
										if( id == Block.whiteStone.blockID ){
											continue;
										}
									}
								}
								
								for( OreInfo ore : OresSearch.searchList ){ // Now we're actually checking if the current x,y,z block is in our searchList.
									if ((ore.draw) && (id == ore.id) && (meta == ore.meta)){
										temp.add( new BlockInfo( x, y, z, ore.color) ); // Add this block to the temp list
										break; // Found a match, move on to the next block.
									}
								}
							}
						}
					}
					RenderTick.ores.clear();
					RenderTick.ores.addAll(temp); // Add all our found blocks to the RenderTick.ores list. To be use by RenderTick when drawing.
					nextTimeMs = System.currentTimeMillis() + delayMs; // Update the delay.
				} else {
					this.thread.interrupt(); // Kill the thread if we turn off xray or the player/world object becomes null.
				}
			}
			//System.out.println(" --- Thread Exited Cleanly! ");
			this.thread = null;
		} catch ( Exception exc ) {
			System.out.println(" ClientTick Thread Interrupted!!! " + exc.toString() ); // This shouldnt get called.
		}
	}

}
