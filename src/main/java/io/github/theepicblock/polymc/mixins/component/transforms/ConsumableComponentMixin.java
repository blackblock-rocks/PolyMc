package io.github.theepicblock.polymc.mixins.component.transforms;

import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.mixin.TransformingComponent;
import net.minecraft.component.type.ConsumableComponent;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.consume.ConsumeEffect;
import net.minecraft.item.consume.UseAction;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;
import java.util.Optional;

@Mixin(ConsumableComponent.class)
public abstract class ConsumableComponentMixin implements TransformingComponent {
    @Shadow @Final private float consumeSeconds;

    @Shadow @Final private RegistryEntry<SoundEvent> sound;

    @Shadow @Final private boolean hasConsumeParticles;

    @Shadow @Final private List<ConsumeEffect> onConsumeEffects;

    @Shadow @Final private UseAction useAction;

    @Override
    public Object polymc$getTransformed(PacketContext player) {
        if (!polymc$requireModification(player)) {
            return this;
        }

        return new ConsumableComponent(this.consumeSeconds, this.useAction, this.sound, this.hasConsumeParticles, List.of());
    }

    @Override
    public boolean polymc$requireModification(PacketContext player) {
        var map = Util.tryGetPolyMap(player);
        for (var effect : this.onConsumeEffects) {
            if (!map.canReceiveConsumeEffect(effect.getType())) {
                return true;
            }
        }
        return false;
    }
}
