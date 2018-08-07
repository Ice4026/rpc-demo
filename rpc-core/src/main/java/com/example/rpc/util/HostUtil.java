package com.example.rpc.util;

import com.example.rpc.exception.RpcException;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author Xia Yubing on 2018/8/3
 */
public final class HostUtil {
    private HostUtil() {
    }

    public static String getLocalHost() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new RpcException("Cannot get local host.", e);
        }
    }
}
