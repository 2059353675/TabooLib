package me.skymc.taboolib.database;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import me.skymc.taboolib.Main;
import me.skymc.taboolib.TabooLib;
import me.skymc.taboolib.message.MsgUtils;
import me.skymc.taboolib.Main.StorageType;
import me.skymc.taboolib.playerdata.DataUtils;

public class GlobalDataManager {
	
	public static FileConfiguration data = DataUtils.addPluginData("TabooLibrary-Variable.yml", null);
	
	/**
	 * ��ȡ����
	 * 
	 * @param name ����
	 * @param defaultVariable Ĭ��ֵ
	 * @return
	 */
	public static String getVariable(String name, String defaultVariable) {
		if (Main.getStorageType() == StorageType.SQL) {
			Object obj = Main.getConnection().getValueLast(Main.getTablePrefix() + "_plugindata", "name", name, "variable");
			return obj != null ? obj.toString().equals("null") ? defaultVariable : obj.toString() : defaultVariable;
		}
		else {
			return data.contains(name.replace(":", "-")) ? data.getString(name.replace(":", "-")) : defaultVariable;
		}
	}
	
	/**
	 * ��ȡ����������÷����������ݿⴢ�淽ʽ��
	 * 
	 * @param name ����
	 * @param defaultVariable Ĭ��ֵ
	 * @return
	 */
	public static String getVariableAsynchronous(String name, String defaultVariable) {
		if (Main.getStorageType() == StorageType.SQL) {
			SQLVariable variable = SQLMethod.getSQLVariable(name);
			return variable == null ? defaultVariable : variable.getVariable().equals("null") ? defaultVariable : variable.getVariable();
		}
		else {
			return getVariable(name, defaultVariable);
		}
	}
	
	/**
	 * ���ñ���
	 * 
	 * @param name ����
	 * @param variable ����
	 */
	public static void setVariable(String name, String variable) {
		if (Main.getStorageType() == StorageType.SQL) {
			Main.getConnection().intoValue(Main.getTablePrefix() + "_plugindata", name, variable == null ? "null" : variable, TabooLib.getServerUID());
		}
		else {
			data.set(name.replace(":", "-"), variable);
		}
	}
	
	/**
	 * ���û���������÷����������ݿⴢ�淽ʽ��
	 * 
	 * @param name
	 * @param variable
	 */
	public static void setVariableAsynchronous(String name, String variable) {
		if (Main.getStorageType() == StorageType.SQL) {
			SQLVariable _variable = SQLMethod.contains(name) ? SQLMethod.getSQLVariable(name).setVariable(variable == null ? "null" : variable) : SQLMethod.addSQLVariable(name, variable == null ? "null" : variable);
			// ��������
			SQLMethod.uploadVariable(_variable, true);
		}
		else {
			setVariable(name, variable);
		}
	}
	
	/**
	 * �������Ƿ����
	 * 
	 * @param name ����
	 */
	public static boolean contains(String name) {
		if (Main.getStorageType() == StorageType.SQL) {
			return getVariable(name, null) == null ? false : true;
		}
		else {
			return data.contains(name.replace(":", "-"));
		}
	}
	
	/**
	 * �������Ƿ񱻻��棨�÷����������ݿⴢ�淽ʽ��
	 * 
	 * @param name ����
	 * @return
	 */
	public static boolean containsAsynchronous(String name) {
		if (Main.getStorageType() == StorageType.SQL) {
			return getVariableAsynchronous(name, null) == null ? false : true;
		}
		else {
			return contains(name);
		}
	}
	
	/**
	 * ��������ʧЧ�ı���
	 * �÷����������ݿⴢ��ʱ��Ч
	 */
	public static void clearInvalidVariables() {
		if (Main.getStorageType() == StorageType.SQL) {
			HashMap<String, String> map = getVariables();
			Main.getConnection().truncateTable(Main.getTablePrefix() + "_plugindata");
			for (String name : map.keySet()) {
				Main.getConnection().intoValue(Main.getTablePrefix() + "_plugindata", name, map.get(name), TabooLib.getServerUID());
			}
		}
	}
	
