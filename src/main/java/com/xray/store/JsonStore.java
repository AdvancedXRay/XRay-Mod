package com.xray.store;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.xray.XRay;
import com.xray.reference.Reference;
import com.xray.reference.block.BlockData;
import com.xray.reference.block.SimpleBlockData;
import net.minecraft.block.Block;
import org.apache.logging.log4j.Level;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JsonStore
{
    private static final String FILE = "block_store.json";
    private static final String CONFIG_DIR = XRay.mc.mcDataDir + "/config/";

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private File jsonFile;

    // This should only be initialised once
    public JsonStore()
    {
        File configDir = new File(CONFIG_DIR, Reference.MOD_ID);

        if( !configDir.exists() )
            configDir.mkdirs();

        jsonFile = new File(CONFIG_DIR + Reference.MOD_ID, FILE);
        if( !jsonFile.exists() ) {
            List<SimpleBlockData> simpleBlockData = new ArrayList<>(BlockStore.DEFAULT_BLOCKS);
            for (int i = 0; i < simpleBlockData.size(); i++)
                simpleBlockData.get(i).setOrder(i);

            this.write(simpleBlockData);
        }
    }

    public void write(HashMap<String, BlockData> blockData) {
        List<SimpleBlockData> simpleBlockData = new ArrayList<>();
        blockData.forEach( (k, v) -> simpleBlockData.add(new SimpleBlockData(v.getEntryName(), k, v.getStateId(), v.getColor(), v.isDrawing(), v.getOrder())) );

        this.write(simpleBlockData);
    }

    private void write(List<SimpleBlockData> simpleBlockData) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(jsonFile)))
        {
            gson.toJson(simpleBlockData, writer);
        }
        catch (IOException e) {
            XRay.logger.log(Level.ERROR, "Failed to write json data to " + FILE);
        }
    }

    public List<SimpleBlockData> read() {
        if( !jsonFile.exists() )
            return new ArrayList<>();

        try
        {
            Type type = new TypeToken<List<SimpleBlockData>>() {}.getType();
            try (BufferedReader reader = new BufferedReader(new FileReader(jsonFile)))
            {
                return gson.fromJson(reader, type);
            }
        }
        catch (IOException e)
        {
            XRay.logger.log(Level.ERROR, "Failed to read json data from " + FILE);
        }

        return new ArrayList<>();
    }
}
