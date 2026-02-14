package com.example.guns.item;

import com.example.guns.ModItems;
import com.example.guns.entity.GrenadeEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

public class GrenadeLauncherItem extends Item {
    private static final int RELOAD_COOLDOWN = 20;  // 1秒 リロード
    private static final int FIRE_COOLDOWN = 10;     // 0.5秒 発射後
    private static final float LAUNCH_SPEED = 1.5f;
    private static final float DIVERGENCE = 0.5f;

    public GrenadeLauncherItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        NbtCompound nbt = stack.getOrCreateNbt();
        String loadedType = nbt.getString("GrenadeType");

        // 装填済み → 発射
        if (!loadedType.isEmpty()) {
            return fire(world, player, hand, stack, nbt, loadedType);
        }

        // 未装填 → リロード
        return reload(world, player, stack, nbt);
    }

    private TypedActionResult<ItemStack> fire(World world, PlayerEntity player, Hand hand,
                                               ItemStack stack, NbtCompound nbt, String grenadeType) {
        // 発射音（低い重厚なトンッ）
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.PLAYERS,
            1.5f, 0.4f);

        if (!world.isClient) {
            GrenadeEntity grenade = new GrenadeEntity(world, player, grenadeType);
            grenade.setVelocity(player, player.getPitch(), player.getYaw(),
                0.0f, LAUNCH_SPEED, DIVERGENCE);
            world.spawnEntity(grenade);

            stack.damage(1, player, p -> p.sendToolBreakStatus(hand));
        }

        // 弾を空にする
        nbt.putString("GrenadeType", "");

        player.getItemCooldownManager().set(this, FIRE_COOLDOWN);
        return TypedActionResult.success(stack, world.isClient);
    }

    private TypedActionResult<ItemStack> reload(World world, PlayerEntity player,
                                                 ItemStack stack, NbtCompound nbt) {
        // インベントリからグレネード弾を探す
        ItemStack grenadeStack = findGrenade(player);
        if (grenadeStack.isEmpty()) {
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.PLAYERS, 0.5f, 1.5f);
            return TypedActionResult.fail(stack);
        }

        String grenadeType = getGrenadeType(grenadeStack);
        grenadeStack.decrement(1);

        // 装填
        nbt.putString("GrenadeType", grenadeType);

        // ガチャン音
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.BLOCK_IRON_DOOR_CLOSE, SoundCategory.PLAYERS, 0.8f, 1.2f);

        player.getItemCooldownManager().set(this, RELOAD_COOLDOWN);
        return TypedActionResult.success(stack, world.isClient);
    }

    private ItemStack findGrenade(PlayerEntity player) {
        Item[] priority = {
            ModItems.GRENADE_LAUNCHER_LIGHTNING_GRENADE,
            ModItems.GRENADE_LAUNCHER_FREEZE_GRENADE,
            ModItems.GRENADE_LAUNCHER_FIRE_GRENADE,
            ModItems.GRENADE_LAUNCHER_ACID_GRENADE,
            ModItems.GRENADE_LAUNCHER_MINING_GRENADE,
            ModItems.GRENADE_LAUNCHER_MEGA_BOMB,
            ModItems.GRENADE_LAUNCHER_BOMB,
            ModItems.GRENADE_LAUNCHER_MINI_BOMB
        };

        for (Item grenadeItem : priority) {
            for (int i = 0; i < player.getInventory().size(); i++) {
                ItemStack invStack = player.getInventory().getStack(i);
                if (invStack.getItem() == grenadeItem) {
                    return invStack;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    private String getGrenadeType(ItemStack stack) {
        Item item = stack.getItem();
        if (item == ModItems.GRENADE_LAUNCHER_FREEZE_GRENADE) return "grenade_launcher_freeze_grenade";
        if (item == ModItems.GRENADE_LAUNCHER_LIGHTNING_GRENADE) return "grenade_launcher_lightning_grenade";
        if (item == ModItems.GRENADE_LAUNCHER_ACID_GRENADE) return "grenade_launcher_acid_grenade";
        if (item == ModItems.GRENADE_LAUNCHER_MINING_GRENADE) return "grenade_launcher_mining_grenade";
        if (item == ModItems.GRENADE_LAUNCHER_MINI_BOMB) return "grenade_launcher_mini_bomb";
        if (item == ModItems.GRENADE_LAUNCHER_BOMB) return "grenade_launcher_bomb";
        if (item == ModItems.GRENADE_LAUNCHER_MEGA_BOMB) return "grenade_launcher_mega_bomb";
        return "grenade_launcher_fire_grenade";
    }

    // 装填バー表示（装填中は緑、空は非表示）
    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        return nbt != null && !nbt.getString("GrenadeType").isEmpty();
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        return 13; // フル表示（1発装填 = 満タン）
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        String type = (nbt != null) ? nbt.getString("GrenadeType") : "";
        return switch (type) {
            case "grenade_launcher_freeze_grenade" -> 0x00BFFF;     // 水色
            case "grenade_launcher_lightning_grenade" -> 0xFFD700;   // 金色
            case "grenade_launcher_fire_grenade" -> 0xFF4500;        // 赤オレンジ
            case "grenade_launcher_acid_grenade" -> 0x00FF00;        // 緑
            case "grenade_launcher_mining_grenade" -> 0xC0C0C0;     // シルバー
            case "grenade_launcher_mini_bomb" -> 0x888888;          // グレー
            case "grenade_launcher_bomb" -> 0xFF0000;               // 赤
            case "grenade_launcher_mega_bomb" -> 0x8B0000;          // ダークレッド
            default -> 0xAAAAAA;
        };
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        NbtCompound nbt = stack.getNbt();
        String type = (nbt != null) ? nbt.getString("GrenadeType") : "";

        if (type.isEmpty()) {
            tooltip.add(Text.literal("右クリック: 装填").formatted(Formatting.GRAY));
        } else {
            String name = switch (type) {
                case "grenade_launcher_freeze_grenade" -> "こおり";
                case "grenade_launcher_lightning_grenade" -> "かみなり";
                case "grenade_launcher_fire_grenade" -> "ほのお";
                case "grenade_launcher_acid_grenade" -> "ようかい";
                case "grenade_launcher_mining_grenade" -> "マイニング";
                case "grenade_launcher_mini_bomb" -> "ミニボム";
                case "grenade_launcher_bomb" -> "ボム";
                case "grenade_launcher_mega_bomb" -> "メガボム";
                default -> "???";
            };
            tooltip.add(Text.literal("装填中: " + name + "グレネード").formatted(Formatting.GREEN));
            tooltip.add(Text.literal("右クリック: 発射").formatted(Formatting.GRAY));
        }
    }
}
