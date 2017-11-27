package com.example.orangeparadise.serverorder;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import com.example.orangeparadise.serverorder.Chef.ChefTask;
import com.example.orangeparadise.serverorder.Order.CookingState;

public class OrderServer{
	private static final int PORT = 4040;
	private static final String ORDER = "order";
	private static final String REFRESH = "refresh";
	private static final String CONFIRM = "confirm";
	private static final String CANCEL = "cancel";
	private static final String QUERY = "query";
	private static final String TRACK = "track";
	
	private static final String HOST_ADDR = "http://10.110.147.69:3333/";
	
	public OrderQueue ordersToOrder = new OrderQueue();
	public OrderQueue ordersOrdered = new OrderQueue();
	public OrderQueue ordersToDo = new OrderQueue();
	public OrderQueue ordersDone = new OrderQueue();
	
	private OrderJsonReader reader;
	
	private ArrayList<Chef> chefs = new ArrayList<>();
	
	private ArrayList<Item> getItems() {
//		ArrayList<Item> items = new ArrayList<>();
//		
//		items.add(new Item("Burger", 5.50, "Gourmet special burger", 
//				HOST_ADDR +"image_burger.png"));
//		items.add(new Item("Chicken", 6.00, "Fried tender, wings or leg",
//				HOST_ADDR + "image_fried_chicken.png"));
//		items.add(new Item("French Fries", 2.00, "Small pieces", 
//				HOST_ADDR + "image_french_fries.png"));
//		items.add(new Item("Onion Rings", 2.50, "Well fried onion rings", 
//				HOST_ADDR + "image_onion_rings.png"));
//		items.add(new Item("New", 0.65, "Test", HOST_ADDR + "image_onion_rings.png"));
//		return items;
		Inventory inventory = Inventory.connectInventory();
		return inventory.getItems();
	}
	
	private String getItemJson() {
		ArrayList<Item> items = getItems();
		
		String itemJson = "[";
		
		for (int i = 0; i < items.size(); i++) {
			itemJson += items.get(i).toString();
			itemJson += ",";
		}
		
		itemJson += "]";
		
		return itemJson;
	}
	
	public void init() {
		chefs.add(new Chef(this));
		reader = new OrderJsonReader(getItems());
		assignTasks();
		try {
			ServerSocket serverSocket = new ServerSocket(PORT);
			System.out.println(getIpAddress());
			while (true) {
				Socket client = serverSocket.accept();
				System.out.println("Client connected");
				System.out.println(client.getInetAddress());
				new HandleThread(client);
			}
		} catch (Exception e) {
			System.out.println("Server Error");
			e.printStackTrace();
		}
	}
	
