package com.example.guns.item;

import com.example.guns.ModItems;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public abstract class BaseGunItem extends Item {
    protected final int magazineSize;
    protected final float baseDamage;
    protected final double range;
    protected final int fireCooldown;
    protected final int reloadCooldown;

    public BaseGunItem(Settings settings, int magazineSize, float baseDamage,
                       double range, int fireCooldown, int reloadCooldown) {
        super(settings);
        this.magazineSize = magazineSize;
        this.baseDamage = baseDamage;
        this.range = range;
        this.fireCooldown = fireCooldown;
        this.reloadCooldown = reloadCooldown;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        NbtCompound nbt = stack.getOrCreateNbt();
        int loaded = nbt.getInt("Loaded");
        String bulletType = nbt.getString("BulletType");

        // Shift+右クリック: 手動リロード（弾種切替用）
        if (player.isSneaking()) {
            return reload(world, player, stack, nbt);
        }

        // 弾が入っていない → 自動リロード
        if (loaded <= 0) {
            return reload(world, player, stack, nbt);
        }

        // 発射
        return fire(world, player, hand, stack, nbt, loaded, bulletType);
    }

    private TypedActionResult<ItemStack> fire(World world, PlayerEntity player, Hand hand,
                                               ItemStack stack, NbtCompound nbt,
                                               int loaded, String bulletType) {
        // 発射音
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST, SoundCategory.PLAYERS,
            1.0f, getFirePitch());

        if (!world.isClient) {
            // 弾丸の性能を取得
            float damageMultiplier = getDamageMultiplier(bulletType);
            boolean setsFire = "magnum_fire_bullet".equals(bulletType);

            // ヒットスキャン
            Vec3d eyePos = player.getEyePos();
            Vec3d lookVec = player.getRotationVec(1.0f);
            Vec3d endPos = eyePos.add(lookVec.multiply(range));
            Box searchBox = player.getBoundingBox()
                .stretch(lookVec.multiply(range)).expand(1.0);

            EntityHitResult hitResult = ProjectileUtil.getEntityCollision(
                world, player, eyePos, endPos, searchBox,
                entity -> entity instanceof LivingEntity && entity != player
            );

            if (hitResult != null && hitResult.getEntity() instanceof LivingEntity target) {
                float damage = baseDamage * damageMultiplier;
                target.damage(world.getDamageSources().playerAttack(player), damage);
                target.addVelocity(lookVec.x * 0.8, 0.2, lookVec.z * 0.8);

                if (setsFire) {
                    target.setOnFireFor(3);
                }
            }

            stack.damage(1, player, p -> p.sendToolBreakStatus(hand));
        }

        // 装弾数を減らす
        loaded--;
        nbt.putInt("Loaded", loaded);
        if (loaded <= 0) {
            nbt.putString("BulletType", "");
        }

        player.getItemCooldownManager().set(this, fireCooldown);
        return TypedActionResult.success(stack, world.isClient);
    }

    private TypedActionResult<ItemStack> reload(World world, PlayerEntity player,
                                                 ItemStack stack, NbtCompound nbt) {
        // インベントリから弾丸を探す（優先: 強化弾 > 火炎弾 > 通常弾）
        Item[] bulletPriority = { ModItems.MAGNUM_HEAVY_BULLET, ModItems.MAGNUM_FIRE_BULLET, ModItems.MAGNUM_BULLET };
        ItemStack bulletStack = ItemStack.EMPTY;
        String foundType = "";

        for (Item bulletItem : bulletPriority) {
            for (int i = 0; i < player.getInventory().size(); i++) {
                ItemStack invStack = player.getInventory().getStack(i);
                if (invStack.getItem() == bulletItem) {
                    bulletStack = invStack;
                    if (bulletItem == ModItems.MAGNUM_BULLET) foundType = "magnum_bullet";
                    else if (bulletItem == ModItems.MAGNUM_HEAVY_BULLET) foundType = "magnum_heavy_bullet";
                    else if (bulletItem == ModItems.MAGNUM_FIRE_BULLET) foundType = "magnum_fire_bullet";
                    break;
                }
            }
            if (!bulletStack.isEmpty()) break;
        }

        if (bulletStack.isEmpty()) {
            // 弾なし → カチッ音
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.PLAYERS, 0.5f, 1.5f);
            return TypedActionResult.fail(stack);
        }

        // 装填
        int toLoad = Math.min(magazineSize, bulletStack.getCount());
        bulletStack.decrement(toLoad);
        nbt.putInt("Loaded", toLoad);
        nbt.putString("BulletType", foundType);

        // リロード音
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.BLOCK_IRON_DOOR_CLOSE, SoundCategory.PLAYERS, 0.5f, 1.5f);

        player.getItemCooldownManager().set(this, reloadCooldown);
        return TypedActionResult.success(stack, world.isClient);
    }

    private float getDamageMultiplier(String bulletType) {
        return switch (bulletType) {
            case "magnum_heavy_bullet" -> 1.5f;
            case "magnum_fire_bullet" -> 1.0f;
            case "magnum_bullet" -> 1.0f;
            default -> 1.0f;
        };
    }

    protected float getFirePitch() {
        return 0.8f;
    }

    // 装弾バー表示
    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        int loaded = (nbt != null) ? nbt.getInt("Loaded") : 0;
        return Math.round((float) loaded / magazineSize * 13.0f);
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        int loaded = (nbt != null) ? nbt.getInt("Loaded") : 0;
        String type = (nbt != null) ? nbt.getString("BulletType") : "";
        if ("magnum_fire_bullet".equals(type)) return 0xFF4500;   // オレンジ
        if ("magnum_heavy_bullet".equals(type)) return 0xFFD700;  // ゴールド
        if (loaded <= 1) return 0xFF0000;                    // 赤
        if (loaded <= 3) return 0xFFAA00;                    // 橙
        return 0x00FF00;                                     // 緑
    }
}
