package com.example.orangeparadise.serverorder;

import java.util.concurrent.TimeUnit;

import com.example.orangeparadise.serverorder.Order.CookingState;
import com.example.orangeparadise.serverorder.Order.QueryState;

public class Chef{
	OrderServer server;
	Inventory inventory = Inventory.connectInventory();
	boolean busy = false;
	
	public Chef(OrderServer server) {
		// TODO Auto-generated constructor stub
		this.server = server;
	}
	
	public enum ChefTask{
		check,
		cook
	}
	
	public void doTask(ChefTask task) {
		switch (task) {
		case check:
			new checkTask().start();
			break;
		case cook:
			new cookTask().start();
			break;
		}
	}
	
	public class checkTask extends Thread {
		
		Order order;
		
		@Override
		public void run() {
			Chef.this.busy = true;
			this.order = Chef.this.server.ordersToOrder.remove();
			
			System.out.print("order details: ");
			for (int i = 0; i < order.nums.size(); ++i) {
				System.out.print(order.nums.get(i) + ",");
			}
			System.out.println();
			
			try {
				TimeUnit.MILLISECONDS.sleep((long) (Math.random() * 1000));
				
				boolean isfully = true;
				boolean isPatial = false;
				
				System.out.println("Check task performed");
				
				for (int i = 0; i < this.order.items.size(); ++i) {
					if (inventory == null) {
						System.out.println("fuck fuck fuck");
					}
					isfully = isfully && inventory.isplenty(
							this.order.items.get(i)
							, this.order.nums.get(i)
							);
				}
				if (isfully) {
					order.queryState = QueryState.Fully;
				} else {
					for (int i = 0; i < this.order.items.size(); ++i) {
						isPatial = isPatial || inventory.hasItem(this.order.items.get(i));
					}
					if (isPatial) {
						order.queryState = QueryState.Patial;
					} else {
						order.queryState = QueryState.NotAvailable;
					}
				}
				if (isPatial) {
					for (int i = 0; i < order.items.size(); ++i) {
						if (!inventory.isplenty(order.items.get(i), order.nums.get(i))) {
							order.nums.set(i, inventory.itemAmt(order.items.get(i)));
							System.out.println(order.nums.get(i));
						}
					}
				}
				Chef.this.server.ordersOrdered.add(order);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Chef.this.busy = false;
		}
	}
	
	public class cookTask extends Thread {
		@Override
		public void run() {
			System.out.println("COOKING NOW...");
			Order order = Chef.this.server.ordersToDo.remove();
			
			try {
				for (int i = 0; i < order.items.size(); ++i) {
					inventory.consumeItem(order.items.get(i), order.nums.get(i));
				}
				TimeUnit.MILLISECONDS.sleep((long) (Math.random()*5000));
				order.cookingState = CookingState.Packing;
				Chef.this.server.ordersDone.add(order);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
