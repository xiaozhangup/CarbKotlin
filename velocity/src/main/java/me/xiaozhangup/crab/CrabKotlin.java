package me.xiaozhangup.crab;

import com.google.inject.Inject;
import com.velocitypowered.api.proxy.ProxyServer;

public final class CrabKotlin {
    private static ProxyServer server;

    @Inject
    public CrabKotlin(ProxyServer proxyServer) {
        server = proxyServer;
    }

    public static ProxyServer getServer() {
        return server;
    }
}
