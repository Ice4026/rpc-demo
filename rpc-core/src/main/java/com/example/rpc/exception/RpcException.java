package com.example.rpc.exception;

/**
 * @author Xia Yubing on 2018/8/2
 */
public class RpcException extends RuntimeException {
    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }
}
