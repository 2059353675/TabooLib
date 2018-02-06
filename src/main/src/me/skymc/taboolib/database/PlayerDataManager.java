package me.skymc.taboolib.database;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import me.skymc.taboolib.Main;
import me.skymc.taboolib.Main.StorageType;
import me.skymc.taboolib.events.PlayerLoadedEvent;
import me.skymc.taboolib.exception.PlayerOfflineException;
import me.skymc.taboolib.fileutils.ConfigUtils;
import me.skymc.taboolib.fileutils.FileUtils;
import me.skymc.taboolib.message.MsgUtils;

public class PlayerDataManager implements Listener {
	
	private static final ConcurrentHashMap<String, FileConfiguration> PLAYER_DATA = new ConcurrentHashMap<>();
	
	public static enum UsernameType {
		UUID, USERNAME;
	}
	
	/**
	 * ��ȡ�û����淽ʽ
	 * 
	 * @return
	 */
	public static UsernameType getUsernameType() {
		return Main.getInst().getConfig().getBoolean("ENABLE-UUID") ? UsernameType.UUID : UsernameType.USERNAME;
	}
	
	/**
	 * ��ȡ�������
	 * 
	 * @param player ���
	 * @return
	 * @throws PlayerOfflineException 
	 */
	public static FileConfiguration getPlayerData(Player player) {
		if (getUsernameType() == UsernameType.UUID) {
			return getPlayerData(player.getUniqueId().toString(), false);
		}
		else {
			return getPlayerData(player.getName(), false);
		}
	}
	
	/**
	 * ��ȡ�������
	 * 
	 * @param player
	 * @return
	 */
	public static FileConfiguration getPlayerData(OfflinePlayer player) {
		if (!player.isOnline()) {
			return null;
		}
		if (getUsernameType() == UsernameType.UUID) {
			return getPlayerData(player.getUniqueId().toString(), false);
		}
		else {
			return getPlayerData(player.getName(), false);
		}
	}
	
	/**
	 * ��ȡ�������
	 * 
	 * @param username ���
	 * @return
	 * @throws PlayerOfflineException 
	 */
	public static FileConfiguration getPlayerData(String username, boolean offline) {
		if (PLAYER_DATA.containsKey(username)) {
			return PLAYER_DATA.get(username);
		}
		else if (offline) {
			if (Main.getStorageType() == StorageType.SQL) {
				throw new PlayerOfflineException("�������ڴ���ģʽΪ���ݿ������»�ȡ�����������");
			}
			return loadPlayerData(username);
		}
		return null;
	}
	
	/**
	 * �����������
	 * 
	 * @param username ���
	 * @return
	 */
	public static FileConfiguration loadPlayerData(String username) {
		// ���ش���
		if (Main.getStorageType() == StorageType.LOCAL) {
			// ��ȡ�ļ�
			File file = FileUtils.file(Main.getPlayerDataFolder(), username + ".yml");
			// ��������
			PLAYER_DATA.put(username, YamlConfiguration.loadConfiguration(file));
		}
		else {
			// �����Ƿ����
			if (Main.getConnection().isExists(Main.getTablePrefix() + "_playerdata", "username", username)) {
				// ��ȡ����
				String code = Main.getConnection().getValue(Main.getTablePrefix() + "_playerdata", "username", username, "configuration").toString();
				try {
					// ��������
					PLAYER_DATA.put(username, ConfigUtils.decodeYAML(code));
				}
				catch (Exception e) {
					// ����������
					PLAYER_DATA.put(username, new YamlConfiguration());
					// ������Ϣ
					MsgUtils.warn("��� &4" + username + " &c��������������쳣: &4" + e.getMessage());
				}
			}
			else {
				// ����������
				PLAYER_DATA.put(username, new YamlConfiguration());
			}
		}
		return PLAYER_DATA.get(username);
	}
	
