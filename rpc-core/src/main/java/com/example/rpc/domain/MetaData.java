package com.example.rpc.domain;

/**
 * @author Xia Yubing on 2018/8/3
 */
public class MetaData {
    // 服务接口名
    private String className;
    // 角色名, consumer或者provider
    private String role;
    private String address;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
