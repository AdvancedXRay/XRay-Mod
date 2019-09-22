package com.xray.store;

import com.xray.reference.block.BlockData;
import com.xray.reference.block.SimpleBlockData;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class BlockStore {

    // Default blocks
    static final List<SimpleBlockData> DEFAULT_BLOCKS = new ArrayList<SimpleBlockData>() {
        {
//            add( SimpleBlockData.firstOreInDictionary( "oreIron", "Iron Ore",             new int[]{170,117, 37}, false ));
//            add( SimpleBlockData.firstOreInDictionary( "oreCoal", "Coal Ore",             new int[]{  0,  0,  0}, false ));
//            add( SimpleBlockData.firstOreInDictionary( "oreGold", "Gold Ore",             new int[]{255,255,  0}, false ));
//            add( SimpleBlockData.firstOreInDictionary( "oreRedstone", "Redstone Ore",     new int[]{255,  0,  0}, false ));
//            add( SimpleBlockData.firstOreInDictionary( "oreDiamond", "Diamond Ore",       new int[]{136,136,255}, true  ));
//            add( SimpleBlockData.firstOreInDictionary( "oreEmerald", "Emerald Ore",       new int[]{  0,136, 10}, true  ));
//            add( SimpleBlockData.firstOreInDictionary( "oreQuartz","Nether Quart",        new int[]{ 30, 74,  0}, false ));
//            add( SimpleBlockData.firstOreInDictionary( "oreLapis", "Lapis",               new int[]{  0,  0,255}, false ));
        }
    };

    private HashMap<UUID, BlockData> store = new HashMap<>();
    private HashMap<String, UUID>    storeReference = new HashMap<>();

    public void put(BlockData data) {
        UUID uniqueId = UUID.randomUUID();
        this.store.put(uniqueId, data);
        this.storeReference.put(data.getBlockName(), uniqueId);
    }

    public HashMap<UUID, BlockData> getStore() {
        return store;
    }

    public void setStore(ArrayList<BlockData> store) {
        this.store.clear();
        this.storeReference.clear();

        store.forEach(this::put);
    }

    public BlockDataWithUUID getStoreByReference(String name) {
        UUID uniqueId = storeReference.get(name);
        if( uniqueId == null )
            return null;

        BlockData blockData = this.store.get(uniqueId);
        if( blockData == null )
            return null;

        return new BlockDataWithUUID(blockData, uniqueId);
    }

    public void toggleDrawing(BlockData data) {
        UUID uniqueId = storeReference.get(data.getBlockName());
        if( uniqueId == null )
            return;

        // We'd hope this never happens...
        BlockData blockData = this.store.get(uniqueId);
        if( blockData == null )
            return;

        blockData.setDrawing(!blockData.isDrawing());
    }

    public static ArrayList<BlockData> getFromSimpleBlockList(List<SimpleBlockData> simpleList)
    {
        ArrayList<BlockData> blockData = new ArrayList<>();

        for (SimpleBlockData e : simpleList) {
            Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(e.getBlockName()));
            if( block == null )
                continue;

            blockData.add(
                    new BlockData(
                            e.getName(),
                            e.getBlockName(),
                            e.getColor(),
                            new ItemStack( block, 1),
                            e.isDrawing(),
                            e.getOrder()
                    )
            );
        }

        return blockData;
    }

    public static final class BlockDataWithUUID {
        BlockData blockData;
        UUID uuid;

        public BlockDataWithUUID(BlockData blockData, UUID uuid) {
            this.blockData = blockData;
            this.uuid = uuid;
        }

        public BlockData getBlockData() {
            return blockData;
        }

        public UUID getUuid() {
            return uuid;
        }
    }
}
