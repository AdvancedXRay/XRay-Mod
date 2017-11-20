package com.xray.client.gui;

import com.xray.common.XRay;
import com.xray.common.reference.Reference;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.GuiConfig;

public class GuiConfigScreen extends GuiConfig {
    public GuiConfigScreen(GuiScreen parentScreen) {
        super(parentScreen,
                new ConfigElement(XRay.config.getCategory(Configuration.CATEGORY_GENERAL)).getChildElements(),
                Reference.MOD_ID,
                false,
                false,
                GuiConfig.getAbridgedConfigPath(XRay.config.toString()));
    }
}
