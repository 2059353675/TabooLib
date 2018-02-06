package me.skymc.taboolib.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;

import com.mysql.jdbc.PreparedStatement;

@Deprecated
public class MysqlConnection {
	
	/**
	 *  Create by Bkm016
	 * 
	 *  2017-7-22 23:25:55 
	 */
	
	private Connection connection = null;
	
	private Statement statement = null;
	
	private Boolean isConnection = false;
	
	public MysqlConnection(String ip, String port, String table, String user, String pass) {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			System("���� MYSQL ϵͳ��ɹ�");
		}
		catch (ClassNotFoundException e) {
			System("���� MYSQL ϵͳ��ʧ��");
		}
		
		// TODO STATE THE URL AND CONNECTION
		String url = "jdbc:mysql://"+ip+":"+port+"/"+table+"?characterEncoding=utf-8";
		
		// TODO CONNECTION
		try {
			connection = DriverManager.getConnection(url, user, pass);
			statement = connection.createStatement();
			
			isConnection = true;
			System("���� MYSQL ���ݿ�ɹ�");
			
			new Thread(() -> {
				while (isConnection) {
					try {
						if (connection.isClosed()) {
							connection = DriverManager.getConnection(url, user, pass);
							System("���ݿ����ӹر�, ������������... [Connection Closed]");
						}
						
						Thread.sleep(30000);
					} catch (SQLException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();
		}
		catch (SQLException e) {
			System("���� MYSQL ���ݿ�ʧ�� ��ϸ��Ϣ: " + e.getLocalizedMessage());
		}
	}
	
	public void closeConnection() 
	{
		try {
			if (statement != null) {
				statement.close();
			}
			if (connection != null) {
				connection.close();
			}
			
			isConnection = false;
			System("���� MYSQL ���ӳɹ�");
		} 
		catch (SQLException e) {
			System("���� MYSQL ����ʧ�� ��ϸ��Ϣ: " + e.getLocalizedMessage());
		}
	}
	
	public Connection getConnection() {
		return this.connection;
	}
	
	public Boolean isConnection() {
		try {
			if (statement.isClosed()) {
				statement = null;
				statement = connection.createStatement();
				System("���ݿ����ӹر�, ������������... [Statement Closed]");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return isConnection;
	}
	
	public Statement getStatement() {
		return this.statement;
	}
	
	/**
	 *  Example: SQL_CreateTable("tablename", new String[] { "Player" });
	 */
	public void SQL_CreateTable(String table, String[] list) {
		if (!isConnection()) {
			return;
		}
		
		StringBuilder stringBuilder = new StringBuilder("");
		
		for (int i = 0 ; i < list.length ; i++) {
			if (i + 1 < list.length) {
				stringBuilder.append("`" + checkString(list[i]) + "` varchar(255), ");
			}
			else {
				stringBuilder.append("`" + checkString(list[i]) + "` varchar(255)");
			}
		}
		String url = "CREATE TABLE IF NOT EXISTS `" + table + "` ( " + stringBuilder + " )";
		
		try {
			getStatement().execute(url);
		}
		catch (SQLException e) {
			System("ִ�� MYSQL ������� ��ϸ��Ϣ: " + e.getLocalizedMessage());
			System("����: " + url);
		}
	}
	
	/**
	 *  Example: SQL_SetValues("tablename", new String[] { "Player" }, new String[] { "BlackSKY" });
	 */
	public void SQL_SetValues(String table, String[] list, String[] values) {
		if (!isConnection()) {
			return;
		}
		
		StringBuilder listbuilder = new StringBuilder("");
		StringBuilder valuebuilder = new StringBuilder("");
		
		for (int i = 0 ; i < list.length ; i++) {
			if (i + 1 < list.length) {
				listbuilder.append("`" + checkString(list[i]) + "`, ");
				valuebuilder.append("'" + checkString(values[i]) + "', ");
			}
			else {
				listbuilder.append("`" + checkString(list[i]) + "`");
				valuebuilder.append("'" + checkString(values[i]) + "'");
			}
		}
		
		String url = "INSERT INTO `" + table + "` ( " + listbuilder + " ) VALUES ( " + valuebuilder + " )";
		try {
			getStatement().execute(url);
		}
		catch (SQLException e) {
			System("ִ�� MYSQL ������� ��ϸ��Ϣ: " + e.getLocalizedMessage());
			System("����: " + url);
			for (int i = 0; i < e.getStackTrace().length && i < 5 ; i++) {
				String name = e.getStackTrace()[i].getClassName();
				
				System("("+i+")λ��: "+name.substring(0, name.lastIndexOf(".")));
				System("     ����: "+e.getStackTrace()[i].getFileName().replaceAll(".java", ""));
				System("     ����: "+e.getStackTrace()[i].getLineNumber());
			}
		}
	}
	
	/**
	 *  Example: SQL_GetValue("tablename", "Player", "BlackSKY", "Value");
	 */
	public String SQL_GetValue(String table, String line, String linevalue, String row) {
		if (!isConnection()) {
			return null;
		}
		
		String url = "SELECT * FROM " + checkString(table) + " WHERE `" + checkString(line) + "` = '" + checkString(linevalue) + "'";
		try {
			ResultSet resultSet = getStatement().executeQuery(url);
			while (resultSet.next()) {
				return resultSet.getString(row);
			}
			resultSet.close();
		} 
		catch (SQLException e) {
			System("ִ�� MYSQL ������� ��ϸ��Ϣ: " + e.getLocalizedMessage());
			System("����: " + url);
		}
		return null;
	}
	
	/**
	 *  Example: SQL_GetValues("tablename", "Player");
	 */
	public List<String> SQL_GetValues(String table, String row) {
		if (!isConnection()) {
			return null;
		}
		
		List<String> list = new ArrayList<>();
		
		String url = "SELECT * FROM " + checkString(table);
		try {
			ResultSet resultSet = getStatement().executeQuery(url);
			while (resultSet.next()) {
				if (resultSet.getString(row) == null) {
					continue;
				}
				list.add(resultSet.getString(row));
			}
			resultSet.close();
		} 
		catch (SQLException e) {
			System("ִ�� MYSQL ������� ��ϸ��Ϣ: " + e.getLocalizedMessage());
			System("����: " + url);
		}
		return list;
	}
	
	/**
	 *  Example: SQL_isExists("tablename", "Player", "BlackSKY");
	 */
	public boolean SQL_isExists(String table, String row, String value) {
		if (!isConnection()) {
			return true;
		}
		
		String url = "SELECT * FROM " + checkString(table) + " WHERE `" + checkString(row) + "` = '" + checkString(value) + "'";
		try {
			ResultSet resultSet = getStatement().executeQuery(url);
			while (resultSet.next()) {
				return true;
			}
			resultSet.close();
		} 
		catch (SQLException e) {
			System("ִ�� MYSQL ������� ��ϸ��Ϣ: " + e.getLocalizedMessage());
			System("����: " + url);
		}
		return false;
	}
	
	/**
	 *  Example: SQL_UpdateValue("tablename", "Player", "BlackSKY", "Value", "10")
	 */
	public void SQL_UpdateValue(String table, String line, String linevalue, String row, String value) {
		if (!isConnection()) {
			return;
		}
		
		String url = "UPDATE `" + checkString(table) + "` SET `" + checkString(row) + "` = '" + checkString(value) + "' WHERE `" + checkString(line) + "` = '" + checkString(linevalue) + "'";
		try {
			getStatement().execute(url);
		} 
		catch (SQLException e) {
			System("ִ�� MYSQL ������� ��ϸ��Ϣ: " + e.getLocalizedMessage());
			System("����: " + url);
		}
	}
	
	/**
	 *  Example: SQL_DeleteValue("tablename", "BlackSKY");
	 */
	public void SQL_DeleteValue(String table, String line, String linevalue) {
		if (!isConnection()) {
			return;
		}
		
		String url = "DELETE FROM `" + checkString(table) + "` WHERE `" + checkString(line) + "` = '" + checkString(linevalue) + "'";
		try {
			getStatement().execute(url);
		} 
		catch (SQLException e) {
			System("ִ�� MYSQL ������� ��ϸ��Ϣ: " + e.getLocalizedMessage());
			System("����: " + url);
		}
	}
	
	/**
	 *  @deprecated ��������
	 * 
	 *  @see Example: SQL_ClearTable("tablename");
	 */
	public void SQL_ClearTable(String table) {
		if (!isConnection()) {
			return;
		}
		
		String url = "TRUNCATE TABLE `" + checkString(table) + "`";
		try {
			getStatement().execute(url);
		} 
		catch (SQLException e) {
			System("ִ�� MYSQL ������� ��ϸ��Ϣ: " + e.getLocalizedMessage());
			System("����: " + url);
		}
	}
	
	public void SQL_execute(String url) {
		if (!isConnection()) {
			return;
		}
		
		try {
			getStatement().execute(url);
		} catch (SQLException e) {
			System("ִ�� MYSQL ������� ��ϸ��Ϣ: " + e.getLocalizedMessage());
			System("����: " + url);
		}
	}
	
	public ResultSet SQL_executeQuery(String url) {
		if (!isConnection()) {
			return null;
		}
		
		try {
			return getStatement().executeQuery(url);
		} catch (SQLException e) {
			System("ִ�� MYSQL ������� ��ϸ��Ϣ: " + e.getLocalizedMessage());
			System("����: " + url);
			return null;
		}
	}
	
	public void SQL_clearTable(String table) {
		SQL_execute("DELETE FROM " + checkString(table) + ";");
	}
	
	public void SQL_deleteTable(String table) {
		SQL_execute("DROP TABLE " + checkString(table) + ";");
	}
	
	private void System(String string) {
		System.out.println("[TabooLib - MYSQL] " + string);
	}
	
	private String checkString(String string) {
		return string.replace("`", "").replace("'", "").replace("\"", "");
	}
	
}
