package com.example.guns.item;

public class RevolverItem extends BaseGunItem {
    public RevolverItem(Settings settings) {
        super(settings,
            6,      // magazineSize
            10.0f,  // baseDamage (5 hearts)
            32.0,   // range
            10,     // fireCooldown (0.5 sec)
            30      // reloadCooldown (1.5 sec)
        );
    }

    @Override
    protected float getFirePitch() {
        return 1.2f; // 高い音 = 軽い銃
    }
}
