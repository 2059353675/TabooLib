package me.skymc.taboolib.socket.packet.impl;

import me.skymc.taboolib.socket.TabooLibClient;
import me.skymc.taboolib.socket.packet.Packet;
import me.skymc.taboolib.socket.packet.PacketType;
import org.bukkit.Bukkit;

/**
 * @Author sky
 * @Since 2018-08-22 23:01
 */
@PacketType(name = "heartbeat")
public class PacketHeartbeat extends Packet {

    public PacketHeartbeat(int port) {
        super(port);
    }

    @Override
    public void readOnServer() {
    }

    @Override
    public void readOnClient() {
        // 更新响应时间
        TabooLibClient.setLatestResponse(System.currentTimeMillis());
        // 回应服务端
        TabooLibClient.sendPacket(new PacketAlive(TabooLibClient.getSocket().getLocalPort()));
    }
}
