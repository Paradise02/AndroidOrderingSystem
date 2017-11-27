package com.example.orangeparadise.serverorder;

import java.util.ArrayList;

public class ClientOrder {
	String clientAddr;
	
	ArrayList<Order> orders = new ArrayList<>();
	
	public ClientOrder(Order ...orders) {
		for(int i = 0; i < orders.length; ++i) {
			this.orders.add(orders[i]);
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
