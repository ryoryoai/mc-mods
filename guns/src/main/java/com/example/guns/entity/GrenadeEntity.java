package com.example.guns.entity;

import com.example.guns.ModEntities;
import com.example.guns.ModItems;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.List;

public class GrenadeEntity extends ThrownItemEntity {
    private String grenadeType = "grenade_launcher_fire_grenade";
    private static final int EFFECT_RADIUS = 4;

    // Deserialization constructor (required by EntityType)
    public GrenadeEntity(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    // Gameplay constructor
    public GrenadeEntity(World world, LivingEntity owner, String grenadeType) {
        super(ModEntities.GRENADE, owner, world);
        this.grenadeType = grenadeType;
        setItem(new ItemStack(getGrenadeItem(grenadeType)));
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.GRENADE_LAUNCHER_FIRE_GRENADE;
    }

    @Override
    protected float getGravity() {
        return 0.05f;
    }

    private Item getGrenadeItem(String type) {
        return switch (type) {
            case "grenade_launcher_freeze_grenade" -> ModItems.GRENADE_LAUNCHER_FREEZE_GRENADE;
            case "grenade_launcher_lightning_grenade" -> ModItems.GRENADE_LAUNCHER_LIGHTNING_GRENADE;
            case "grenade_launcher_acid_grenade" -> ModItems.GRENADE_LAUNCHER_ACID_GRENADE;
            case "grenade_launcher_mining_grenade" -> ModItems.GRENADE_LAUNCHER_MINING_GRENADE;
            case "grenade_launcher_mini_bomb" -> ModItems.GRENADE_LAUNCHER_MINI_BOMB;
            case "grenade_launcher_bomb" -> ModItems.GRENADE_LAUNCHER_BOMB;
            case "grenade_launcher_mega_bomb" -> ModItems.GRENADE_LAUNCHER_MEGA_BOMB;
            default -> ModItems.GRENADE_LAUNCHER_FIRE_GRENADE;
        };
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putString("GrenadeType", grenadeType);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("GrenadeType")) {
            grenadeType = nbt.getString("GrenadeType");
        }
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        if (!getWorld().isClient) {
            // Explosion sound
            getWorld().playSound(null, getX(), getY(), getZ(),
                SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS,
                1.5f, 0.9f + getWorld().getRandom().nextFloat() * 0.2f);

            // Apply type-specific effect
            applyEffect();

            // Explosion particles
            ServerWorld serverWorld = (ServerWorld) getWorld();
            serverWorld.spawnParticles(ParticleTypes.EXPLOSION_EMITTER,
                getX(), getY(), getZ(), 1, 0, 0, 0, 0);
            serverWorld.spawnParticles(ParticleTypes.EXPLOSION,
                getX(), getY(), getZ(), 6, 1.5, 1.0, 1.5, 0);

            discard();
        }
    }

    private void applyEffect() {
        switch (grenadeType) {
            case "grenade_launcher_freeze_grenade" -> applyFreezeEffect();
            case "grenade_launcher_lightning_grenade" -> applyLightningEffect();
            case "grenade_launcher_fire_grenade" -> applyFireEffect();
            case "grenade_launcher_acid_grenade" -> applyAcidEffect();
            case "grenade_launcher_mining_grenade" -> applyMiningEffect();
            case "grenade_launcher_mini_bomb" -> applyBombEffect(2.0f);
            case "grenade_launcher_bomb" -> applyBombEffect(4.0f);
            case "grenade_launcher_mega_bomb" -> applyBombEffect(8.0f);
        }
    }

