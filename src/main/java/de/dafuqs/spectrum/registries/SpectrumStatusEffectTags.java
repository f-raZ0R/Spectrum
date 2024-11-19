package de.dafuqs.spectrum.registries;

import de.dafuqs.spectrum.*;
import net.minecraft.entity.*;
import net.minecraft.entity.effect.*;
import net.minecraft.registry.*;
import net.minecraft.registry.tag.*;

public class SpectrumStatusEffectTags {
	
	public static TagKey<StatusEffect> NO_EFFECT_CLEAR;
	public static TagKey<StatusEffect> IMMUNITY_IMMUNE;
	public static TagKey<StatusEffect> NO_DURATION_EXTENSION;
	public static TagKey<StatusEffect> SOPORIFIC;
	public static TagKey<StatusEffect> NIGHT_ALCHEMY;

	public static void register() {
		NO_EFFECT_CLEAR = of("unclearable");
		IMMUNITY_IMMUNE = of("immunity_immune");
		NO_DURATION_EXTENSION = of("no_duration_extension");
		SOPORIFIC = of("soporific");
		NIGHT_ALCHEMY = of("night_alchemy");
	}
	
	private static TagKey<StatusEffect> of(String id) {
		return TagKey.of(RegistryKeys.STATUS_EFFECT, SpectrumCommon.locate(id));
	}

	public static boolean isIn(TagKey<StatusEffect> tag, StatusEffect effect) {
		return Registries.STATUS_EFFECT.getEntry(effect).isIn(tag);
	}
	
	public static boolean isImmunityImmune(StatusEffect statusEffect) {
		return isIn(SpectrumStatusEffectTags.IMMUNITY_IMMUNE, statusEffect);
	}
	
	public static boolean isUnclearable(StatusEffect statusEffect) {
		return isIn(SpectrumStatusEffectTags.NO_EFFECT_CLEAR, statusEffect);
	}
	
	public static boolean hasEffectWithTag(LivingEntity livingEntity, TagKey<StatusEffect> tag) {
		for (StatusEffect statusEffect : livingEntity.getActiveStatusEffects().keySet()) {
			if (SpectrumStatusEffectTags.isIn(tag, statusEffect)) {
				return true;
			}
		}
		return false;
	}
	
}
