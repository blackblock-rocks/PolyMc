package io.github.theepicblock.polymc.mixins.block.implementations;

import io.github.theepicblock.polymc.impl.Util;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Optional;

@Mixin(targets = "net/minecraft/entity/data/TrackedDataHandlerRegistry$2")
public class TrackedDataImplementation {
    /*@ModifyArg(method = "write(Lnet/minecraft/network/PacketByteBuf;Ljava/util/Optional;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;getRawIdFromState(Lnet/minecraft/block/BlockState;)I"))
    private BlockState redirectGetRawId(BlockState state) {
        var player = PacketContext.get().getTarget();
        var map = Util.tryGetPolyMap(player);

        return map.getClientState(state, player);
    }*/

    @Inject(method = "write(Lnet/minecraft/network/PacketByteBuf;Ljava/util/Optional;)V", at=@At("HEAD"), cancellable = true)
    private void getRawId(PacketByteBuf packetByteBuf, Optional<BlockState> optional, CallbackInfo ci) {
        if (optional.isPresent()) {
            var player = PacketContext.get().getTarget();
            System.out.println("Got player: " + player);
            BlockState state = optional.get();
            packetByteBuf.writeVarInt(Util.getPolydRawIdFromState(state, player));

        } else {
            packetByteBuf.writeVarInt(0);
        }
    }
}
