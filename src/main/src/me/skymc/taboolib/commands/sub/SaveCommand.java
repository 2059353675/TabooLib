package me.skymc.taboolib.commands.sub;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.skymc.taboolib.Main;
import me.skymc.taboolib.commands.SubCommand;
import me.skymc.taboolib.fileutils.ConfigUtils;
import me.skymc.taboolib.inventory.ItemUtils;
import me.skymc.taboolib.message.ChatCatcher;
import me.skymc.taboolib.message.MsgUtils;
import me.skymc.taboolib.message.ChatCatcher.Catcher;
import me.skymc.taboolib.playerdata.DataUtils;

public class SaveCommand extends SubCommand {

	public SaveCommand(CommandSender sender, String[] args) {
		super(sender, args);
		if (!(sender instanceof Player)) {
			MsgUtils.send(sender, "&4��̨�޷���ô��");
			return;
		}
		
		if (args.length < 2) {
			MsgUtils.send(sender, "&4��������ȷ������");
			return;
		}
		
		if (((Player) sender).getItemInHand().getType().equals(Material.AIR)) {
			MsgUtils.send(sender, "&4�㲻�ܱ������");
			return;
		}
		
		if (ItemUtils.getItemCachesFinal().containsKey(args[1])) {
			MsgUtils.send(sender, "&4����������Ӧ����Ʒ�����ڹ̶���Ʒ����, �޷�����");
			return;
		}
		
		if (ItemUtils.getItemCaches().containsKey(args[1])) {
			// �����������
			if (ChatCatcher.contains((Player) sender)) {
				MsgUtils.send(sender, "&4����һ�����ڽ��е���������, ����ɺ�����ô��");
				return;
			}
			
			ChatCatcher.call((Player) sender, new ChatCatcher.Catcher() {
				
				@Override
				public void cancel() {
					MsgUtils.send(sender, "&7�˳�����");
				}
				
				@Override
				public Catcher before() {
					MsgUtils.send(sender, "��Ʒ &f" + args[1] + "&7 �Ѵ���, �������Ҫ������, ��������������� \"&f��&7\"");
					return this;
				}
				
				@SuppressWarnings("deprecation")
				@Override
				public boolean after(String message) {
					if (message.equals("��")) {
						saveItem(args[1], ((Player) sender).getItemInHand());
						MsgUtils.send(sender, "��Ʒ &f" + args[1] + " &7���滻");
					}
					else {
						MsgUtils.send(sender, "&7�˳�����");
					}
					return false;
				}
			});
		}
		else {
			saveItem(args[1], ((Player) sender).getItemInHand());
			MsgUtils.send(sender, "��Ʒ &f" + args[1] + " &7�ѱ���");
		}
	}

	
	private void saveItem(String name, ItemStack item) {
		FileConfiguration conf = ConfigUtils.load(Main.getInst(), ItemUtils.getItemCacheFile());
		conf.set(name + ".bukkit", item);
		DataUtils.saveConfiguration(conf, ItemUtils.getItemCacheFile());
		ItemUtils.reloadItemCache();
	}
}
