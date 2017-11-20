package com.example.orangeparadise.clientorderdemo;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.example.orangeparadise.clientException.NoSocketAvailableException;
import com.example.orangeparadise.clientException.OtherClientException;
import com.example.orangeparadise.clientUtility.ClientJsonReader;
import com.example.orangeparadise.clientUtility.Contact;
import com.example.orangeparadise.clientUtility.OrderItem;
import com.example.orangeparadise.clientUtility.OrderJsonParser;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;


public class ClientOrderService extends Service {
    private Binder orderBinder = new OrderBinder();
    private static final String ADDR = "192.168.1.13";
    private static final String PORT = "4040";
    public static final String NOTIFICATION = "com.example.orangeparadise.clientorderdemo.NOTIFICATION";
    public static final String RESULT_TYPE = "type";
    public static final String RESULT = "result";
    public static final String AVAILABLE = "available";
    public static final String FAILED = "failed";

    private static final String TAG = "ClientOrderService";

    public enum OrderState{
        Watching,
        Ordering,
        Confirming,
        Preparing,
        Done
    }

    public OrderState currentState = OrderState.Watching;

    public static final String OPERATION = "operation";
    public static final int CONNECT = 0;
    public static final int ORDER = 1;

    private Socket socket = null;

    //private List<Contact> contacts = new ArrayList<>();
    private List<OrderItem> items = new ArrayList<>();
    public List<Integer> orderNums = new ArrayList<>();

    private ClientJsonReader jsonReader = new ClientJsonReader();

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return orderBinder;
    }

    public class OrderBinder extends Binder{
        public ClientOrderService getService(){
            Log.d(TAG, "return service reference");
            return ClientOrderService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Client Service begin...");
        //Bundle bundle = intent.getExtras();

        return super.onStartCommand(intent, flags, startId);
    }

    private void broadcast(String type, String result){
        // TODO: Broadcast information extracted from socket

        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(RESULT_TYPE, type);
        intent.putExtra(RESULT, result);
        sendBroadcast(intent);
    }

    private void broadcast(String type){
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(RESULT_TYPE, type);
        sendBroadcast(intent);
    }

    public void initConnection() throws NoSocketAvailableException, OtherClientException{
        fetchItemRequest();
        /*try {
            Log.i(TAG, "TRY TO CONNECT..." + ADDR);
            socket = new Socket(ADDR, Integer.parseInt(PORT));

            Log.i(TAG, "SOCKET accepted, client port:" + socket.getLocalPort());

            DataInputStream inputStream = new
                    DataInputStream(socket.getInputStream());
            Log.i(TAG, "GET INPUTSTREAM...");

            String test = inputStream.toString();
            Log.i(TAG, test);

            String ret = inputStream.readUTF();
            Log.i(TAG, "receive: " + ret);
            items = jsonReader.parseOrderItem(ret);
            for (int i = 0; i < items.size(); ++i){
                Log.i(TAG, items.get(i).toString());
            }

            if (items.size() == 0){
                Log.i(TAG, "No item data fetched");
                broadcast(FAILED);
                return;
            }

            broadcast(AVAILABLE, String.valueOf(items.size()));

            *//*clientListeningThread = new ClientListeningThread(socket);
            clientListeningThread.start();*//*
        } catch (UnknownHostException e) {
            OtherClientException otherClientException = new OtherClientException();
            otherClientException.initCause(e);
            e.printStackTrace();
            broadcast(FAILED);
        } catch (IOException e) {
            OtherClientException otherClientException = new OtherClientException();
            otherClientException.initCause(e);
            throw otherClientException;
        } finally {
            if (socket != null){
                try{
                    socket.close();
                } catch (IOException e) {
                    socket = null;
                    e.printStackTrace();
                }
            }
        }*/
    }

    public void fetchItemRequest(){
        // TODO: refresh on click
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.i(TAG, "TRY TO CONNECT..." + ADDR);
                    socket = new Socket(ADDR, Integer.parseInt(PORT));

                    Log.i(TAG, "SOCKET accepted, client port:" + socket.getLocalPort());
                    DataOutputStream outputStream = new
                            DataOutputStream(socket.getOutputStream());

                    outputStream.writeUTF("refresh");
                    Log.i(TAG, "Refreshing...");

                    DataInputStream inputStream = new
                            DataInputStream(socket.getInputStream());
                    Log.i(TAG, "GET INPUTSTREAM...");

                    String test = inputStream.toString();
                    Log.i(TAG, test);

                    String ret = inputStream.readUTF();
                    Log.i(TAG, "receive: " + ret);
                    items = jsonReader.parseOrderItem(ret);
                    for (int i = 0; i < items.size(); ++i){
                        Log.i(TAG, items.get(i).toString());
                    }

                    if (items.size() == 0){
                        Log.i(TAG, "No item data fetched");
                        broadcast(FAILED);
                        return;
                    }

                    broadcast(AVAILABLE, String.valueOf(items.size()));
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                for (int i = 0; i < items.size(); ++i){
                    orderNums.add(0);
                }
            }
        }).start();
    }

    public void orderItemRequest(){
        // TODO: order on click
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.i(TAG, "TRY TO CONNECT..." + ADDR);
                    socket = new Socket(ADDR, Integer.parseInt(PORT));

                    Log.i(TAG, "SOCKET accepted, client port:" + socket.getLocalPort());
                    DataOutputStream outputStream = new
                            DataOutputStream(socket.getOutputStream());

                    outputStream.writeUTF("order");
                    Log.i(TAG, "Ordering...");

                    DataInputStream inputStream = new
                            DataInputStream(socket.getInputStream());
                    Log.i(TAG, "GET INPUTSTREAM...");

                    String response = inputStream.readUTF();
                    Log.i(TAG, response);

                    if (response.equals("OK")){
                        Log.i(TAG, "Place Order List...");
                        outputStream.writeUTF(assembleOrderJson());
                    }

                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    /*private class ClientListeningThread extends Thread{
        Socket socket = null;

        ClientListeningThread(Socket socket){
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                DataInputStream inputStream = new
                        DataInputStream(socket.getInputStream());
                String ret = inputStream.readUTF();
                Log.i(TAG, "receive: " + ret);

                broadcast(AVAILABLE, ret);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }*/

    public List<OrderItem> getItems() {
        ArrayList<OrderItem> items = new ArrayList<>();
        items.addAll(this.items);
        return items;
    }

    public boolean orderEmpty(){
        for (int i : orderNums){
            if (i > 0) return false;
        }
        return true;
    }

    private String assembleOrderJson(){
        String json = "[";
        for (int i = 0; i < items.size(); ++i){
            if (orderNums.get(i) > 0){
                OrderJsonParser ord = new OrderJsonParser(
                        items.get(i).getItemName(),
                        orderNums.get(i)
                );
                json += ord;
                json += ",";
            }
        }
        json += "]";
        Log.i(TAG, json);
        return json;
    }
}
