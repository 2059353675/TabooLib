package me.skymc.taboolib.player;

import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("deprecation")
public class PlayerUtils {
	
	/**
	 * ��ȡĿ�귽��
	 * 
	 * @param player ���
	 * @param max �����Ұ
	 * @return
	 */
	public static Block getTargetBlock(Player player, int max) {
		HashSet<Byte> Byte = new HashSet<>();
		Byte.add((byte) 0);
		return player.getTargetBlock(Byte, max);
	}
	
	/**
	 * ��д����
	 * 
	 * @param player ���
	 * @param scoreboard �Ƿ�����Ʒְ�
	 */
	public static void resetData(Player player, boolean scoreboard) {
		if (player.isDead()) {
			player.spigot().respawn();
		}
		player.closeInventory();
	    player.setGameMode(GameMode.SURVIVAL);
		player.getInventory().setArmorContents(new ItemStack[4]);
	    player.getInventory().setContents(new ItemStack[0]);
	    player.setAllowFlight(false);
	    player.setFlying(false);
	    player.setExp(0.0F);
	    player.setLevel(0);
	    player.setSneaking(false);
	    player.setSprinting(false);
	    player.setFoodLevel(20);
	    player.setSaturation(10.0F);
	    player.setExhaustion(0.0F);
	    player.setMaxHealth(20.0D);
	    player.setHealth(20.0D);
	    player.setFireTicks(0);
		player.setItemOnCursor(null);
		player.getActivePotionEffects().clear();
		player.getEnderChest().clear();
		player.updateInventory();
		if (scoreboard) {
			player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
		}
	}
}
