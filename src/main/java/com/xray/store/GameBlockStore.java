package com.xray.store;

import com.xray.reference.block.BlockItem;
import com.xray.xray.Controller;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;

public class GameBlockStore {

    private ArrayList<BlockItem> store = new ArrayList<>();

    /**
     * This method is used to fill the store as we do not intend to update this after
     * it has been populated, it's a singleton by nature but we still need some
     * amount of control over when it is populated.
     */
    public void populate()
    {
        // Avoid doing the logic again unless repopulate is called
        if( this.store.size() != 0 )
            return;

        for ( Item item : ForgeRegistries.ITEMS ) {

            if( !(item instanceof ItemBlock) )
                continue;

            Block block = Block.getBlockFromItem(item);
            if ( item == Items.AIR || block == Blocks.AIR )
                continue; // avoids troubles

            if( item.getHasSubtypes() && item.getCreativeTab() != null ) {
                NonNullList<ItemStack> subItems = NonNullList.create();
                item.getSubItems(item.getCreativeTab(), subItems);
                for (ItemStack subItem : subItems) {
                    if (subItem.equals(ItemStack.EMPTY) || subItem.getItem() == Items.AIR || Controller.blackList.contains(block))
                        continue;

                    store.add(new BlockItem(Block.getStateId(Block.getBlockFromItem(subItem.getItem()).getBlockState().getBaseState()), subItem));
                }
            }
            else {
                if( Controller.blackList.contains(block) )
                    continue;

                store.add(new BlockItem(Block.getStateId(block.getBlockState().getBaseState()), new ItemStack(item)));
            }
        }
    }

    public void repopulate()
    {
        this.store.clear();
        this.populate();
    }

    public ArrayList<BlockItem> getStore() {
        return this.store;
    }
}
