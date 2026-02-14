package com.example.guns.item;

public class MagnumItem extends BaseGunItem {
    public MagnumItem(Settings settings) {
        super(settings,
            6,      // magazineSize
            15.0f,  // baseDamage (7.5 hearts)
            48.0,   // range
            20,     // fireCooldown (1 sec)
            40      // reloadCooldown (2 sec)
        );
    }

    @Override
    protected float getFirePitch() {
        return 0.5f; // 低い音 = 重い銃
    }
}
