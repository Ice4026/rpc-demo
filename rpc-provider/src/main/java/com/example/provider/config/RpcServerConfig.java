package com.example.provider.config;

import com.example.rpc.server.RpcServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Xia Yubing on 2018/8/3
 */
@Configuration
public class RpcServerConfig {
    @Bean
    public RpcServer rpcServer() {
        return new RpcServer("172.16.1.35:2181");
    }
}
