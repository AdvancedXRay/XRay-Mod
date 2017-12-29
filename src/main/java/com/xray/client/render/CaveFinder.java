package com.xray.client.render;

import com.xray.common.XRay;
import com.xray.common.reference.BlockInfo;
import com.xray.common.reference.OreInfo;
import com.xray.common.utils.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.Sys;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CaveFinder implements Runnable
{
    private static final Minecraft mc = Minecraft.getMinecraft();
	private static long nextTimeMs = System.currentTimeMillis();
	private static final int delayMs = 200;
	private Thread thread = null;

	public static List<BlockInfo> caveBlocks = new ArrayList<>();

	@SubscribeEvent
	public void tickEnd( TickEvent.ClientTickEvent event )
	{
		if ( (event.phase == TickEvent.Phase.END) && (mc.player != null) )
		{
			// If we're in a world and want to start drawing create the thread.
			if( XRay.drawCaves && ((this.thread == null) || !this.thread.isAlive()) && ( (mc.world != null) && (mc.player != null) ) )
			{
				this.thread = new Thread( this );
				this.thread.setDaemon( false );
				this.thread.setPriority( Thread.MAX_PRIORITY );
				this.thread.start();
			}
		}
	}


	@Override
	public void run() // Our thread code for finding ores near the player.
	{
		try
		{
			while( !this.thread.isInterrupted() ) // Check the internal interrupt flag. Exit thread if set.
			{
				if( !blockFinder() )
                    this.thread.interrupt(); // Kill the thread if we turn off xray or the player/world object becomes null.
			}
			//System.out.println(" --- Thread Exited Cleanly! ");
			this.thread = null;
		}
		catch ( Exception exc )
		{
			System.out.println(" ClientTick Thread Interrupted!!! " + exc.toString() ); // This shouldnt get called.
		}
	}

	private static boolean blockFinder() {
		int radius = 50; // Get the radius around the player to search.

		if (XRay.drawCaves && mc.world != null && mc.player != null)
		{
			if ( nextTimeMs > System.currentTimeMillis() ) // Delay to avoid spamming ore updates.
				return true;

			int px = XRay.localPlyX;
			int py = XRay.localPlyY;
			int pz = XRay.localPlyZ;

			List<BlockInfo> temp = new ArrayList<>();
			for (int y = Math.max( 0, py - 96 ); y < py - 15; y++) // Check the y axis. from 0 or the players y-96 to the players y + 32
			{
				for (int x = px - radius; x < px + radius; x++) // Iterate the x axis around the player in radius.
				{
					for (int z = pz - radius; z < pz + radius; z++) // z axis.
					{
						IBlockState state = mc.world.getBlockState(  new BlockPos(x, y, z) );

						Block thisBlock = state.getBlock();
						if(!Objects.equals(thisBlock.getLocalizedName(), "Air"))
							continue;

						temp.add( new BlockInfo(x, y, z, new int[]{0, 0, 0}));
					}
				}
			}

			caveBlocks.clear();
			caveBlocks.addAll(temp);

			nextTimeMs = System.currentTimeMillis() + delayMs; // Update the delay.
		}
		else
		    return false;

		return true;
	}
}
