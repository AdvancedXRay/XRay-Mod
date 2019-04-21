package com.xray.utils;

import com.xray.reference.block.BlockInfo;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

/**
 * Created by MiKeY on 29/12/17.
 */
public class Utils {

    /**
     * Shortcut to display a message to the player
     *
     * @param player Minecraft Player
     * @param message String Message
     */
    public static void sendMessage(EntityPlayerSP player, String message) {
        player.sendMessage( new TextComponentString(message) );
    }

    /**
     * Lazy function to auto fill some pars for getStateForPlacement
     *
     * @param world - mc world
     * @param player - mc player
     * @param stack - ItemStack
     * @return IBlockState from {@link Block#getStateForPlacement(World, BlockPos, EnumFacing, float, float, float, int, EntityLivingBase, EnumHand)}
     */
    public static IBlockState getStateFromPlacement(World world, EntityLivingBase player, ItemStack stack) {
        return Block.getBlockFromItem(stack.getItem()).getStateForPlacement(
            world, player.getPosition(), EnumFacing.NORTH, 0.1f, 0.1f, 0.1f, stack.getMetadata(), player, player.getActiveHand()
        );
    }

    public static int clampColor(int c)
    {
        return c < 0 ? 0 : c > 255 ? 255 : c;
    }

    /**
     * Renders a bounding box around a specific block.
     * Could be done better and should use {@link net.minecraft.util.math.AxisAlignedBB#AxisAlignedBB(BlockPos)}
     * logically...
     *
     * @param buffer render buffer
     * @param b Block Information
     * @param opacity Opacity of the outlines
     */
    public static void renderBlockBounding(BufferBuilder buffer, BlockInfo b, int opacity) {
        if( b == null )
            return;

        final float size = 1.0f;
        int red = b.color[0];
        int green = b.color[1];
        int blue = b.color[2];
        int x = b.getX();
        int y = b.getY();
        int z = b.getZ();

        // TOP
        buffer.pos(x, y + size, z).color(red, green, blue, opacity).endVertex();
        buffer.pos(x + size, y + size, z).color(red, green, blue, opacity).endVertex();
        buffer.pos(x + size, y + size, z).color(red, green, blue, opacity).endVertex();
        buffer.pos(x + size, y + size, z + size).color(red, green, blue, opacity).endVertex();
        buffer.pos(x + size, y + size, z + size).color(red, green, blue, opacity).endVertex();
        buffer.pos(x, y + size, z + size).color(red, green, blue, opacity).endVertex();
        buffer.pos(x, y + size, z + size).color(red, green, blue, opacity).endVertex();
        buffer.pos(x, y + size, z).color(red, green, blue, opacity).endVertex();

        // BOTTOM
        buffer.pos(x + size, y, z).color(red, green, blue, opacity).endVertex();
        buffer.pos(x + size, y, z + size).color(red, green, blue, opacity).endVertex();
        buffer.pos(x + size, y, z + size).color(red, green, blue, opacity).endVertex();
        buffer.pos(x, y, z + size).color(red, green, blue, opacity).endVertex();
        buffer.pos(x, y, z + size).color(red, green, blue, opacity).endVertex();
        buffer.pos(x, y, z).color(red, green, blue, opacity).endVertex();
        buffer.pos(x, y, z).color(red, green, blue, opacity).endVertex();
        buffer.pos(x + size, y, z).color(red, green, blue, opacity).endVertex();

        // Edge 1
        buffer.pos(x + size, y, z + size).color(red, green, blue, opacity).endVertex();
        buffer.pos(x + size, y + size, z + size).color(red, green, blue, opacity).endVertex();

        // Edge 2
        buffer.pos(x + size, y, z).color(red, green, blue, opacity).endVertex();
        buffer.pos(x + size, y + size, z).color(red, green, blue, opacity).endVertex();

        // Edge 3
        buffer.pos(x, y, z + size).color(red, green, blue, opacity).endVertex();
        buffer.pos(x, y + size, z + size).color(red, green, blue, opacity).endVertex();

        // Edge 4
        buffer.pos(x, y, z).color(red, green, blue, opacity).endVertex();
        buffer.pos(x, y + size, z).color(red, green, blue, opacity).endVertex();
    }
}
