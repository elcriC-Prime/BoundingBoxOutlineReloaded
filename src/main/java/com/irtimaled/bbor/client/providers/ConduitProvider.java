package com.irtimaled.bbor.client.providers;

import com.irtimaled.bbor.client.config.BoundingBoxTypeHelper;
import com.irtimaled.bbor.client.interop.ClientWorldUpdateTracker;
import com.irtimaled.bbor.client.models.BoundingBoxConduit;
import com.irtimaled.bbor.common.BoundingBoxType;
import com.irtimaled.bbor.common.EventBus;
import com.irtimaled.bbor.common.models.Coords;
import com.irtimaled.bbor.common.models.DimensionId;
import com.irtimaled.bbor.mixin.client.access.IConduitBlockEntity;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ConduitBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;

public class ConduitProvider implements IBoundingBoxProvider<BoundingBoxConduit>, ICachingProvider {

    private static final Long2ObjectMap<BoundingBoxConduit> boxes = Long2ObjectMaps.synchronize(new Long2ObjectLinkedOpenHashMap<>(), ConduitProvider.class);
    private static volatile ObjectList<BoundingBoxConduit> boxesCopy = new ObjectArrayList<>(boxes.values());

    static {
        EventBus.subscribe(ClientWorldUpdateTracker.BlockEntityAddEvent.class, event -> {
            updateOrCreateConduit(event.blockEntity());
        });
        EventBus.subscribe(ClientWorldUpdateTracker.BlockEntityUpdateEvent.class, event -> {
            updateOrCreateConduit(event.blockEntity());
        });
        EventBus.subscribe(ClientWorldUpdateTracker.BlockEntityRemoveEvent.class, event -> {
            if (boxes.remove(BlockPos.asLong(event.x(), event.y(), event.z())) != null) {
                updateCopy();
            }
        });
        EventBus.subscribe(ClientWorldUpdateTracker.ChunkUnloadEvent.class, event -> {
            boolean removed = false;
            synchronized (ConduitProvider.class) {
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

    public static void updateOrCreateConduit(BlockEntity blockEntity) {
        if (blockEntity instanceof ConduitBlockEntity conduit) {
            int levels = ((IConduitBlockEntity) conduit).getActivatingBlocks().size() / 7;
            final long key = blockEntity.getPos().asLong();
            BoundingBoxConduit existing = boxes.get(key);
            if (existing != null && existing.getLevel() == levels) return;
            Coords coords = new Coords(conduit.getPos());
            boxes.put(key, new BoundingBoxConduit(coords, levels));
            updateCopy();
        }
    }

    public static void updateCopy() {
        synchronized (ConduitProvider.class) {
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
        return BoundingBoxTypeHelper.shouldRender(BoundingBoxType.Conduit);
    }

    @Override
    public Iterable<BoundingBoxConduit> get(DimensionId dimensionId) {
        return boxesCopy;
    }
}
