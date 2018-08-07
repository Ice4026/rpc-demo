package com.example.rpc.handler;

import com.example.rpc.domain.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Xia Yubing on 2018/8/6
 */
public class RpcResponseHandler extends SimpleChannelInboundHandler<RpcResponse> {
    Map<String, CountDownLatch> locks;
    Map<String, RpcResponse> responses;

    public RpcResponseHandler(Map<String, CountDownLatch> locks, Map<String, RpcResponse> responses) {
        this.locks = locks;
        this.responses = responses;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse msg) throws Exception {
        responses.put(msg.getRequestId(), msg);
        CountDownLatch reentrantLock = locks.get(msg.getRequestId());
        locks.remove(msg.getRequestId());
        reentrantLock.countDown();
    }
}
