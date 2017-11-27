package com.example.orangeparadise.serverorder;

import java.util.List;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class BadOrderException extends Exception{}

public class OrderJsonReader {
	
	private static List<Item> items = new ArrayList<>();
	
	static {
		items.add(new Item("default", 0.0, "", ""));
	}
	
	public OrderJsonReader(List<Item> items) {
		//System.out.println("Order Reader Constructor");
		this.items.clear();
		this.items.addAll(items);
		for(int i = 0; i < this.items.size(); ++i) {
			System.out.println(items.get(i));
		}
	}
	
	public Order parseOrder(String jsonOrder, String ip) throws BadOrderException {
		Order order = null;
		
		try {
			JSONArray jsonArray = new JSONArray(jsonOrder);
			ArrayList<Item> clientItems = new ArrayList<>();
			ArrayList<Integer> clientItemNums = new ArrayList<>();
			for (int i = 0; i < jsonArray.length(); i ++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				String itemName = jsonObject.getString("name");
				Integer itemAmt = jsonObject.getInt("amount");
				
				for(int j = 0; j < items.size(); ++j) {
					if (items.get(j).name.equals(itemName)) {
						//System.out.println("In");
						clientItems.add(this.items.get(j));
						clientItemNums.add(itemAmt);
					}
				}
				
				if (clientItems.size() == 0) {
					throw new BadOrderException();
				}
				
				order = new Order(clientItems, clientItemNums, ip);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			BadOrderException exception = new BadOrderException();
			exception.initCause(e);
			throw exception;
		}
		
		return order;
	}
}
