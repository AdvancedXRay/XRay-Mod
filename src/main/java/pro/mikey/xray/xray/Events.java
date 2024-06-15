package pro.mikey.xray.xray;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

public class Events {
    public static void breakBlock(BlockPos pos, BlockState blockState) {
        RenderEnqueue.checkBlock(pos, blockState, !blockState.isAir());
    }

    public static void tickEnd(ClientTickEvent.Post event) {
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().level != null) {
            Controller.requestBlockFinder(false);
        }
    }

    public static void onWorldRenderLast(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        if (Controller.isXRayActive() && Minecraft.getInstance().player != null) {
            // this is a world pos of the player
            Render.renderBlocks(event);
        }
    }
}
