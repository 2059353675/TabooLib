package me.skymc.taboolib.commands.sub;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.skymc.taboolib.commands.SubCommand;
import me.skymc.taboolib.inventory.ItemUtils;
import me.skymc.taboolib.message.MsgUtils;
import me.skymc.taboolib.other.NumberUtils;

public class ItemCommand extends SubCommand {
	
	/**
	 * /TabooLib item ��Ʒ ��� ����
	 * 
	 * @param sender
	 * @param args
	 */
	public ItemCommand(CommandSender sender, String[] args) {
		super(sender, args);
		
		if (args.length < 2) {
			MsgUtils.send(sender, "��������ȷ����Ʒ����");
			setReturn(false);
		}
		else {
			if (ItemUtils.getCacheItem(args[1]) == null) {
				MsgUtils.send(sender, "��Ʒ &f" + args[1] + "&7 ������");
				setReturn(false);
				return;
			}
			
			Player player;
			Integer amount = 1;
			ItemStack item = ItemUtils.getCacheItem(args[1]).clone();
			
			if (args.length > 2) {
				player = Bukkit.getPlayerExact(args[2]);
				if (player == null) {
					MsgUtils.send(sender, "��� &f" + args[2] + "&7 ������");
					setReturn(false);
					return;
				}
			}
			else if (sender instanceof Player) {
				player = (Player) sender;
			}
			else {
				MsgUtils.send(sender, "��̨��������ô��");
				setReturn(false);
				return;
			}
			
			if (args.length > 3) {
				amount = NumberUtils.getInteger(args[3]);
				if (amount < 1) {
					MsgUtils.send(sender, "�����������0");
					setReturn(false);
					return;
				}
			}
			item.setAmount(amount);
			
			HashMap<Integer, ItemStack> map = player.getInventory().addItem(item);
			if (map.size() > 0) {
				player.getWorld().dropItem(player.getLocation(), item);
			}
			
			MsgUtils.send(sender, "��Ʒ�ѷ�������� &f" + player.getName() + " &7�ı�����");
			setReturn(true);
		}
	}
}
