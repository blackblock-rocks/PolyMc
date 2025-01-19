package io.github.theepicblock.polymc.mixins.item.recipe;

import io.github.theepicblock.polymc.impl.misc.SkipCheck;
import net.minecraft.recipe.display.SlotDisplay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(SlotDisplay.TagSlotDisplay.class)
public abstract class TagSlotDisplayMixin implements SkipCheck {
    @Unique
    private boolean skipped = false;

    @Override
    public boolean polymc$skipped() {
        return this.skipped;
    }

    @Override
    public void polymc$setSkipped() {
        this.skipped = true;
    }
}