    // ========== こおりグレネード ==========
    // 範囲に雪と氷を設置、敵を凍結+鈍足
    private void applyFreezeEffect() {
        World world = getWorld();
        BlockPos center = BlockPos.ofFloored(getX(), getY(), getZ());

        for (int x = -EFFECT_RADIUS; x <= EFFECT_RADIUS; x++) {
            for (int y = -1; y <= 2; y++) {
                for (int z = -EFFECT_RADIUS; z <= EFFECT_RADIUS; z++) {
                    double dist = Math.sqrt(x * x + y * y + z * z);
                    if (dist > EFFECT_RADIUS) continue;

                    BlockPos pos = center.add(x, y, z);
                    BlockState state = world.getBlockState(pos);

                    // Water → Ice
                    if (state.isOf(Blocks.WATER)) {
                        world.setBlockState(pos, Blocks.ICE.getDefaultState());
                    }
                    // Air above solid → Snow layer
                    if (state.isAir()) {
                        BlockState below = world.getBlockState(pos.down());
                        if (!below.isAir() && below.isSolid()) {
                            world.setBlockState(pos, Blocks.SNOW.getDefaultState());
                        }
                    }
                }
            }
        }

        // Entities: Slowness IV (5s) + Mining Fatigue II (5s) + Freeze visual
        for (LivingEntity entity : getEntitiesInRadius()) {
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 3));
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 100, 1));
            entity.setFrozenTicks(200);
        }

        // Ice particles
        ServerWorld serverWorld = (ServerWorld) world;
        serverWorld.spawnParticles(ParticleTypes.SNOWFLAKE,
            getX(), getY() + 0.5, getZ(), 50, 2.5, 1.5, 2.5, 0.05);
        // Freeze sound
        world.playSound(null, getX(), getY(), getZ(),
            SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.PLAYERS, 1.0f, 1.5f);
    }

    // ========== かみなりグレネード ==========
    // 着弾地点に雷を落とす
    private void applyLightningEffect() {
        World world = getWorld();

        // Summon lightning bolt
        LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(world);
        if (lightning != null) {
            lightning.refreshPositionAfterTeleport(getX(), getY(), getZ());
            world.spawnEntity(lightning);
        }

        // Extra electric particles
        ServerWorld serverWorld = (ServerWorld) world;
        serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
            getX(), getY() + 0.5, getZ(), 30, 1.5, 2.0, 1.5, 0.1);

        // Entities in radius also get Glowing (so you can see what got hit)
        for (LivingEntity entity : getEntitiesInRadius()) {
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 100, 0));
        }
    }

    // ========== ほのおグレネード ==========
    // 範囲に火を設置、敵を燃やす
    private void applyFireEffect() {
        World world = getWorld();
        BlockPos center = BlockPos.ofFloored(getX(), getY(), getZ());

        for (int x = -EFFECT_RADIUS; x <= EFFECT_RADIUS; x++) {
            for (int y = -1; y <= 3; y++) {
                for (int z = -EFFECT_RADIUS; z <= EFFECT_RADIUS; z++) {
                    double dist = Math.sqrt(x * x + z * z);
                    if (dist > EFFECT_RADIUS) continue;

                    BlockPos pos = center.add(x, y, z);
                    BlockState state = world.getBlockState(pos);

                    // Place fire on solid surfaces
                    if (state.isAir()) {
                        BlockState below = world.getBlockState(pos.down());
                        if (!below.isAir() && below.isSolid()) {
                            world.setBlockState(pos, Blocks.FIRE.getDefaultState());
                        }
                    }
                }
            }
        }

        // Set entities on fire + direct damage
        for (LivingEntity entity : getEntitiesInRadius()) {
            entity.setOnFireFor(8);
            entity.damage(getWorld().getDamageSources().onFire(), 6.0f);
        }

        // Flame particles
        ServerWorld serverWorld = (ServerWorld) world;
        serverWorld.spawnParticles(ParticleTypes.FLAME,
            getX(), getY() + 0.5, getZ(), 60, 2.5, 1.5, 2.5, 0.08);
        serverWorld.spawnParticles(ParticleTypes.LAVA,
            getX(), getY() + 0.5, getZ(), 15, 2.0, 1.0, 2.0, 0.0);
    }

    // ========== ようかいグレネード ==========
    // 弱いブロックを溶かす、敵にウィザー+毒
    private void applyAcidEffect() {
        World world = getWorld();
        BlockPos center = BlockPos.ofFloored(getX(), getY(), getZ());

        for (int x = -EFFECT_RADIUS; x <= EFFECT_RADIUS; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -EFFECT_RADIUS; z <= EFFECT_RADIUS; z++) {
                    double dist = Math.sqrt(x * x + y * y + z * z);
                    if (dist > EFFECT_RADIUS) continue;

                    BlockPos pos = center.add(x, y, z);
                    BlockState state = world.getBlockState(pos);

                    // Dissolve weak/organic blocks
                    if (state.isIn(BlockTags.LEAVES) ||
                        state.isIn(BlockTags.FLOWERS) ||
                        state.isIn(BlockTags.WOOL) ||
                        state.isOf(Blocks.SNOW) ||
                        state.isOf(Blocks.SNOW_BLOCK) ||
                        state.isOf(Blocks.ICE) ||
                        state.isOf(Blocks.PACKED_ICE) ||
                        state.isOf(Blocks.COBWEB) ||
                        state.isOf(Blocks.GRASS) ||
                        state.isOf(Blocks.TALL_GRASS) ||
                        state.isOf(Blocks.VINE) ||
                        state.isOf(Blocks.MOSS_BLOCK) ||
                        state.isOf(Blocks.MOSS_CARPET)) {
                        world.breakBlock(pos, false);
                    }
                }
            }
        }

        // Wither II (5s) + Poison II (4s)
        for (LivingEntity entity : getEntitiesInRadius()) {
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 100, 1));
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 80, 1));
        }

        // Green slime/acid particles
        ServerWorld serverWorld = (ServerWorld) world;
        serverWorld.spawnParticles(ParticleTypes.ITEM_SLIME,
            getX(), getY() + 0.5, getZ(), 40, 2.5, 1.5, 2.5, 0.1);
        // Acid hiss sound
        world.playSound(null, getX(), getY(), getZ(),
            SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.PLAYERS, 1.0f, 0.8f);
    }

    // ========== ボムグレネード ==========
    // TNTのように爆発する（強さ可変）
    private void applyBombEffect(float power) {
        getWorld().createExplosion(
            this, getX(), getY(), getZ(), power,
            World.ExplosionSourceType.TNT
        );
    }

    // ========== マイニンググレネード ==========
    // 範囲のブロックをドロップ付きで破壊（採掘）
    private void applyMiningEffect() {
        World world = getWorld();
        BlockPos center = BlockPos.ofFloored(getX(), getY(), getZ());
        int mineRadius = 3;

        for (int x = -mineRadius; x <= mineRadius; x++) {
            for (int y = -mineRadius; y <= mineRadius; y++) {
                for (int z = -mineRadius; z <= mineRadius; z++) {
                    double dist = Math.sqrt(x * x + y * y + z * z);
                    if (dist > mineRadius) continue;

                    BlockPos pos = center.add(x, y, z);
                    BlockState state = world.getBlockState(pos);

                    // Skip air, bedrock, and fluids
                    if (state.isAir() || state.isOf(Blocks.BEDROCK)
                        || state.isOf(Blocks.WATER) || state.isOf(Blocks.LAVA)) continue;

                    // breakBlock(pos, true) = drop items
                    world.breakBlock(pos, true);
                }
            }
        }

        // Rock debris particles
        ServerWorld serverWorld = (ServerWorld) world;
        serverWorld.spawnParticles(ParticleTypes.CRIT,
            getX(), getY() + 0.5, getZ(), 50, 2.0, 1.5, 2.0, 0.1);
        serverWorld.spawnParticles(ParticleTypes.SMOKE,
            getX(), getY() + 0.5, getZ(), 30, 2.0, 1.0, 2.0, 0.05);

        // Mining blast sound
        world.playSound(null, getX(), getY(), getZ(),
            SoundEvents.ENTITY_WITHER_BREAK_BLOCK, SoundCategory.PLAYERS, 1.0f, 0.7f);
    }

    private List<LivingEntity> getEntitiesInRadius() {
        Box box = new Box(
            getX() - EFFECT_RADIUS, getY() - EFFECT_RADIUS, getZ() - EFFECT_RADIUS,
            getX() + EFFECT_RADIUS, getY() + EFFECT_RADIUS, getZ() + EFFECT_RADIUS
        );
        return getWorld().getEntitiesByClass(LivingEntity.class, box,
            entity -> entity.squaredDistanceTo(getPos()) <= EFFECT_RADIUS * EFFECT_RADIUS);
    }
}
