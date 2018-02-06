package me.skymc.taboolib.commands.sub;

import org.bukkit.command.CommandSender;

import me.skymc.taboolib.commands.SubCommand;
import me.skymc.taboolib.database.GlobalDataManager;
import me.skymc.taboolib.message.MsgUtils;

public class VariableGetCommand extends SubCommand {

	public VariableGetCommand(CommandSender sender, String[] args) {
		super(sender, args);
		
		if (args.length < 3) {
			MsgUtils.send(sender, "&4��������ȷ��ָ�� ");
		}
		else if (!(args[1].equals("-a") || args[1].equals("-s"))) {
			MsgUtils.send(sender, "&4��������ȷ�Ķ�ȡ��ʽ");
		}
		
		Long time = System.currentTimeMillis();
		String value = null;
		
		if (args[1].equals("-s")) {
			value = GlobalDataManager.getVariable(args[2], null);
		}
		else if (args[1].equals("-a")) {
			value = GlobalDataManager.getVariableAsynchronous(args[2], null);
		}
		
		if (value == null) {
			MsgUtils.send(sender, "��ȡ���, ��ʱ: &f" + (System.currentTimeMillis() - time) + " &7(ms)");
			MsgUtils.send(sender, "���� &f" + args[2] + " &7������");
		}
		else {
			MsgUtils.send(sender, "��ȡ���, ��ʱ: &f" + (System.currentTimeMillis() - time) + " &7(ms)");
			MsgUtils.send(sender, "���� &f" + args[2] + " &7��ֵΪ &f" + value);
		}
	}
}
