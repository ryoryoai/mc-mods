package com.example.guns.client;

import com.example.guns.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;

public class GunsModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // グレネード弾をアイテムの見た目で表示（飛んでいるグレネードが見える）
        EntityRendererRegistry.register(ModEntities.GRENADE, FlyingItemEntityRenderer::new);
    }
}
