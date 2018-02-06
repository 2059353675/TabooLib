package me.skymc.taboolib.inventory;

import java.util.Arrays;
import java.util.LinkedList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.skymc.taboolib.methods.MethodsUtils;

public class InventoryUtil {
	
	public final static LinkedList<Integer> SLOT_OF_CENTENTS = new LinkedList<>(Arrays.asList(
			10, 11, 12, 13, 14, 15, 16,
			19, 20, 21, 22, 23, 24, 25,
			28, 29, 30, 31, 32, 33, 34,
			37, 38, 39, 40, 41, 42, 43));
	
	@Deprecated
	public static boolean isEmpey(Player p) {
        return isEmpty(p, 0);
    }
	
	/**
	 * ��鱳���Ƿ��п�λ
	 * 
	 * @param p ���
	 * @param i ��ʼλ��
	 */
	public static boolean isEmpty(Player p, int i) {
        while (i < 35) {
            if (p.getInventory().getItem(i) == null) {
            	return true;
            }
            i++;
        }
        return false;
    }
	
	/**
	 * �������Ƿ���ָ����Ʒ
	 * 
	 * @param player ���
	 * @param item ��Ʒ
	 * @param amount ����
	 * @param remove �Ƿ�ɾ��
	 */
	public static boolean hasItem(Player player, ItemStack item, int amount, boolean remove) {
		int hasAmount = 0;
		for (ItemStack _item : player.getInventory()) {
			if (item.isSimilar(_item)) {
				hasAmount += _item.getAmount();
			}
		}
		if (hasAmount < amount) {
			return false;
		}
		int requireAmount = amount;
		for (int i = 0; i < player.getInventory().getSize() && remove; i++) {
			ItemStack _item = player.getInventory().getItem(i);
			if (_item != null && _item.isSimilar(item)) {
				/**
				 * ���ѭ��������Ʒ���� С�� ��Ҫ������
				 * �� ɾ����Ʒ��������Ҫ������
				 */
				if (_item.getAmount() < requireAmount) {
					player.getInventory().setItem(i, null);
					requireAmount -= _item.getAmount();
				}
				/**
				 * ���ѭ��������Ʒ���� ���� ��Ҫ������
				 * �� ɾ����Ʒ��ֱ�ӽ���
				 */
				else if (_item.getAmount() == requireAmount) {
					player.getInventory().setItem(i, null);
					return true;
				}
				/**
				 * ���ѭ��������Ʒ���� ���� ��Ҫ������
				 * ��۳� ��Ҫ������
				 */
				else {
					_item.setAmount(_item.getAmount() - requireAmount);
					return true;
				}
			}
		}
		return true;
	}
	
	@Deprecated
	public static boolean hasItem(Inventory targetInventory, ItemStack targetItem, Integer amount) {
		int inventoryAmount = 0;
		for (ItemStack item : targetInventory) {
			if (item != null) {
				if (item.isSimilar(targetItem)) {
					inventoryAmount += item.getAmount();
				}
			}
		}
		if (inventoryAmount >= amount) {
			return true;
		}
		return false;
	}
	
	@Deprecated
	public static boolean takeItem2(Inventory inv, ItemStack takeitem, Integer amount) {
		for (int i = 0; i < inv.getSize(); ++i) {
			if (amount <= 0) {
				return true;
			}
			
			ItemStack item = inv.getItem(i);
			if (item == null) {
				continue;
			}
			if (!item.isSimilar(takeitem)) {
				continue;
			}
			if (item.getAmount() >= amount) {
				if (item.getAmount() - amount == 0) {
					inv.setItem(i, null);
				}
				else {
					item.setAmount(item.getAmount() - amount);
				}
				return true;
			}
			else {
				amount -= item.getAmount();
				inv.setItem(i, null);
			}
		}
		return false;
	}
}
