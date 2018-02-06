package me.skymc.taboolib.fileutils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashMap;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import com.google.common.base.Charsets;

import me.skymc.taboolib.Main;
import me.skymc.taboolib.message.MsgUtils;

public class ConfigUtils {
	
	public static FileConfiguration decodeYAML(String args) {
		return YamlConfiguration.loadConfiguration(new StringReader(Base64Coder.decodeString(args)));
	}
	
	public static String encodeYAML(FileConfiguration file) {
		return Base64Coder.encodeLines(file.saveToString().getBytes()).replaceAll("\\s+", "");
	}
	
	/**
	 * �� UTF-8 �ĸ�ʽ���������ļ�
	 * 
	 * @param main
	 * @param filename
	 * @return
	 */
	public static YamlConfiguration load(Plugin plugin, File file) {
		YamlConfiguration yaml = new YamlConfiguration();
		try {
			yaml = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(file), Charsets.UTF_8));
		} catch (FileNotFoundException e) {
			MsgUtils.warn("�����ļ�����ʧ��!");
			MsgUtils.warn("���: &4" + plugin.getName());
			MsgUtils.warn("�ļ�: &4" + file.getName());
		}
		return yaml;
	}
	
	@Deprecated
	public static YamlConfiguration load(Plugin plugin, String file) {
		return load(plugin, FileUtils.file(file));
	}
}