	/**
	 * ��ȡ������Ч����
	 * 
	 * @return
	 */
	public static HashMap<String, String> getVariables() {
		HashMap<String, String> map = new HashMap<>();
		if (Main.getStorageType() == StorageType.SQL) {
			LinkedList<HashMap<String, Object>> list = Main.getConnection().getValues(Main.getTablePrefix() + "_plugindata", "id", -1, false, "name", "variable");
			for (HashMap<String, Object> _map : list) {
				if (!_map.get("variable").toString().equals("null")) {
					map.put(_map.get("name").toString(), _map.get("variable").toString());
				}
			}
		}
		else {
			for (String name : data.getConfigurationSection("").getKeys(false)) {
				map.put(name, data.getString(name));
			}
		}
		return map;
	}
	
	/**
	 * ��ȡ����������÷����������ݿⴢ�淽ʽ��
	 * 
	 * @return
	 */
	public static HashMap<String, String> getVariablesAsynchronous() {
		if (Main.getStorageType() == StorageType.SQL) {
			HashMap<String, String> map = new HashMap<>();
			for (SQLVariable variable : SQLMethod.getSQLVariables()) {
				if (!variable.getVariable().equals("null")) {
					map.put(variable.getName(), variable.getVariable());
				}
			}
			return map;
		}
		else {
			return getVariables();
		}
	}
	
	/**
	 * ���ݿ����
	 * 
	 * @author sky
	 *
	 */
	public static class SQLVariable {
		
		public String name = "";
		public String variable = "";
		public String upgradeUID = "";
		
		public SQLVariable(String name, String variable, String upgradeUID) {
			this.name = name;
			this.variable = variable;
			this.upgradeUID = upgradeUID;
		}
		
		public String getName() {
			return name;
		}
		
		public String getVariable() {
			return variable;
		}
		
		public SQLVariable setVariable(String args) {
			this.variable = args;
			return this;
		}
		
		public String getUpgradeUID() {
			return upgradeUID;
		}
	}
	
	/**
	 * ���ݿⷽ��
	 * 
	 * @author sky
	 *
	 */
	public static class SQLMethod {
		
		private static ConcurrentHashMap<String, SQLVariable> variables = new ConcurrentHashMap<>();
		
		/**
		 * ��ȡ����
		 * 
		 * @param name ����
		 */
		public static SQLVariable getSQLVariable(String name) {
			return variables.get(name);
		}
		
		/**
		 * ��ȡ���б���
		 * 
		 * @return
		 */
		public static Collection<SQLVariable> getSQLVariables() {
			return variables.values();
		}
		
		/**
		 * ���һ������
		 * 
		 * @param name ����
		 * @param value ֵ
		 * @return
		 */
		public static SQLVariable addSQLVariable(String name, String value) {
			SQLVariable variable = new SQLVariable(name, value, TabooLib.getServerUID());
			variables.put(name, variable);
			return variable;
		}
		
		/**
		 * �Ƴ�һ������
		 * 
		 * @param name ����
		 * @return
		 */
		public static SQLVariable removeSQLVariable(String name) {
			if (variables.contains(name)) {
				variables.get(name).setVariable("null");
			}
			return variables.get(name);
		}
		
		/**
		 * �Ƿ��������
		 * 
		 * @param name ����
		 * @return
		 */
		public static boolean contains(String name) {
			return variables.containsKey(name);
		}
		
		/**
		 * �������ݿ��е����б�������
		 * 
		 * @param sync �Ƿ��첽
		 */
		public static void loadVariables(boolean sync) {
			if (Main.getStorageType() == StorageType.LOCAL) {
				return;
			}
			
			BukkitRunnable runnable = new BukkitRunnable() {
				
				@Override
				public void run() {
					LinkedList<HashMap<String, Object>> list = Main.getConnection().getValues(Main.getTablePrefix() + "_plugindata", "id", -1, false, "name", "variable", "upgrade");
					for (HashMap<String, Object> _map : list) {
						if (!_map.get("variable").toString().equals("null")) {
							variables.put(_map.get("name").toString(), new SQLVariable(_map.get("name").toString(), _map.get("variable").toString(), _map.get("upgrade").toString()));
						}
					}
				}
			};
			
			if (sync) {
				runnable.runTaskAsynchronously(Main.getInst());
			}
			else {
				runnable.run();
			}
		}
		
