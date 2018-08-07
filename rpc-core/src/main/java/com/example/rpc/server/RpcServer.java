package com.example.rpc.server;

import com.example.rpc.annotation.RpcService;
import com.example.rpc.constant.RpcConstant;
import com.example.rpc.domain.MetaData;
import com.example.rpc.domain.RpcRequest;
import com.example.rpc.domain.RpcResponse;
import com.example.rpc.handler.RpcRequestHandler;
import com.example.rpc.register.ServiceRegistry;
import com.example.rpc.serializer.RpcDecoder;
import com.example.rpc.serializer.RpcEncoder;
import com.example.rpc.util.HostUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Xia Yubing on 2018/8/2
 */
public class RpcServer implements ApplicationContextAware, InitializingBean {
    private static final int PORT = 8765;
    private Map<String, Object> handlers;

    private ServiceRegistry serviceRegistry;

    public RpcServer(String registerAddress) {
        serviceRegistry = new ServiceRegistry(registerAddress);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        channel.pipeline()
                                .addLast(new RpcDecoder(RpcRequest.class))
                                .addLast(new RpcEncoder(RpcResponse.class))
                                .addLast(new RpcRequestHandler(handlers));
                    }
                }).option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
        ChannelFuture channelFuture = serverBootstrap.bind(PORT).sync();
        handlers.forEach((k, v) -> {
            MetaData metaData = new MetaData();
            metaData.setClassName(k);
            metaData.setRole(RpcConstant.PROVIDER);
            metaData.setAddress(HostUtil.getLocalHost() + ":" + PORT);

            serviceRegistry.register(metaData);
        });
        channelFuture.channel().closeFuture().sync();
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        handlers = new HashMap<>();
        applicationContext.getBeansWithAnnotation(RpcService.class)
                .forEach((k, v) -> handlers.put(v.getClass().getAnnotation(RpcService.class).value().getName(), v));
    }
}
