package de.dafuqs.spectrum.mixin.accessors;

import net.minecraft.entity.effect.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

import java.util.*;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Accessor
    DamageSource getLastDamageSource();

    @Accessor("lastDamageSource")
    public void setLastDamageSource(DamageSource damageSource);

    @Accessor()
    Map<StatusEffect, StatusEffectInstance> getActiveStatusEffects();
}
