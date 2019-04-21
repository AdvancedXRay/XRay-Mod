package com.xray.world;

import com.xray.reference.Reference;
import com.xray.reference.block.BlockData;
import com.xray.utils.OutlineColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;

public class BlockStorage extends WorldSavedData {

    private static final String DATA_KEY = Reference.MOD_NAME + "_BlockData";

    private HashMap<String, BlockData> blockStorage = new HashMap<>();

    public BlockStorage() {
        super(DATA_KEY);
    }

    public BlockStorage(String name) {
        super(name);
    }

    public static BlockStorage get(World world) {
        MapStorage storage = world.getMapStorage();

        if (storage == null)
            throw new IllegalStateException("World#getMapStorage returned null. The following WorldSave failed to save data: " + DATA_KEY);

        BlockStorage instance = (BlockStorage) storage.getOrLoadData(BlockStorage.class, DATA_KEY);

        if (instance == null) {
            instance = new BlockStorage();
            storage.setData(DATA_KEY, instance);
        }

        return instance;
    }

    @Override
    @ParametersAreNonnullByDefault
    public void readFromNBT(NBTTagCompound nbt) {

        NBTTagList list = nbt.getTagList("blocks", 10);

        for (int i = 0; i < list.tagCount(); ++i)
        {
            NBTTagCompound compound = list.getCompoundTagAt(i);

            blockStorage.put(
                    compound.getString("key"),
                    new BlockData(
                            compound.getString("entryName"),
                            new OutlineColor( compound.getIntArray("color") ),
                            new ItemStack( compound.getCompoundTag("stack") ),
                            compound.getBoolean("drawing")
                    )
            );
        }

        System.out.println("READ NBY");
        System.out.println(blockStorage.size());
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        NBTTagList list = new NBTTagList();

        blockStorage.forEach( (k, v) -> {
            NBTTagCompound c = new NBTTagCompound();

            c.setString("key", k);

            c.setString("entryName", v.getEntryName());
            c.setIntArray("color", new int[]{v.getOutline().getRed(), v.getOutline().getGreen(), v.getOutline().getBlue()});
            c.setBoolean("drawing", v.isDrawing());
            c.setTag("stack", v.getItemStack().writeToNBT(new NBTTagCompound()));

            list.appendTag(c);
        });

        compound.setTag("blocks", list);
        System.out.println("WRUTE NBY");

        return compound;
    }

    public HashMap<String, BlockData> getBlockStorage() {
        return blockStorage;
    }

    public void setBlockStorage(HashMap<String, BlockData> blockStorage) {
        this.blockStorage = blockStorage;
    }
}

