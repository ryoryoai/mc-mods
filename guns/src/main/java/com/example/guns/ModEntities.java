package com.example.guns;

import com.example.guns.entity.GrenadeEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEntities {
    public static final EntityType<GrenadeEntity> GRENADE = Registry.register(
        Registries.ENTITY_TYPE,
        new Identifier("gun", "grenade"),
        FabricEntityTypeBuilder.<GrenadeEntity>create(SpawnGroup.MISC, GrenadeEntity::new)
            .dimensions(EntityDimensions.fixed(0.25f, 0.25f))
            .trackRangeBlocks(64)
            .trackedUpdateRate(10)
            .build()
    );

    public static void registerModEntities() {
        // Force class loading to trigger static initializers
    }
}
