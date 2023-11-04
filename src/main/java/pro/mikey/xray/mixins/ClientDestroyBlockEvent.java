package pro.mikey.xray.mixins;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pro.mikey.xray.xray.Events;

@Debug(export = true)
@Mixin(ClientLevel.class)
public abstract class ClientDestroyBlockEvent {

    @Inject(method = "setBlock", at = @At("RETURN"))
    public void onBlockDestroy(BlockPos arg, BlockState arg2, int i, int j, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            Events.breakBlock(arg, arg2);
        }
    }
}
