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
    private TextView txtReceipt, txtReceiptName, txtReceiptQty, txtReceiptTotal;
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
        txtReceiptName = (TextView) findViewById(R.id.txt_confirm_receipt_name);
        txtReceiptQty = (TextView) findViewById(R.id.txt_confirm_receipt_qty);
        txtReceiptTotal = (TextView) findViewById(R.id.txt_confirm_receipt_total);
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
                clientOrderService.confirmOrderRequest();
                ConfirmActivity.this.startActivity(new Intent(ConfirmActivity.this, MainActivityVertical.class));
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clientOrderService.cancelOrderRequest();
                ConfirmActivity.this.startActivity(new Intent(ConfirmActivity.this, MainActivityVertical.class));
            }
        });

        Bundle bundle = getIntent().getExtras();
        String result = bundle.getString(ClientOrderService.RESULT);
        if (result.equals(ClientOrderService.PARTIAL)){
            Toast.makeText(getBaseContext(), "Sorry your order can only be partially prepared", Toast.LENGTH_LONG).show();
        }
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
        getReceipt();
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

    @Override
    public void onBackPressed() {
        startActivity(new Intent(getBaseContext(), MainActivityVertical.class));
    }

    private void getReceipt(){
        String receipt = "receipt:\n";
        String receiptName = "Item\n";
        String receiptQty = "Qty\n";
        String receiptPrice = "Price\n";

        receipt += String.format("%-20s %-15s %-10s\n", "Item", "Qty", "Price");

        double total = 0.0;
        for (int i = 0; i < orders.size(); ++i){
            /*String receiptLine = String.format("%-20s %-15s %-10.2f\n",
                    orders.get(i).item.getItemName(),
                    orders.get(i).num,
                    orders.get(i).num * orders.get(i).item.getItemPrice());
            receipt += receiptLine;*/
            total += orders.get(i).num * orders.get(i).item.getItemPrice();
            receiptName += orders.get(i).item.getItemName();
            receiptName += "\n";
            receiptQty += String.valueOf(orders.get(i).num);
            receiptQty += "\n";
            receiptPrice += String.format("$%.2f\n", orders.get(i).num * orders.get(i).item.getItemPrice());
        }

        receiptName += "Tax\n";
        receiptQty += "\n";
        receiptPrice += String.format("$%.2f\n", total*0.06);
        receiptName += "Total\n";
        receiptQty += "\n";
        receiptPrice += String.format("$%.2f\n", total*1.06);

        txtReceiptName.setText(receiptName);
        txtReceiptQty.setText(receiptQty);
        txtReceiptTotal.setText(receiptPrice);
    }
}
