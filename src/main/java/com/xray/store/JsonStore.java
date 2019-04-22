package com.xray.store;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.xray.XRay;
import com.xray.reference.Reference;
import com.xray.reference.block.BlockData;
import com.xray.reference.block.SimpleBlockData;

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

        jsonFile = new File(CONFIG_DIR, FILE);
    }

    public void write(HashMap<String, BlockData> blockData) throws IOException {
        List<SimpleBlockData> simpleBlockData = new ArrayList<>();
        blockData.forEach( (k, v) -> simpleBlockData.add(new SimpleBlockData(v.getEntryName(), k, v.getStateId(), v.getColor(), v.isDrawing())) );

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(jsonFile)))
        {
            gson.toJson(simpleBlockData, writer);
        }
    }

    public List<SimpleBlockData> read() {
        // No file, no data
        if( !jsonFile.exists() )
            return new ArrayList<>();

        try {
            Type type = new TypeToken<List<SimpleBlockData>>() {}.getType();
            try (BufferedReader reader = new BufferedReader(new FileReader(jsonFile)))
            {
                return gson.fromJson(reader, type);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }
}
