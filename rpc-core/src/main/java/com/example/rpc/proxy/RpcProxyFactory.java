package com.example.rpc.proxy;

import com.example.rpc.client.RpcClient;
import com.example.rpc.domain.RpcRequest;

import java.lang.reflect.Proxy;
import java.util.UUID;

/**
 * @author Xia Yubing on 2018/8/6
 */
public class RpcProxyFactory {
    private RpcClient rpcClient;

    public RpcProxyFactory(String registerAddress) {
        rpcClient = new RpcClient(registerAddress);
    }

    public <T> T create(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class[]{clazz},
                (proxy, method, args) -> {
                    RpcRequest request = new RpcRequest();
                    request.setRequestId(UUID.randomUUID().toString());
                    request.setClassName(method.getDeclaringClass().getName());
                    request.setMethodName(method.getName());
                    request.setParameterTypes(method.getParameterTypes());
                    request.setParameters(args);

                    return rpcClient.send(request).getResult();
                }
        );
    }
}
