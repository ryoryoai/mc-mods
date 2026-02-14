package com.example.thunderaxe;

import com.example.thunderaxe.item.ThunderAxeItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {
    public static final Item THUNDER_AXE = registerItem("thunder_axe",
        new ThunderAxeItem(new Item.Settings().maxCount(1).maxDamage(500)));

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM,
            new Identifier("axe", name), item);
    }

    public static void registerModItems() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT)
            .register(entries -> {
                entries.add(THUNDER_AXE);
            });
    }
}