	private String getIpAddress() {
		String ip = "";
		
		try {
            Enumeration<NetworkInterface> enumNetworkInterfaces =
                    NetworkInterface.getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()){
                NetworkInterface networkInterface = enumNetworkInterfaces.nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface.getInetAddresses();
                while (enumInetAddress.hasMoreElements()){
                    InetAddress inetAddress = enumInetAddress.nextElement();
                    if (inetAddress.isSiteLocalAddress()){
                        ip += "SiteLocalAddress: " + inetAddress.getHostAddress() + "\n";
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
            ip += "SomethingWrong! " + e.toString() + "\n";
        }

        return ip;
	}
	
	private class HandleThread implements Runnable{
		private Socket socket;
		
		public HandleThread(Socket client) {
			this.socket = client;
			new Thread(this).start();
		}

		@Override
		public void run() {
			try {
				DataInputStream inputStream = new 
						DataInputStream(socket.getInputStream());
				String clientRequest = inputStream.readUTF();
				DataOutputStream outputStream = new 
						DataOutputStream(socket.getOutputStream());
				System.out.println(clientRequest);
				try {
					switch (clientRequest) {
					case ORDER:
						doOrder(inputStream, outputStream);
						break;
					case REFRESH:
						doRefresh(outputStream);
						break;
					case QUERY:
						doQuery(inputStream, outputStream);
						break;
					case CONFIRM:
						doConfirm();
						break;
					case CANCEL:
						doCancel();
						break;
					case TRACK:
						doTrack(outputStream);
						break;
					default:
						// TODO exception handling thrown
						break;
					}
				} catch (EOFException e) {
					// TODO: handle exception
					outputStream.writeUTF("Fail\n");
				}
				inputStream.close();
				outputStream.close();
			} catch (IOException e) {
				// TODO: handle exception
				e.printStackTrace();
			} finally {
				if (socket != null) {
					try {
						socket.close();
					} catch (Exception e2) {
						// TODO: handle exception
						socket = null;
						e2.printStackTrace();
					}
				}
			}
			
		}

		private void doRefresh(DataOutputStream outputStream) throws IOException {
			System.out.println(getItemJson());
			outputStream.writeUTF(getItemJson());
		}
		
		private void doOrder(DataInputStream inputStream, 
							DataOutputStream outputStream) throws IOException {
			// TODO: query if order can be fully or partially done
			outputStream.writeUTF("OK");
			String ordJson = inputStream.readUTF();
			System.out.println(ordJson);
			//System.out.println("read json");
			try {
				Order order = reader.parseOrder(ordJson, this.socket.getInetAddress().toString());
				for (int i = 0; i < order.nums.size(); i++) {
					System.out.println(String.format(
							"%s %d", order.items.get(i).name,
							order.nums.get(i)));
				}
				System.out.println(order.ipAddr);
				ordersToOrder.add(order);
				//new Chef(OrderServer.this).doTask(ChefTask.check);
			} catch (BadOrderException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		private void doQuery(DataInputStream inputStream, DataOutputStream outputStream) throws IOException {
			//System.out.println("query from "+ this.socket.getRemoteSocketAddress());
			Order order = ordersOrdered.findOrderByIp(this.socket.getInetAddress().toString());
			if (order == null) {
				System.out.println("Not yet in ordered list");
				outputStream.writeUTF("Preparing\n");
			} else {
				switch (order.queryState) {
				case NotAvailable:
					System.out.println("NotAvailable");
					outputStream.writeUTF("NotAvailable\n");
					break;
				case Patial:
					System.out.println("Partial");
					outputStream.writeUTF("Partial\n");
					inputStream.readUTF();
					String resp = "";
					for (int i = 0; i < order.nums.size(); i++) {
						System.out.println(String.format(
								"%s %d", order.items.get(i).name,
								order.nums.get(i)));
					}
					for (int i = 0; i < getItems().size(); ++i) {
						int num = 0;
						for (int j = 0; j < order.nums.size(); ++j) {
							if (order.items.get(j).name.equals(getItems().get(i).name)) {
								num += order.nums.get(j);
							}
						}
						resp += String.valueOf(num);
						resp += ",";
					}
					resp += "\n";
					outputStream.writeUTF(resp);
					break;
				case Fully:
					System.out.println("Fully");
					outputStream.writeUTF("Fully\n");
					break;
				default:
					System.out.println("Preparing");
					outputStream.writeUTF("Preparing\n");
					break;
				}
			}
		}
		public void doCancel() {
			//System.out.println("DO CANCEL");
			Order order = ordersOrdered.findOrderByIp(this.socket.getInetAddress().toString());
			ordersOrdered.remove(order);
			ordersToOrder.remove(order);
			//System.out.println(ordersOrdered.findOrderByIp(this.socket.getInetAddress().toString())==null);
		}
	
		public void doConfirm() {
			// TODO
			Order order = ordersOrdered.findOrderByIp(this.socket.getInetAddress().toString());
			ordersOrdered.remove(order);
			ordersToDo.add(order);
			//System.out.println(ordersToDo.findOrderByIp(this.socket.getInetAddress().toString())==null);
		}
		
		private void doTrack(DataOutputStream outputStream) throws IOException {
			// TODO 
			Order order = ordersDone.findOrderByIp(this.socket.getInetAddress().toString());
			if (order == null) {
				System.out.println("cooking...");
				outputStream.writeUTF("Cooking\n");
			} else {
				if (order.cookingState == CookingState.Packing) {
					System.out.println("Done");
					outputStream.writeUTF("Packing\n");
				}
			}
			
		}
		
	}
	
	
	public void assignTasks() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				while (true) {
					Iterator<Chef> iterator = chefs.iterator();
					while (iterator.hasNext() && (!ordersToOrder.isEmpty() || !ordersToDo.isEmpty())) {
						Chef chef = (Chef) iterator.next();
						if (!chef.busy) {
							if (!ordersToOrder.isEmpty()) {
								chef.doTask(ChefTask.check);
								continue;
							}
							if (!ordersToDo.isEmpty()) {
								System.out.println("assign cooking task");
								chef.doTask(ChefTask.cook);
								continue;
							}
						}
					}
					
					try {
						TimeUnit.MILLISECONDS.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}).start();
	}
	
	
	
	public static void main(String[] args) {
		System.out.println("Order Server launched ...");
		OrderServer orderServer = new OrderServer();
		orderServer.init();
	}
}
