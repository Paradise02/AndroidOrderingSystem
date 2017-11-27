package com.example.orangeparadise.serverorder;

import java.util.ArrayList;

public class Inventory {
	
	private ArrayList<Item> items = new ArrayList<>();
	private ArrayList<Integer> instockNums = new ArrayList<>();
	private final String HOST_ADDR;
	
	private static Inventory inventory = new Inventory("http://10.110.147.69:3333/");
	
	private Inventory(String HOST_ADDR) {
		this.HOST_ADDR = HOST_ADDR;
		items.add(new Item("Burger", 5.50, "Gourmet special burger", 
				HOST_ADDR +"image_burger.png"));
		instockNums.add(10);
		items.add(new Item("Chicken", 6.00, "Fried tender, wings or leg",
				HOST_ADDR + "image_fried_chicken.png"));
		instockNums.add(5);
		items.add(new Item("French Fries", 2.00, "Small pieces", 
				HOST_ADDR + "image_french_fries.png"));
		instockNums.add(12);
		items.add(new Item("Onion Rings", 2.50, "Well fried onion rings", 
				HOST_ADDR + "image_onion_rings.png"));
		instockNums.add(8);
		//items.add(new Item("New", 0.65, "Test", HOST_ADDR + "image_onion_rings.png"));
	}
	
	public static Inventory connectInventory() {
		return Inventory.inventory;
	}
	
	public void addItem(Item item, int amt) {
		for (int i = 0; i < items.size(); i++) {
			if (items.get(i).name.equals(item.name)) {
				instockNums.set(i, instockNums.get(i)+amt);
				return;
			}
		}
		items.add(item);
		instockNums.add(amt);
	}
	
	public synchronized boolean isplenty(Item item, int amt) {
		for (int i = 0; i < items.size(); i++) {
			if (items.get(i).name.equals(item.name)) {
				return instockNums.get(i) >= amt;
			}
		}
		return false;
	}
	
	public synchronized void consumeItem(Item item, int amt) {
		for (int i = 0; i < items.size(); i++) {
			if (items.get(i).name.equals(item.name)) {
				if (instockNums.get(i) -  amt > 0) {
					instockNums.set(i, instockNums.get(i) - amt);
				} else {
					items.remove(i);
					instockNums.remove(i);
				}
				return;
			}
		}
	}
	
	public synchronized boolean hasItem(Item item) {
		for (int i = 0; i < items.size(); ++i) {
			if (items.get(i).name.equals(item.name)) {
				return instockNums.get(i) > 0;
			}
		}
		return false;
	}
	
	public synchronized int itemAmt(Item item) {
		for (int i = 0; i < items.size(); ++i) {
			if (items.get(i).name.equals(item.name)) {
				return instockNums.get(i);
			}
		}
		return 0;
	}
	
	public synchronized ArrayList<Item> getItems(){
		ArrayList<Item> itemsList = new ArrayList<>();
		itemsList.addAll(this.items);
		return itemsList;
	}

	public static void main(String[] args) {
		Inventory inventory = Inventory.connectInventory();
		System.out.println(inventory.isplenty(new Item("", (double) 0, "", ""), 1));
		System.out.println(inventory.isplenty(new Item("Burger", 0.0, "", ""), 100));

		System.out.println(inventory.isplenty(new Item("Burger", 0.0, "", ""), 1));

		System.out.println(inventory.hasItem(new Item("Burger", 0.0, "", "")));
		System.out.println(inventory.hasItem(new Item("ppp", 0.0, "", "")));
		inventory.consumeItem(new Item("Burger", 0.0, "", ""), 10);
		System.out.println(inventory.hasItem(new Item("Burger", 0.0, "", "")));

	}

}