	/**
	 * �����������
	 * 
	 * @param username ���
	 * @param remove �Ƿ��Ƴ�����
	 */
	public static void savePlayerData(String username, boolean remove) {
		// û������
		if (!PLAYER_DATA.containsKey(username)) {
			return;
		}
		// ���ش���
		if (Main.getStorageType() == StorageType.LOCAL) {
			// ��ȡ�ļ�
			File file = FileUtils.file(Main.getPlayerDataFolder(), username + ".yml");
			// ��������
			try {
				PLAYER_DATA.get(username).save(file);
			}
			catch (Exception e) {
				// TODO: handle exception
			}
		}
		// ��������ݿⴢ����������
		else if (PLAYER_DATA.get(username).getConfigurationSection("").getKeys(false).size() > 0) {
			// �����Ƿ����
			if (Main.getConnection().isExists(Main.getTablePrefix() + "_playerdata", "username", username)) {
				// д������
				Main.getConnection().setValue(Main.getTablePrefix() + "_playerdata", "username", username, "configuration", ConfigUtils.encodeYAML(PLAYER_DATA.get(username)));
			}
			else {
				// ��������
				Main.getConnection().intoValue(Main.getTablePrefix() + "_playerdata", username, ConfigUtils.encodeYAML(PLAYER_DATA.get(username)));
			}
		}
		// ��ȡ������Զ�Ӧ�����
		Player player;
		if (getUsernameType() == UsernameType.UUID) {
			player = Bukkit.getPlayer(UUID.fromString(username));
		}
		else {
			player = Bukkit.getPlayerExact(username);
		}
		// ����Ƴ����� �� ��Ҳ�����
		if (remove || player == null) {
			PLAYER_DATA.remove(username);
		}
	}
	
	/**
	 * ����������ҵĻ���
	 * 
	 * @param sync �Ƿ��첽����
	 * @param remove �Ƿ��Ƴ�����
	 */
	public static void saveAllCaches(boolean sync, boolean remove) {
		BukkitRunnable runnable = new BukkitRunnable() {
			
			@Override
			public void run() {
				long time = System.currentTimeMillis();
				// ����
				for (String name : PLAYER_DATA.keySet()) {
					savePlayerData(name, false);
				}
				// ��ʾ
				if (!Main.getInst().getConfig().getBoolean("HIDE-NOTIFY")) {
					MsgUtils.send("���� &f" + PLAYER_DATA.size() + " &7���������, ��ʱ: &f" + (System.currentTimeMillis() - time) + " &7(ms)");
				}
			}
		};
		// ����첽
		if (sync) {
			runnable.runTaskAsynchronously(Main.getInst());
		}
		// ���ͬ��
		else {
			runnable.run();
		}
	}
	
	/**
	 * ����������ҵ�����
	 * 
	 * @param sync �Ƿ��첽����
	 * @param remove �Ƿ��Ƴ�����
	 */
	public static void saveAllPlayers(boolean sync, boolean remove) {
		// ��������
		BukkitRunnable runnable = new BukkitRunnable() {
			
			@Override
			public void run() {
				for (Player player : Bukkit.getOnlinePlayers()) {
					savePlayerData(Main.getInst().getConfig().getBoolean("ENABLE-UUID") ? player.getUniqueId().toString() : player.getName(), remove);
				}
			}
		};
		// ����첽
		if (sync) {
			runnable.runTaskAsynchronously(Main.getInst());
		}
		// ���ͬ��
		else {
			runnable.run();
		}
	}
	
	@EventHandler
	public void join(PlayerJoinEvent e) {
		new BukkitRunnable() {
			
			@Override
			public void run() {
				// ��������
				loadPlayerData(Main.getInst().getConfig().getBoolean("ENABLE-UUID") ? e.getPlayer().getUniqueId().toString() : e.getPlayer().getName());
				// �������
				Bukkit.getPluginManager().callEvent(new PlayerLoadedEvent(e.getPlayer()));
			}
		}.runTaskAsynchronously(Main.getInst());
	}
	
	@EventHandler
	public void quit(PlayerQuitEvent e) {
		if (!Main.isDisable()) {
			new BukkitRunnable() {
				
				@Override
				public void run() {
					// ��������
					savePlayerData(Main.getInst().getConfig().getBoolean("ENABLE-UUID") ? e.getPlayer().getUniqueId().toString() : e.getPlayer().getName(), true);
				}
			}.runTaskAsynchronously(Main.getInst());
		}
	}
}
