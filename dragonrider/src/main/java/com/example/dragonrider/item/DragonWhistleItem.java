package com.example.dragonrider.item;

import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.List;

public class DragonWhistleItem extends Item {
    private static final int COOLDOWN_TICKS = 100; // 5秒
    private static final double SEARCH_RADIUS = 200.0;

    public DragonWhistleItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        // 笛の音
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.BLOCK_NOTE_BLOCK_FLUTE.value(), SoundCategory.PLAYERS,
            2.0f, 0.5f);

        if (!world.isClient) {
            ServerWorld serverWorld = (ServerWorld) world;

            // 範囲内のエンダードラゴンを検索
            Box searchBox = new Box(
                player.getX() - SEARCH_RADIUS, player.getY() - SEARCH_RADIUS, player.getZ() - SEARCH_RADIUS,
                player.getX() + SEARCH_RADIUS, player.getY() + SEARCH_RADIUS, player.getZ() + SEARCH_RADIUS
            );
            List<EnderDragonEntity> dragons = serverWorld.getEntitiesByClass(
                EnderDragonEntity.class, searchBox, d -> true);

            EnderDragonEntity dragon;
            if (!dragons.isEmpty()) {
                // 一番近いドラゴンを呼び寄せる
                dragon = dragons.get(0);
                double minDist = dragon.squaredDistanceTo(player);
                for (EnderDragonEntity d : dragons) {
                    double dist = d.squaredDistanceTo(player);
                    if (dist < minDist) {
                        minDist = dist;
                        dragon = d;
                    }
                }
                // プレイヤーの前方にテレポート
                double yaw = Math.toRadians(player.getYaw());
                double tx = player.getX() - Math.sin(yaw) * 5;
                double tz = player.getZ() + Math.cos(yaw) * 5;
                dragon.setPosition(tx, player.getY() + 3, tz);

                player.sendMessage(Text.literal("ドラゴンを よびよせた！").formatted(Formatting.LIGHT_PURPLE), true);
            } else {
                // ドラゴンがいない → 召喚
                dragon = new EnderDragonEntity(net.minecraft.entity.EntityType.ENDER_DRAGON, world);
                double yaw = Math.toRadians(player.getYaw());
                double tx = player.getX() - Math.sin(yaw) * 5;
                double tz = player.getZ() + Math.cos(yaw) * 5;
                dragon.setPosition(tx, player.getY() + 3, tz);
                world.spawnEntity(dragon);

                player.sendMessage(Text.literal("ドラゴンを しょうかんした！").formatted(Formatting.LIGHT_PURPLE), true);
            }

            // テレポートエフェクト音
            world.playSound(null, dragon.getX(), dragon.getY(), dragon.getZ(),
                SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.NEUTRAL,
                1.5f, 0.8f);

            // 騎乗
            player.startRiding(dragon, true);
        }

        player.getItemCooldownManager().set(this, COOLDOWN_TICKS);
        return TypedActionResult.success(stack, world.isClient);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true; // エンチャントのキラキラ
    }
}
