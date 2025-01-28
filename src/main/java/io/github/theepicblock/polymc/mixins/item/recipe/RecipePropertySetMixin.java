package io.github.theepicblock.polymc.mixins.item.recipe;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.theepicblock.polymc.impl.Util;
import net.minecraft.item.Item;
import net.minecraft.recipe.RecipePropertySet;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;

@Mixin(RecipePropertySet.class)
public class RecipePropertySetMixin {
    @ModifyReturnValue(method = "method_64703", at = @At("TAIL"))
    private static List<RegistryEntry<Item>> removePolymerEntries(List<RegistryEntry<Item>> original) {
        var map = Util.tryGetPolyMap(PacketContext.get());
        var x = new ArrayList<>(original);
        x.removeIf(a -> !map.canReceiveRegistryEntry(Registries.ITEM, a));
        return x;
    }
}
