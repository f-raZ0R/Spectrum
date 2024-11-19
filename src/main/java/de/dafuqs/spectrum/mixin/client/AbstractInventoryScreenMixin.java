package de.dafuqs.spectrum.mixin.client;

import com.llamalad7.mixinextras.sugar.*;
import de.dafuqs.spectrum.*;
import de.dafuqs.spectrum.api.status_effect.*;
import de.dafuqs.spectrum.registries.*;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.entity.effect.*;
import net.minecraft.util.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(AbstractInventoryScreen.class)
public class AbstractInventoryScreenMixin {

	@Unique
	private static final Identifier INCURABLE_EFFECT_BACKGROUNDS = SpectrumCommon.locate("textures/gui/incurable_effect_backgrounds.png");
	@Unique
	private static final Identifier NIGHT_EFFECT_BACKGROUNDS = SpectrumCommon.locate("textures/gui/night_alchemy_effect_backgrounds.png");
	@Unique
	private static final Identifier DIVINITY_EFFECT_BACKGROUNDS = SpectrumCommon.locate("textures/gui/divinity_effect_backgrounds.png");

	@ModifyArg(method = "drawStatusEffectBackgrounds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V", ordinal = 0))
	public Identifier modifyWideBackground(Identifier texture, @Local StatusEffectInstance effect) {
		return getTexture(texture, effect);
	}

	@ModifyArg(method = "drawStatusEffectBackgrounds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V", ordinal = 1))
	public Identifier modifyBackground(Identifier texture, @Local StatusEffectInstance effect) {
		return getTexture(texture, effect);
	}

	private static Identifier getTexture(Identifier texture, StatusEffectInstance effect) {
		var type = effect.getEffectType();

		if (type == SpectrumStatusEffects.DIVINITY)
			return DIVINITY_EFFECT_BACKGROUNDS;

		if (Incurable.isIncurable(effect) && type != SpectrumStatusEffects.ETERNAL_SLUMBER && type != SpectrumStatusEffects.FATAL_SLUMBER) {
			return INCURABLE_EFFECT_BACKGROUNDS;
		}

		if (SpectrumStatusEffectTags.isIn(SpectrumStatusEffectTags.NIGHT_ALCHEMY, type))
			return NIGHT_EFFECT_BACKGROUNDS;

		return texture;
	}
}
