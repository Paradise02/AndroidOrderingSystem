package com.example.orangeparadise.clientorderdemo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.orangeparadise.clientUtility.OrderItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 97159 on 11/10/2017.
 */

public class OrderActivity extends Activity implements ServiceConnection{
    private ListView listView;
    private List<Order> orders = new ArrayList<>();
    private List<Integer> orderNums;
    private List<OrderItem> orderItems;
    private OrderListAdapter listAdapter;
    private ClientOrderService clientOrderService;

    private static final String TAG = "OrderConfirmActivity";

    private FloatingActionButton FABItem;
    private Button ordButton;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.order_layout);

        orders.add(new Order(new OrderItem("default", 0.0, "", "default"),
                0));
        listView = (ListView) findViewById(R.id.ord_list_view);
        listAdapter = new OrderListAdapter(this, orders);
        listView.setAdapter(listAdapter);

        FABItem = (FloatingActionButton) findViewById(R.id.FABItem);

        FABItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrderActivity.this, MainActivityVertical.class);
                OrderActivity.this.startActivity(intent);
            }
        });

        ordButton = (Button) findViewById(R.id.ord_item_btn);

        ordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clientOrderService.orderItemRequest();
                clientOrderService.currentState = ClientOrderService.OrderState.Ordering;
                OrderActivity.this.startActivity(new Intent(OrderActivity.this, ConfirmActivity.class));
            }
        });
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        //Log.i(TAG, String.valueOf(orders.size()));
        clientOrderService = ((ClientOrderService.OrderBinder) service).getService();
        List<OrderItem> orderItems = clientOrderService.getItems();
        List<Integer> orderNums = clientOrderService.orderNums;
        orders.clear();
        for (int i = 0; i < orderItems.size(); i++){
            if (orderNums.get(i) > 0){
                //Log.i(TAG, orderItems.get(i).getItemName() + " " + orderNums.get(i));
                orders.add(new Order(
                        orderItems.get(i),
                        orderNums.get(i)
                ));
            }
        }
        this.orderNums = orderNums;
        this.orderItems = orderItems;
        listAdapter.notifyDataSetChanged();
        Log.i(TAG, String.valueOf(orders.size()));
        //Log.i(TAG, "Notify Data change");
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        clientOrderService = null;
    }

    @Override
    protected void onResume() {
        registerReceiver(receiver, new IntentFilter(ClientOrderService.NOTIFICATION));
        Intent intent = new Intent(this, ClientOrderService.class);
        bindService(intent, this, Context.BIND_AUTO_CREATE);
        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(receiver);
        unbindService(this);
        super.onPause();
    }

    static class ViewHolder{
        public ImageButton imgbtnPlus;
        public ImageButton imgbtnMinus;
        public TextView totalPrice;
        public TextView title;
        public TextView info;
    }

    class OrderListAdapter extends BaseAdapter{
        private LayoutInflater inflater = null;
        private List<Order> orders = new ArrayList<>();

        OrderListAdapter(Context context, List<Order> orders){
            this.inflater = LayoutInflater.from(context);
            this.orders = orders;
        }

        @Override
        public int getCount() {
            return this.orders.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null){
                holder = new ViewHolder();
                convertView = this.inflater.inflate(R.layout.item_order, null);
                holder.title = (TextView) convertView.findViewById(R.id.ord_item_title);
                holder.info = (TextView) convertView.findViewById(R.id.ord_item_amt);
                holder.totalPrice = (TextView) convertView.findViewById(R.id.ord_item_totalPrice);
                holder.imgbtnMinus = (ImageButton) convertView.findViewById(R.id.ord_imgbtn_item_minus);
                holder.imgbtnPlus = (ImageButton) convertView.findViewById(R.id.ord_imgbtn_item_plus);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }
            holder.title.setText(String.valueOf(orders.get(position).item.getItemName()));
            holder.info.setText(String.valueOf(orders.get(position).num));
            holder.totalPrice.setText("$"+orders.get(position).item.getItemPrice()*orders.get(position).num);

            if (orderItems != null) {
                holder.imgbtnPlus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        orders.set(position, new Order(orders.get(position).item,
                                orders.get(position).num + 1));
                        for (int i = 0; i < orderItems.size(); ++i) {
                            if (orderItems.get(i).getItemName().equals(orders.get(position).item.getItemName())) {
                                orderNums.set(i, orders.get(position).num);
                                break;
                            }
                        }
                        OrderListAdapter.this.notifyDataSetChanged();
                    }
                });

                holder.imgbtnMinus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        orders.set(position, new Order(orders.get(position).item,
                                orders.get(position).num - 1));
                        for (int i = 0; i < orderItems.size(); ++i){
                            if (orderItems.get(i).getItemName().equals(orders.get(position).item.getItemName())){
                                orderNums.set(i, orders.get(position).num);
                                break;
                            }
                        }
                        if (orders.get(position).num == 0){
                            orders.remove(position);
                        }
                        OrderListAdapter.this.notifyDataSetChanged();
                        if (clientOrderService.orderEmpty()){
                            Toast.makeText(getBaseContext(), "Bag is empty, redirect to order form", Toast.LENGTH_LONG).show();

                            Intent intent = new Intent(OrderActivity.this, MainActivityVertical.class);
                            OrderActivity.this.startActivity(intent);
                        }
                    }
                });
            }

            return convertView;
        }
    }
}

class Order{
    OrderItem item;
    int num;

    Order(OrderItem item, int num){
        this.item =item;
        this.num = num;
    }
}
