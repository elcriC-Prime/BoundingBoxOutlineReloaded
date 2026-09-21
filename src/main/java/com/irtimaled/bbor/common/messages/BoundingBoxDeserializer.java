package com.irtimaled.bbor.common.messages;

import com.irtimaled.bbor.common.BoundingBoxType;
import com.irtimaled.bbor.common.models.AbstractBoundingBox;
import com.irtimaled.bbor.common.models.BoundingBoxCuboid;
import com.irtimaled.bbor.common.models.Coords;

class BoundingBoxDeserializer {
    static AbstractBoundingBox deserialize(PayloadReader reader) {
        if (!reader.isReadable(2)) return null;

        char type = reader.readChar();
        return type == 'S' ? deserializeStructure(reader) : null;
    }

    private static AbstractBoundingBox deserializeStructure(PayloadReader reader) {
        if (!reader.isReadable(28)) return null;
        BoundingBoxType type = BoundingBoxType.getByNameHash(reader.readInt());
        Coords minCoords = reader.readCoords();
        Coords maxCoords = reader.readCoords();
        if (type == null) return null;
        return BoundingBoxCuboid.from(minCoords, maxCoords, type);
    }
}
