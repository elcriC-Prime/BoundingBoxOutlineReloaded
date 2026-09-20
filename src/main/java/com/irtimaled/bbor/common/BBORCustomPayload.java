package com.irtimaled.bbor.common;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record BBORCustomPayload(PacketByteBuf byteBuf, Identifier id) implements CustomPayload {

    public BBORCustomPayload(Identifier identifier, PacketByteBuf buf) {
        this(buf != null ? new PacketByteBuf(buf.copy()) : new PacketByteBuf(Unpooled.buffer()), identifier);
    }

    public static PacketCodec<PacketByteBuf, BBORCustomPayload> codec(Identifier id) {
        return CustomPayload.codecOf(
                (payload, buf) -> {
                    PacketByteBuf data = payload.byteBuf();
                    if (data != null && data.readableBytes() > 0) {
                        buf.writeBytes(data.slice(data.readerIndex(), data.readableBytes()));
                    }
                },
                buf -> {
                    int readable = buf.readableBytes();
                    PacketByteBuf copy = new PacketByteBuf(readable > 0 ? buf.readBytes(readable) : Unpooled.buffer());
                    return new BBORCustomPayload(copy, id);
                }
        );
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return new Id<>(id);
    }
}
