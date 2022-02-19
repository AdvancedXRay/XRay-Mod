package pro.mikey.xray.store;

import pro.mikey.xray.xray.Controller;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;

public class GameBlockStore {

    private ArrayList<BlockWithItemStack> store = new ArrayList<>();

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
            if( !(item instanceof net.minecraft.world.item.BlockItem) )
                continue;

            Block block = Block.byItem(item);
            if ( item == Items.AIR || block == Blocks.AIR || Controller.blackList.contains(block) )
                continue; // avoids troubles

            store.add(new BlockWithItemStack(block, new ItemStack(item)));
        }
    }

    public void repopulate()
    {
        this.store.clear();
        this.populate();
    }

    public ArrayList<BlockWithItemStack> getStore() {
        return this.store;
    }

    public static final class BlockWithItemStack {
        private Block block;
        private ItemStack itemStack;

        public BlockWithItemStack(Block block, ItemStack itemStack) {
            this.block = block;
            this.itemStack = itemStack;
        }

        public Block getBlock() {
            return block;
        }

        public ItemStack getItemStack() {
            return itemStack;
        }
    }
}
