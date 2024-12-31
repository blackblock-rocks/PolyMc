package io.github.theepicblock.polymc.impl.poly.item;

import io.github.theepicblock.polymc.api.item.CustomModelDataManager;
import net.minecraft.item.Item;

/**
 * Polys extending this class are known to not make any placement sounds themselves, and will instead
 * have their sound be created by {@link io.github.theepicblock.polymc.mixins.block.PlaceAnimationAndSoundFix}
 */
public class PlaceableItemPoly extends SimpleItemPoly {
    public PlaceableItemPoly(Item target) {
        super(target);
    }
}
