package com.example.thunderaxe.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterials;

public class ThunderAxeItem extends AxeItem {
    public ThunderAxeItem(Settings settings) {
        // IRON base damage=2, axe bonus damage=7 → total 9
        // attackSpeed offset: 0.9 attacks/sec → offset = 0.9 - 4.0 = -3.1
        super(ToolMaterials.IRON, 7.0f, -3.1f, settings);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target,
                           LivingEntity attacker) {
        // Glowing effect: 8 seconds (160 ticks), level 1
        target.addStatusEffect(
            new StatusEffectInstance(StatusEffects.GLOWING, 160, 0)
        );
        return super.postHit(stack, target, attacker);
    }
}
