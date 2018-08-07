package com.example.rpc.handler;

import com.example.rpc.domain.RpcRequest;
import com.example.rpc.domain.RpcResponse;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.log4j.Logger;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * 处理RpcRequest请求
 *
 * @author Xia Yubing on 2018/8/2
 */
public class RpcRequestHandler extends SimpleChannelInboundHandler<RpcRequest> {
    private Map<String, Object> handlers;

    public RpcRequestHandler(Map<String, Object> handlers) {
        this.handlers = handlers;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest msg) throws Exception {
        RpcResponse response = new RpcResponse();
        response.setRequestId(msg.getRequestId());
        try {
            Object result = handle(msg);
            response.setResult(result);
        } catch (Exception t) {
            response.setError(t);
        }
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private Object handle(RpcRequest msg) throws InvocationTargetException {
        Object service = handlers.get(msg.getClassName());

        FastClass fastClass = FastClass.create(service.getClass());
        FastMethod fastMethod = fastClass.getMethod(msg.getMethodName(), msg.getParameterTypes());
        return fastMethod.invoke(service, msg.getParameters());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        ctx.close();
    }
}
