package com.xray.store;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.xray.XRay;
import com.xray.reference.Reference;
import com.xray.reference.block.BlockData;
import com.xray.reference.block.SimpleBlockData;
import org.apache.logging.log4j.Level;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JsonStore
{
    private static final String FILE = "block_store.json";
    private static final String FILE_SPECIFIC = "block_store_1_12.json";
    private static final String CONFIG_DIR = XRay.mc.mcDataDir + "/config/";

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private File jsonFileOld;
    private File jsonFileNew;

    // This should only be initialised once
    public JsonStore()
    {
        File configDir = new File(CONFIG_DIR, Reference.MOD_ID);

        if( !configDir.exists() )
            configDir.mkdirs();

        jsonFileNew = new File(CONFIG_DIR + Reference.MOD_ID, FILE_SPECIFIC);
        jsonFileOld = new File(CONFIG_DIR + Reference.MOD_ID, FILE);
        if( !jsonFileOld.exists() && !jsonFileNew.exists() ) {
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
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(jsonFileNew)))
        {
            gson.toJson(simpleBlockData, writer);
        }
        catch (IOException e) {
            XRay.logger.log(Level.ERROR, "Failed to write json data to " + FILE);
        }
    }


    public List<SimpleBlockData> read() {
        return read(true);
    }

    public List<SimpleBlockData> read(boolean readOld) {
        if( !jsonFileOld.exists() && !jsonFileNew.exists() )
            return new ArrayList<>();

        // Try and read the old one first if there is no new one
        if (jsonFileOld.exists() && !jsonFileNew.exists() && readOld) {
            Type type = new TypeToken<List<SimpleBlockData>>() {}.getType();
            try (BufferedReader reader = new BufferedReader(new FileReader(jsonFileOld))) {
                // If parsing worked, write it to the new file and delete the old one
                List<SimpleBlockData> simpleBlockData = gson.fromJson(reader, type);
                this.write(simpleBlockData);
                boolean delete = jsonFileOld.delete();
                if (delete) {
                    XRay.logger.debug("Deleted old config file in preference for versioned file");
                }
            } catch (IOException | JsonSyntaxException e) {
                e.printStackTrace();
                boolean delete = jsonFileOld.delete();
                if (delete) {
                    XRay.logger.debug("Deleted invalid config");
                }
            }

            // Try and read the new file or default to an empty
            this.read(false);
        } else {
            // Read the new one :D
            Type type = new TypeToken<List<SimpleBlockData>>() {}.getType();
            try (BufferedReader reader = new BufferedReader(new FileReader(jsonFileNew))) {
                return gson.fromJson(reader, type);
            } catch (IOException | JsonSyntaxException e) {
                e.printStackTrace();
            }
        }

        return new ArrayList<>();
    }
}
