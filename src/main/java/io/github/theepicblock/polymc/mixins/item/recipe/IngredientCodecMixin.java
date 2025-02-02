package io.github.theepicblock.polymc.mixins.item.recipe;

import eu.pb4.polymer.common.api.PolymerCommonUtils;
import io.github.theepicblock.polymc.impl.Util;
import net.minecraft.item.Item;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;

/**
 * The Ingredient codecs are no longer based on the ItemStack codecs,
 * so we need to replace them in order to prevent them sending modded item data to the client
 */
@Mixin(Ingredient.class)
public abstract class IngredientCodecMixin {

    @Inject(
        method = {
            "method_61677",
            "method_61673",
            "method_61680"
        },
        at = @At("RETURN"),
        cancellable = true
    )
    private static void polymc$convertRegistryEntries(Ingredient ingredient, CallbackInfoReturnable<RegistryEntryList<Item>> cir) {
        cir.setReturnValue(polymc$convertToVanilla(cir.getReturnValue()));
    }

    /**
     * Convert a RegistryEntryList of (potentially modded) items
     * to something the client will understand
     */
    @Unique
    private static RegistryEntryList<Item> polymc$convertToVanilla(RegistryEntryList<Item> itemList) {

        if (!PolymerCommonUtils.isServerNetworkingThread()) {
            return itemList;
        }

        var ctx = PacketContext.get();

        if (ctx.getPacketListener() == null) {
            return itemList;
        }

        var map = Util.tryGetPolyMap(ctx);

        List<RegistryEntry<Item>> items = new ArrayList<>(itemList.size());

        for (var entry : itemList) {

            Item item = entry.value();
            Identifier id = Registries.ITEM.getId(item);

            if (Util.isVanilla(id)) {
                items.add(entry);
                continue;
            }

            Item clientItem = map.getClientItem(item.getDefaultStack(), ctx.getPlayer(), null).getItem();
            items.add(Registries.ITEM.getEntry(clientItem));
        }

        return RegistryEntryList.of(items);
    }
}
