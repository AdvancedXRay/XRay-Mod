package com.xray.common.helper;

import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Created by MiKeY on 08/07/17.
 */
public final class ItemStackHelper {

    public static boolean isEmpty( ItemStack stack ) {
        if ( stack.getItem() != null && stack.getItem() != Item.getItemFromBlock(Blocks.AIR ) )
        {
            if( stack.stackSize <= 0 )
                return true;
        }
        else
            return true;

        return false;
    }

}
