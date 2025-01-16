package io.github.theepicblock.polymc.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.serialization.Codec;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import io.github.theepicblock.polymc.impl.Util;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Optional;
import java.util.function.Function;

@Mixin(Registry.class)
public interface RegistryMixin {
    @Shadow RegistryEntry<Object> getEntry(Object value);

    @Shadow Optional<RegistryEntry.Reference<Object>> getEntry(int rawId);

    @Shadow int getRawId(@Nullable Object value);

    @ModifyReturnValue(method = "getReferenceEntryCodec", at = @At(value = "RETURN"))
    private Codec<RegistryEntry.Reference<Object>> patchCodec(Codec<RegistryEntry.Reference<Object>> codec) {
        return codec.xmap(Function.identity(), content -> { // Encode
            if (PolymerCommonUtils.isServerNetworkingThread() && content.hasKeyAndValue()) {
                var ctx = PacketContext.get();
                var map = Util.tryGetPolyMap(ctx);
                //noinspection rawtypes
                if (map.canReceiveRegistryEntry((Registry) this, content)) {
                    return content;
                }

                var fallback = this.getEntry(0).orElseThrow();
                var val = content.value();
                if (val instanceof Item item) {
                    var client = map.getClientItem(new ItemStack(item), ctx.getPlayer(), null);
                    return this.getEntry(this.getRawId(client.getItem())).orElse(fallback);
                } else if (val instanceof Block item) {
                    var client = map.getClientState(item.getDefaultState(), ctx.getPlayer());
                    return this.getEntry(this.getRawId(client.getBlock())).orElse(fallback);
                } else if (val instanceof SoundEvent ) {
                    return this.getEntry(this.getRawId(SoundEvents.INTENTIONALLY_EMPTY)).orElse(fallback);
                }

                return fallback;
            }
            return content;
        });
    }
}
