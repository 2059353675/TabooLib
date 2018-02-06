package me.skymc.taboolib.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;

import me.skymc.taboolib.message.MsgUtils;
import me.skymc.taboolib.mysql.MysqlUtils;
import me.skymc.taboolib.mysql.protect.MySQLConnection;

public class ListenerPluginDisable implements Listener {
	
	@EventHandler
	public void disable(PluginDisableEvent e) {
		int i = 0;
		for (MySQLConnection conn : MysqlUtils.CONNECTIONS) {
			if (conn.getPlugin().equals(e.getPlugin())) {
				MysqlUtils.CONNECTIONS.remove(conn);
				conn.closeConnection();
				i++;
			}
		}
		if (i > 0) {
			MsgUtils.send("��ֹͣ��� &f" + e.getPlugin().getName() + "&7 �� &f" + i + "&7 �����ݿ�����");
		}
	}

}