		/**
		 * ��鵱ǰ�����Ƿ���������������
		 * 
		 * @param sync �Ƿ��첽
		 */
		public static void checkVariable(boolean sync) {
			if (Main.getStorageType() == StorageType.LOCAL) {
				return;
			}
			
			BukkitRunnable runnable = new BukkitRunnable() {
				
				@Override
				public void run() {
					/**
					 * �����������л�ȡ���б���
					 * �µı����Ḳ�Ǿɵı���
					 */
					LinkedList<HashMap<String, Object>> list = Main.getConnection().getValues(Main.getTablePrefix() + "_plugindata", "id", -1, false, "name", "variable", "upgrade");
					// ѭ������
					for (HashMap<String, Object> value : list) {
						Object name = value.get("name");
						try {
							// �����������
							if (variables.containsKey(name)) {
								// ������������ɱ�������
								if (!value.get("upgrade").equals(variables.get(name).getUpgradeUID())) {
									// ��������ǿ�
									if (value.get("variable").equals("null")) {
										// ɾ������
										variables.remove(name);
									}
									else {
										// ���±���
										variables.get(name).setVariable(value.get("variable").toString());
									}
								}
							}
							// ����������������ص�����
							else if (!value.get("variable").equals("null")) {
								variables.put(value.get("name").toString(), new SQLVariable(value.get("name").toString(), value.get("variable").toString(), value.get("upgrade").toString()));
							}
						}
						catch (Exception e) {
							// �Ƴ�
							variables.remove(name);
							// ��ʾ
							MsgUtils.warn("���������쳣: &4" + name);
							MsgUtils.warn("ԭ��: &4" + e.getMessage());
						}
					}
				}
			};
			
			if (sync) {
				runnable.runTaskAsynchronously(Main.getInst());
			}
			else {
				runnable.run();
			}
		}
		
		/**
		 * �����ݿ��ϴ���������
		 * 
		 * @param sync �Ƿ��첽
		 */
		public static void uploadVariables(boolean sync) {
			if (Main.getStorageType() == StorageType.LOCAL) {
				return;
			}
			
			for (SQLVariable variable : variables.values()) {
				uploadVariable(variable, sync);
			}
		}
		
		/**
		 * �����ݿ��ϴ���ǰ����
		 * 
		 * @param variable ����
		 * @param sync �Ƿ��첽
		 */
		public static void uploadVariable(SQLVariable variable, boolean sync) {
			if (Main.getStorageType() == StorageType.LOCAL) {
				return;
			}
			
			BukkitRunnable runnable = new BukkitRunnable() {
				
				@Override
				public void run() {
					Main.getConnection().intoValue(Main.getTablePrefix() + "_plugindata", variable.getName(), variable.getVariable() == null ? "null" : variable.getVariable(), TabooLib.getServerUID());
				}
			};
			
			if (sync) {
				runnable.runTaskAsynchronously(Main.getInst());
			}
			else {
				runnable.run();
			}
		}
		
		/**
		 * �������ݿⴢ�淽��
		 * 
		 */
		public static void startSQLMethod() {
			long time = System.currentTimeMillis();
			// ��������
			loadVariables(false);
			// ��ʾ��Ϣ
			MsgUtils.send("�����ݿ��л�ȡ &f" + variables.size() + " &7������, ��ʱ: &f" + (System.currentTimeMillis() - time) + " &7(ms)");
			
			// ������
			new BukkitRunnable() {
				
				@Override
				public void run() {
					checkVariable(true);
				}
			}.runTaskTimerAsynchronously(Main.getInst(), Main.getInst().getConfig().getInt("PluginData.CHECK-DELAY") * 20, Main.getInst().getConfig().getInt("PluginData.CHECK-DELAY") * 20);
		}
		
		/**
		 * �������ݿⴢ�淽��
		 * 
		 */
		public static void cancelSQLMethod() {
			// �ϴ�����
			uploadVariables(false);
		}
	}
}
