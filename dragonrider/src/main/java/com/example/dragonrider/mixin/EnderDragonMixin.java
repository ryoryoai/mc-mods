package com.example.dragonrider.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(EnderDragonEntity.class)
public class EnderDragonMixin {

    // ========== 騎乗中の移動制御 ==========
    // プレイヤーの視線方向に飛行する
    @Inject(method = "tickMovement", at = @At("TAIL"))
    private void controlWhenRidden(CallbackInfo ci) {
        EnderDragonEntity dragon = (EnderDragonEntity) (Object) this;
        if (!(dragon.getFirstPassenger() instanceof PlayerEntity player)) return;

        float yaw = player.getYaw();
        float pitch = MathHelper.clamp(player.getPitch(), -60f, 60f);
        float speed = 3.0f;

        double radYaw = Math.toRadians(yaw);
        double radPitch = Math.toRadians(pitch);

        double dx = -Math.sin(radYaw) * Math.cos(radPitch) * speed * 0.05;
        double dy = -Math.sin(radPitch) * speed * 0.05;
        double dz = Math.cos(radYaw) * Math.cos(radPitch) * speed * 0.05;

        // ドラゴンを視線方向に移動
        double newX = dragon.getX() + dx;
        double newY = dragon.getY() + dy;
        double newZ = dragon.getZ() + dz;
        dragon.setPosition(newX, newY, newZ);

        // ドラゴンの向きをプレイヤーの向きに合わせる
        // EnderDragonのモデルは通常エンティティと180度逆向き
        dragon.setYaw(yaw + 180);
        dragon.bodyYaw = yaw + 180;

        // ライダーをドラゴンの背中に配置（Y + 3.5）
        player.setPosition(newX, newY + 3.5, newZ);
    }

    // ========== ライダーへのダメージ防止 ==========
    // ドラゴンの体が触れてもライダーにダメージを与えない
    @Inject(method = "damageLivingEntities", at = @At("HEAD"))
    private void protectRiderFromDamage(List<Entity> entities, CallbackInfo ci) {
        EnderDragonEntity dragon = (EnderDragonEntity) (Object) this;
        Entity rider = dragon.getFirstPassenger();
        if (rider != null) {
            entities.removeIf(e -> e == rider);
        }
    }

    // ドラゴンの翼がライダーを吹き飛ばさない
    @Inject(method = "launchLivingEntities", at = @At("HEAD"))
    private void protectRiderFromLaunch(List<Entity> entities, CallbackInfo ci) {
        EnderDragonEntity dragon = (EnderDragonEntity) (Object) this;
        Entity rider = dragon.getFirstPassenger();
        if (rider != null) {
            entities.removeIf(e -> e == rider);
        }
    }
}
