package com.irtimaled.bbor.client.interop;

import com.irtimaled.bbor.client.config.ConfigManager;
import com.irtimaled.bbor.common.models.Coords;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.SpawnSettings;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class SpawnableBlocksHelper {
//    private static final EntityType entityType = EntityType.Builder.create(SpawnGroup.MONSTER)
//            .setDimensions(0f, 0f).disableSaving().build(null);

    private static final Identifier MUSHROOM_FIELDS_ID = Identifier.ofVanilla("mushroom_fields");
    private static final Identifier DEEP_DARK_ID = Identifier.ofVanilla("deep_dark");

    public static void findSpawnableBlocks(Coords coords, int width, int height, BlockProcessor blockProcessor) {
        int blockX = coords.getX();
        int minX = blockX - width;
        int maxX = blockX + width + 1;

        int blockZ = coords.getZ();
        int minZ = blockZ - width;
        int maxZ = blockZ + width + 1;

        World world = MinecraftClient.getInstance().world;
        if (world == null) return;
        int blockY = coords.getY();
        int minY = Math.max(world.getBottomY() + 1, blockY - height);
        int maxY = Math.min(world.getTopY() - 1, blockY + height);

        BlockPos.Mutable pos = new BlockPos.Mutable();
        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                pos.set(x, minY - 1, z);
                BlockState upperBlockState = world.getBlockState(pos);
                for (int y = minY; y < maxY; y++) {
                    BlockState spawnBlockState = upperBlockState;
                    pos.set(x, y, z);
                    upperBlockState = world.getBlockState(pos);
                    if (isSpawnable(world, pos, spawnBlockState, upperBlockState)) {
                        blockProcessor.process(pos.toImmutable());
                    }
                }
            }
        }
    }

    private static final ConcurrentHashMap<Biome, Boolean> isNether = new ConcurrentHashMap<>();

    public static boolean isSpawnable(World world, BlockPos pos, BlockState spawnBlockState, BlockState upperBlockState) {
        VoxelShape collisionShape = upperBlockState.getCollisionShape(world, pos);
        if (!upperBlockState.emitsRedstonePower() &&
                !upperBlockState.isIn(BlockTags.RAILS) &&
                collisionShape.getMax(Direction.Axis.Y) <= 0 &&
                upperBlockState.getFluidState().isEmpty() &&
                (world.getLightLevel(LightType.BLOCK, pos) <= ConfigManager.spawnableBlocksSafeLight.get()) &&
                !Block.isFaceFullSquare(collisionShape, Direction.UP)
        ) {
            final RegistryEntry<Biome> biomeRegistryEntry = world.getBiome(pos);
            Biome biome = biomeRegistryEntry.value();
            final boolean isNether = SpawnableBlocksHelper.isNether.computeIfAbsent(biome, biome1 -> {
                final SpawnSettings spawnSettings = biome.getSpawnSettings();
                final List<SpawnSettings.SpawnEntry> entries = spawnSettings.getSpawnEntries(SpawnGroup.MONSTER).getEntries();
                for (SpawnSettings.SpawnEntry entry : entries) {
                    if (entry.type.getBaseClass() == ZombifiedPiglinEntity.class) return true;
                }
                return false;
            });
            final Identifier id = biomeRegistryEntry.getKey().map(net.minecraft.registry.RegistryKey::getValue).orElse(null);
            return spawnBlockState.allowsSpawning(world, pos.down(), isNether ? EntityType.ZOMBIFIED_PIGLIN : EntityType.ZOMBIE) &&
                    (id == null || (!id.equals(MUSHROOM_FIELDS_ID) && !id.equals(DEEP_DARK_ID)));
        }
        return false;
    }
}
