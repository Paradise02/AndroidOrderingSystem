package com.example.orangeparadise.serverorder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;

public class OrderQueue implements Queue<Order>{
	
	ArrayList<Order> orders = new ArrayList<>();

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		OrderQueue orderQueue = new OrderQueue();
	}

	
	public Order findOrderByIp(String ipAddr) {
		// find an order in queue by client ipAddr
		//System.out.println("Query ip:" + ipAddr);
		for(Order order:this.orders) {
			//System.out.println(order.ipAddr);
			if (order.ipAddr.equals(ipAddr)) {
				return order;
			}
		}
		return null;
	}
	
	@Override
	public int size() {
		return this.orders.size();
	}

	@Override
	public boolean isEmpty() {
		return this.orders.size() == 0;
	}

	@Override
	public boolean contains(Object o) {
		return this.orders.contains(o);
	}

	@Override
	public Iterator<Order> iterator() {
		return this.orders.iterator();
	}

	@Override
	public Object[] toArray() {
		return this.orders.toArray();
	}

	@Override
	public <Order> Order[] toArray(Order[] a) {
		return (Order[]) this.orders.toArray(a);
	}

	@Override
	public boolean remove(Object o) {
		return this.orders.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return this.orders.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends Order> c) {
		return this.orders.addAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return this.orders.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return this.orders.retainAll(c);
	}

	@Override
	public void clear() {
		this.orders.clear();
	}

	@Override
	public boolean add(Order e) {
		return this.orders.add(e);
	}

	@Override
	public boolean offer(Order e){
		return false;
	}

	@Override
	public Order remove() {
		if (this.orders.size() > 0) {
			return this.orders.remove(0);
		} else {
			throw new NoSuchElementException("remove method in Order Queue");
		}
	}

	@Override
	public Order poll() {
		return this.orders.remove(0);
	}

	@Override
	public Order element() {
		if (this.orders.size() > 0) {
			return this.orders.get(0);
		} else {
			throw new NoSuchElementException("element method in Order Queue");
		}
		
	}

	@Override
	public Order peek() {
		return this.orders.get(0);
	}

}
