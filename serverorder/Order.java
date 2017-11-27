package com.example.orangeparadise.serverorder;

import java.util.ArrayList;

//"[{\"name\":\"Burger\", \"price\":\"5.50\", \"description\":\"Gourmet special burger\", \"url\":\"http://192.168.1.13:3333/image_burger.png\"}

class Item{
	String name;
	Double price;
	String description;
	String url;
	
	public Item(String name, Double price, String description, String url) {
		this.name = name;
		this.price = price;
		this.description = description;
		this.url = url;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "{\"name\":\"" + this.name + "\","
				+ "\"price\":\"" + this.price + "\","
				+ "\"description\":\"" + this.description +"\","
				+ "\"url\":\"" + this.url +"\"}";
	}
	
}

public class Order {
	//Item item;
	//Integer num;
	ArrayList<Item> items = new ArrayList<>();
	ArrayList<Integer> nums = new ArrayList<>();
	final String ipAddr;
	
	QueryState queryState;
	CookingState cookingState;
	
	public enum QueryState{
		Preparing,
		NotAvailable,
		Fully,
		Patial
	}
	
	public enum CookingState{
		Querying,
		Cooking,
		Packing
	}
	
	public Order(ArrayList<Item> items, ArrayList<Integer> nums, String ipAddr) {
		this.items = items;
		this.nums = nums;
		this.ipAddr = ipAddr;
		this.queryState = QueryState.Preparing;
		this.cookingState = CookingState.Querying;
	}
}
