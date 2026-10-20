package com.irtimaled.bbor.client.providers;

import com.irtimaled.bbor.client.config.BoundingBoxTypeHelper;
import com.irtimaled.bbor.client.interop.ClientWorldUpdateTracker;
import com.irtimaled.bbor.client.models.BoundingBoxMobSpawner;
import com.irtimaled.bbor.common.BoundingBoxType;
import com.irtimaled.bbor.common.EventBus;
import com.irtimaled.bbor.common.models.Coords;
import com.irtimaled.bbor.common.models.DimensionId;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;

public class MobSpawnerProvider implements IBoundingBoxProvider<BoundingBoxMobSpawner>, ICachingProvider {

    private static final Long2ObjectMap<BoundingBoxMobSpawner> boxes = Long2ObjectMaps.synchronize(new Long2ObjectLinkedOpenHashMap<>(), MobSpawnerProvider.class);
    private static volatile ObjectList<BoundingBoxMobSpawner> boxesCopy = new ObjectArrayList<>(boxes.values());

    static {
        EventBus.subscribe(ClientWorldUpdateTracker.BlockEntityAddEvent.class, event -> {
            updateOrCreateMobSpawner(event.blockEntity());
        });
        EventBus.subscribe(ClientWorldUpdateTracker.BlockEntityUpdateEvent.class, event -> {
            updateOrCreateMobSpawner(event.blockEntity());
        });
        EventBus.subscribe(ClientWorldUpdateTracker.BlockEntityRemoveEvent.class, event -> {
            if (boxes.remove(BlockPos.asLong(event.x(), event.y(), event.z())) != null) {
                updateCopy();
            }
        });
        EventBus.subscribe(ClientWorldUpdateTracker.ChunkUnloadEvent.class, event -> {
            boolean removed = false;
            synchronized (MobSpawnerProvider.class) {
                var iterator = boxes.long2ObjectEntrySet().iterator();
                while (iterator.hasNext()) {
                    long key = iterator.next().getLongKey();
                    if (ChunkSectionPos.getSectionCoord(BlockPos.unpackLongX(key)) == event.x() &&
                            ChunkSectionPos.getSectionCoord(BlockPos.unpackLongZ(key)) == event.z()) {
                        iterator.remove();
                        removed = true;
                    }
                }
            }
            if (removed) {
                updateCopy();
            }
        });
        EventBus.subscribe(ClientWorldUpdateTracker.WorldResetEvent.class, event -> {
            boxes.clear();
            updateCopy();
        });
    }

    public static void updateOrCreateMobSpawner(BlockEntity blockEntity) {
        if (blockEntity instanceof MobSpawnerBlockEntity mobSpawner) {
            final long key = blockEntity.getPos().asLong();
            if (boxes.containsKey(key)) return;
            Coords coords = new Coords(mobSpawner.getPos());
            boxes.put(key, BoundingBoxMobSpawner.from(coords));
            updateCopy();
        }
    }

    private static void updateCopy() {
        synchronized (MobSpawnerProvider.class) {
            boxesCopy = new ObjectArrayList<>(boxes.values());
        }
    }

    @Override
    public void clearCache() {
        boxes.clear();
        updateCopy();
    }

    @Override
    public boolean canProvide(DimensionId dimensionId) {
        return BoundingBoxTypeHelper.shouldRender(BoundingBoxType.MobSpawner);
    }

    @Override
    public Iterable<BoundingBoxMobSpawner> get(DimensionId dimensionId) {
        return boxesCopy;
    }
}
