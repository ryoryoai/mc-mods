package com.example.guns;

import com.example.guns.item.GrenadeLauncherItem;
import com.example.guns.item.MagnumItem;
import com.example.guns.item.RevolverItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {
    // 銃 (gun:)
    public static final Item MAGNUM = gun("magnum",
        new MagnumItem(new Item.Settings().maxCount(1).maxDamage(200)));
    public static final Item REVOLVER = gun("revolver",
        new RevolverItem(new Item.Settings().maxCount(1).maxDamage(300)));
    public static final Item GRENADE_LAUNCHER = gun("grenade_launcher",
        new GrenadeLauncherItem(new Item.Settings().maxCount(1).maxDamage(150)));

    // マグナム/リボルバー弾 (bullet:magnum_*)
    public static final Item MAGNUM_BULLET = bullet("magnum_bullet",
        new Item(new Item.Settings().maxCount(64)));
    public static final Item MAGNUM_HEAVY_BULLET = bullet("magnum_heavy_bullet",
        new Item(new Item.Settings().maxCount(64)));
    public static final Item MAGNUM_FIRE_BULLET = bullet("magnum_fire_bullet",
        new Item(new Item.Settings().maxCount(64)));

    // グレネードランチャー弾 (bullet:grenade_launcher_*)
    public static final Item GRENADE_LAUNCHER_FREEZE_GRENADE = bullet("grenade_launcher_freeze_grenade",
        new Item(new Item.Settings().maxCount(16)));
    public static final Item GRENADE_LAUNCHER_LIGHTNING_GRENADE = bullet("grenade_launcher_lightning_grenade",
        new Item(new Item.Settings().maxCount(16)));
    public static final Item GRENADE_LAUNCHER_FIRE_GRENADE = bullet("grenade_launcher_fire_grenade",
        new Item(new Item.Settings().maxCount(16)));
    public static final Item GRENADE_LAUNCHER_ACID_GRENADE = bullet("grenade_launcher_acid_grenade",
        new Item(new Item.Settings().maxCount(16)));
    public static final Item GRENADE_LAUNCHER_MINING_GRENADE = bullet("grenade_launcher_mining_grenade",
        new Item(new Item.Settings().maxCount(16)));
    public static final Item GRENADE_LAUNCHER_MINI_BOMB = bullet("grenade_launcher_mini_bomb",
        new Item(new Item.Settings().maxCount(16)));
    public static final Item GRENADE_LAUNCHER_BOMB = bullet("grenade_launcher_bomb",
        new Item(new Item.Settings().maxCount(16)));
    public static final Item GRENADE_LAUNCHER_MEGA_BOMB = bullet("grenade_launcher_mega_bomb",
        new Item(new Item.Settings().maxCount(8)));

    private static Item gun(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier("gun", name), item);
    }

    private static Item bullet(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier("bullet", name), item);
    }

    public static void registerModItems() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT)
            .register(entries -> {
                entries.add(MAGNUM);
                entries.add(REVOLVER);
                entries.add(GRENADE_LAUNCHER);
                entries.add(MAGNUM_BULLET);
                entries.add(MAGNUM_HEAVY_BULLET);
                entries.add(MAGNUM_FIRE_BULLET);
                entries.add(GRENADE_LAUNCHER_FREEZE_GRENADE);
                entries.add(GRENADE_LAUNCHER_LIGHTNING_GRENADE);
                entries.add(GRENADE_LAUNCHER_FIRE_GRENADE);
                entries.add(GRENADE_LAUNCHER_ACID_GRENADE);
                entries.add(GRENADE_LAUNCHER_MINING_GRENADE);
                entries.add(GRENADE_LAUNCHER_MINI_BOMB);
                entries.add(GRENADE_LAUNCHER_BOMB);
                entries.add(GRENADE_LAUNCHER_MEGA_BOMB);
            });
    }
}
