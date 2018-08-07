package com.example.rpc.serializer;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author Xia Yubing on 2018/8/2
 */
public class RpcEncoder extends MessageToByteEncoder {
    private Class clazz;

    public RpcEncoder(Class clazz) {
        this.clazz = clazz;
    }
    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        if (!clazz.isInstance(msg)) {
            return;
        }
        byte[] data = SerializationUtil.serialize(msg);
        out.writeInt(data.length);
        out.writeBytes(data);
    }
}
