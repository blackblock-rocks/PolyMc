package io.github.theepicblock.polymc.mixins.item.recipe;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import eu.pb4.polymer.common.impl.CompatStatus;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.misc.SkipCheck;
import io.github.theepicblock.polymc.impl.misc.TransformingPacketCodec;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.display.SlotDisplay;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;

@Mixin(value = SlotDisplay.class, priority = 900)
public interface SlotDisplayMixin {
    @SuppressWarnings("DataFlowIssue")
    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/codec/PacketCodec;dispatch(Ljava/util/function/Function;Ljava/util/function/Function;)Lnet/minecraft/network/codec/PacketCodec;"))
    private static PacketCodec<RegistryByteBuf, SlotDisplay> transformDisplays(PacketCodec<RegistryByteBuf, SlotDisplay> original) {
        return TransformingPacketCodec.encodeOnly(original, (buf, display) -> {
            var map = Util.tryGetPolyMap(PacketContext.get());

            return switch (display) {
                case SlotDisplay.ItemSlotDisplay item when !map.canReceiveRegistryEntry(Registries.ITEM, item.item()) ->
                        new SlotDisplay.StackSlotDisplay(item.item().value().getDefaultStack());
                case SlotDisplay.TagSlotDisplay tagSlot when !((SkipCheck) (Object) tagSlot).polymc$skipped() -> {
                    var tag = buf.getRegistryManager().getOrThrow(RegistryKeys.ITEM).getOptional(tagSlot.tag());
                    if (tag.isEmpty()) {
                        yield tagSlot;
                    }

                    var array = new ArrayList<SlotDisplay>();
                    for (var entry : tag.get()) {
                        if (!map.canReceiveRegistryEntry(Registries.ITEM, entry)) {
                            array.add(new SlotDisplay.StackSlotDisplay(entry.value().getDefaultStack()));
                        }
                    }
                    if (!array.isEmpty()) {
                        var out = new SlotDisplay.TagSlotDisplay(tagSlot.tag());
                        ((SkipCheck) (Object) out).polymc$setSkipped();

                        if (CompatStatus.POLYMER_CORE) {
                            if (((SkipCheck) (Object) tagSlot).polymer$skipped()) {
                                ((SkipCheck) (Object) out).polymer$setSkipped();
                            }
                        }
                        array.addFirst(out);
                        yield new SlotDisplay.CompositeSlotDisplay(array);
                    }
                    yield tagSlot;
                }
                default -> display;
            };
        });
    }
}
