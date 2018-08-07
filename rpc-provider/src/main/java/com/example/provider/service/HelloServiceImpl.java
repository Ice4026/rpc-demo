package com.example.provider.service;

import com.example.rpc.annotation.RpcService;

/**
 * @author Xia Yubing on 2018/8/3
 */
@RpcService(HelloService.class)
public class HelloServiceImpl implements HelloService {
    @Override
    public String hello(String name) {
        System.out.println("[X] received: " + name);
        return "Hello " + name;
    }
}
