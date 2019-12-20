package com.xray.utils;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.PlayerEntity;

public final class TempMapping {
    public static final class Player {
        public static double getPosX(PlayerEntity playerEntity) {
            return playerEntity.func_226277_ct_();
        }

        public static double getPosY(PlayerEntity playerEntity) {
            return playerEntity.func_226278_cu_();
        }

        public static double getPosZ(PlayerEntity playerEntity) {
            return playerEntity.func_226281_cx_();
        }
    }

    public static final class Render {
        public static void enableGUIStandardItemLighting() {
            RenderHelper.func_227780_a_();
        }
    }

    public static final class MinecraftInstance {
        public static MainWindow mainWindow() {
            return Minecraft.getInstance().func_228018_at_();
        }
    }
}
