package com.xray.store;

import com.xray.XRay;
import com.xray.reference.Reference;

import java.io.File;
import java.io.IOException;

public class JsonStore
{
    private static final String FILE = "block_store.json";
    private static final String CONFIG_DIR = XRay.mc.mcDataDir + "/config/";

    private File jsonFile;

    // This should only be initialised once
    public JsonStore()
    {
        File configDir = new File(CONFIG_DIR, Reference.MOD_ID);

        if( !configDir.exists() )
            configDir.mkdirs();

        jsonFile = new File(CONFIG_DIR, FILE);

    }
}
