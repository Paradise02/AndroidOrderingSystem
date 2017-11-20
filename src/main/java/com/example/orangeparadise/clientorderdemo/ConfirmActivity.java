package com.example.orangeparadise.clientorderdemo;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.orangeparadise.clientUtility.OrderItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 97159 on 11/16/2017.
 */

public class ConfirmActivity extends Activity implements ServiceConnection{

    private ClientOrderService clientOrderService;
    private static final String TAG = "ConfirmActivity";
    private TextView txtReceipt;
    private FloatingActionButton FABitem;
    private Button btnConfirm;
    private Button btnCancel;

    private ArrayList<Order> orders = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.confirm_order);
        super.onCreate(savedInstanceState);
        orders.add(null);
        txtReceipt = (TextView) findViewById(R.id.txt_confirm_receipt);
        FABitem = (FloatingActionButton) findViewById(R.id.FABItem);
        btnCancel = (Button) findViewById(R.id.ord_cancel_btn);
        btnConfirm = (Button) findViewById(R.id.ord_confirm_btn);

        FABitem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ConfirmActivity.this, MainActivityVertical.class);
                ConfirmActivity.this.startActivity(intent);
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getBaseContext(), getReceipt(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        clientOrderService = ((ClientOrderService.OrderBinder) service).getService();
        List<OrderItem> orderItems = clientOrderService.getItems();
        List<Integer> orderNums = clientOrderService.orderNums;
        this.orders.clear();
        for (int i = 0; i < orderItems.size(); i++){
            if (orderNums.get(i) > 0){
                this.orders.add(new Order(
                        orderItems.get(i),
                        orderNums.get(i)
                ));
            }
        }
        Log.d(TAG, getReceipt());
        txtReceipt.setText(getReceipt());
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        clientOrderService = null;
    }

    @Override
    protected void onResume() {
        Intent intent = new Intent(this, ClientOrderService.class);
        bindService(intent, this, BIND_AUTO_CREATE);
        super.onResume();
    }

    @Override
    protected void onPause() {
        unbindService(this);
        super.onPause();
    }

    private String getReceipt(){
        String receipt = "receipt:\n";

        receipt += String.format("%-20s %-15s %-10s\n", "Item", "Qty", "Price");

        double total = 0.0;
        for (int i = 0; i < orders.size(); ++i){
            String receiptLine = String.format("%-20s %-15s %-10.2f\n",
                    orders.get(i).item.getItemName(),
                    orders.get(i).num,
                    orders.get(i).num * orders.get(i).item.getItemPrice());
            receipt += receiptLine;
            total += orders.get(i).num * orders.get(i).item.getItemPrice();
        }

        receipt += String.format("%-20s %-15s %-10.2f\n", "Tax", "", total * 0.06);
        receipt += String.format("%-20s %-15s %-10.2f\n", "Total", "", total * 1.06);

        return receipt;
    }
}
