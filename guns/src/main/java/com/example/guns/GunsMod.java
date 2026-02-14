package com.example.guns;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GunsMod implements ModInitializer {
    public static final String MOD_ID = "guns";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModItems.registerModItems();
        ModEntities.registerModEntities();
        LOGGER.info("Guns mod initialized! (magnum, revolver, grenade launcher, bullets, grenades)");
    }
}
