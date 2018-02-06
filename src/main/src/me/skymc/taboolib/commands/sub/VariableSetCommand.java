package me.skymc.taboolib.commands.sub;

import org.bukkit.command.CommandSender;

import me.skymc.taboolib.commands.SubCommand;
import me.skymc.taboolib.database.GlobalDataManager;
import me.skymc.taboolib.message.MsgUtils;

public class VariableSetCommand extends SubCommand {

	public VariableSetCommand(CommandSender sender, String[] args) {
		super(sender, args);
		
		if (args.length < 4) {
			MsgUtils.send(sender, "&4��������ȷ��ָ�� ");
		}
		else if (!(args[1].equals("-a") || args[1].equals("-s"))) {
			MsgUtils.send(sender, "&4��������ȷ��д�뷽ʽ");
		}
		
		Long time = System.currentTimeMillis();
		String value = getArgs(3);
		
		if (args[1].equals("-s")) {
			GlobalDataManager.setVariable(args[2], value);
		}
		else if (args[1].equals("-a")) {
			GlobalDataManager.setVariableAsynchronous(args[2], value);
		}
		
		MsgUtils.send(sender, "д�����, ��ʱ: &f" + (System.currentTimeMillis() - time) + " &7(ms)");
		setReturn(true);
	}
}
