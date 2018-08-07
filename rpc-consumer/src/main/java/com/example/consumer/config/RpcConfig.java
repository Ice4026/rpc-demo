package com.example.consumer.config;

import com.example.provider.service.HelloService;
import com.example.rpc.proxy.RpcProxyFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Xia Yubing on 2018/8/6
 */
@Configuration
public class RpcConfig {

    @Bean
    public RpcProxyFactory rpcProxyFactory() {
        return new RpcProxyFactory("172.16.1.35:2181");
    }

    @Bean
    public HelloService helloService() {
        return rpcProxyFactory().create(HelloService.class);
    }
}
