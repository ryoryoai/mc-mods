package com.example.dragonrider;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.util.ActionResult;

public class DragonRiderMod implements ModInitializer {
    @Override
    public void onInitialize() {
        ModItems.registerModItems();

        // 右クリックでエンダードラゴンに騎乗
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            // ドラゴンのパーツ（体・翼・尻尾）をクリックした場合、本体を取得
            Entity target = entity;
            if (entity instanceof EnderDragonPart part) {
                target = part.owner;
            }

            if (target instanceof EnderDragonEntity dragon) {
                if (!dragon.hasPassengers() && !player.isSneaking()) {
                    if (!world.isClient) {
                        player.startRiding(dragon, true);
                    }
                    return ActionResult.SUCCESS;
                }
            }
            return ActionResult.PASS;
        });
    }
}
