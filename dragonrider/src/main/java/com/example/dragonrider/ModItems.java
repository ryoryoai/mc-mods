package com.example.dragonrider;

import com.example.dragonrider.item.DragonWhistleItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public class ModItems {
    public static final Item DRAGON_WHISTLE = register("dragon_whistle",
        new DragonWhistleItem(new FabricItemSettings().maxCount(1).rarity(Rarity.EPIC)));

    private static Item register(String id, Item item) {
        return Registry.register(Registries.ITEM, new Identifier("dragonrider", id), item);
    }

    public static void registerModItems() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            entries.add(DRAGON_WHISTLE);
        });
    }
}
