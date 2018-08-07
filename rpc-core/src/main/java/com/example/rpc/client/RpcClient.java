package com.example.rpc.client;

import com.example.rpc.discovery.ServiceDiscovery;
import com.example.rpc.domain.RpcRequest;
import com.example.rpc.domain.RpcResponse;
import com.example.rpc.handler.RpcResponseHandler;
import com.example.rpc.serializer.RpcDecoder;
import com.example.rpc.serializer.RpcEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;


/**
 * @author Xia Yubing on 2018/8/6
 */
public class RpcClient {
    private Bootstrap bootstrap;
    private ServiceDiscovery serviceDiscovery;

    // requestId mapping to lock
    private Map<String, CountDownLatch> locks = new ConcurrentHashMap<>();
    // requestId mapping to RpcResponses
    private Map<String, RpcResponse> responses = new ConcurrentHashMap<>();

    public RpcClient(String registerAddress) {
        serviceDiscovery = new ServiceDiscovery(registerAddress);
        init();
    }

    public RpcResponse send(RpcRequest rpcRequest) {
        try {
            String address = serviceDiscovery.discover(rpcRequest.getClassName());
            String[] hostAndPort = address.split(":");

            ChannelFuture channelFuture = bootstrap.connect(hostAndPort[0], Integer.parseInt(hostAndPort[1])).sync();
//            ReentrantLock reentrantLock = new ReentrantLock();
            CountDownLatch countDownLatch = new CountDownLatch(1);
            locks.put(rpcRequest.getRequestId(), countDownLatch);
            channelFuture.channel().writeAndFlush(rpcRequest);
//            reentrantLock.lock();
            countDownLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return responses.get(rpcRequest.getRequestId());
    }

    public void init() {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new RpcEncoder(RpcRequest.class))
                                .addLast(new RpcDecoder(RpcResponse.class))
                                .addLast(new RpcResponseHandler(locks, responses));
                    }
                })
                .option(ChannelOption.SO_KEEPALIVE, true);
    }
}
