package com.xray.world;

import com.xray.reference.Reference;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

import javax.annotation.ParametersAreNonnullByDefault;

public class WorldSave extends WorldSavedData {

    private static final String DATA_KEY = Reference.MOD_NAME + "_BlockData";

    public WorldSave() {
        super(DATA_KEY);
    }

    public WorldSave(String name) {
        super(name);
    }

    public static WorldSave get(World world) {
        MapStorage storage = world.getMapStorage();

        if( storage == null )
            return null;

        WorldSave instance = (WorldSave) storage.getOrLoadData(WorldSave.class, DATA_KEY);

        if (instance == null) {
            instance = new WorldSave();
            storage.setData(DATA_KEY, instance);
        }

        return instance;
    }

    @Override
    @ParametersAreNonnullByDefault
    public void readFromNBT(NBTTagCompound nbt) {
    }

    @Override
    @ParametersAreNonnullByDefault
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        return null;
    }

}

