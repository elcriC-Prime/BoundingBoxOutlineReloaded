package com.irtimaled.bbor.common.messages;

import com.irtimaled.bbor.common.BBORCustomPayload;
import com.irtimaled.bbor.common.models.Coords;
import com.irtimaled.bbor.common.models.DimensionId;
import net.minecraft.network.PacketByteBuf;

public class PayloadReader {
    private final PacketByteBuf buffer;

    public PayloadReader(BBORCustomPayload payload) {
        this.buffer = payload.byteBuf();
        if (this.buffer != null) {
            this.buffer.readerIndex(0);
        }
    }

    public PayloadReader(PacketByteBuf buffer) {
        this.buffer = buffer;
        if (this.buffer != null) {
            this.buffer.readerIndex(0);
        }
    }

    public PacketByteBuf handle() {
        return buffer;
    }

    public long readLong() {
        return buffer.readLong();
    }

    public int readInt() {
        return buffer.readInt();
    }

    public int readVarInt() {
        return buffer.readVarInt();
    }

    public boolean isReadable() {
        return buffer.isReadable();
    }

    public boolean isReadable(int count) {
        return buffer.isReadable(count);
    }

    public char readChar() {
        return buffer.readChar();
    }

    public Coords readCoords() {
        int x = readVarInt();
        int y = readVarInt();
        int z = readVarInt();
        return new Coords(x, y, z);
    }

    public DimensionId readDimensionId() {
        return DimensionId.from(buffer.readIdentifier());
    }

    public String readString() {
        return buffer.readString();
    }
}
