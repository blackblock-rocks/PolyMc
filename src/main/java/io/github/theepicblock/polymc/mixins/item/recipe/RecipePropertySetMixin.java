package io.github.theepicblock.polymc.mixins.item.recipe;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.theepicblock.polymc.impl.Util;
import net.minecraft.item.Item;
import net.minecraft.recipe.RecipePropertySet;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;

@Mixin(RecipePropertySet.class)
public class RecipePropertySetMixin {
    @ModifyReturnValue(method = "method_64703", at = @At("TAIL"))
    private static List<RegistryEntry<Item>> removePolymerEntries(List<RegistryEntry<Item>> original) {
        var ctx = PacketContext.get();

        if (ctx.getPacketListener() == null) {
            return original;
        }

        var map = Util.tryGetPolyMap(ctx);
        List<RegistryEntry<Item>> converted = new ArrayList<>(original.size());

        for (var entry : original) {
            Item item = entry.value();
            Identifier id = Registries.ITEM.getId(item);

            if (Util.isVanilla(id)) {
                converted.add(entry);
                continue;
            }

            Item clientItem = map.getClientItem(item.getDefaultStack(), ctx.getPlayer(), null).getItem();
            converted.add(Registries.ITEM.getEntry(clientItem));
        }

        return converted;
    }
}
