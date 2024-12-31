package io.github.theepicblock.polymc.mixins.component.transforms;

import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.mixin.TransformingComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.consume.ApplyEffectsConsumeEffect;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;

@Mixin(ApplyEffectsConsumeEffect.class)
public abstract class ApplyEffectsConsumeEffectMixin implements TransformingComponent {
    @Shadow @Final private List<StatusEffectInstance> effects;

    @Override
    public Object polymc$getTransformed(PacketContext context) {
        if (!polymc$requireModification(context)) {
            return this;
        }

        return new ApplyEffectsConsumeEffect(List.of());
    }

    @Override
    public boolean polymc$requireModification(PacketContext context) {
        var map = Util.tryGetPolyMap(context);
        for (var effect : this.effects) {
            if (!map.canReceiveStatusEffect(effect.getEffectType())) {
                return true;
            }
        }
        return false;
    }
}
