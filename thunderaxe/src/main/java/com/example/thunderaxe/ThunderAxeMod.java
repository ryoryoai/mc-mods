package com.example.thunderaxe;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThunderAxeMod implements ModInitializer {
    public static final String MOD_ID = "thunderaxe";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModItems.registerModItems();
        LOGGER.info("Thunder Axe mod initialized!");
    }
}
