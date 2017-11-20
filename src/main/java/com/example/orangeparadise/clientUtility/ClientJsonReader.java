package com.example.orangeparadise.clientUtility;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by 97159 on 11/7/2017.
 */

public class ClientJsonReader {

    String testSuite = new String(
            "[{'firstname' : 'upwm', 'lastname': 'dn', 'phone': '781-995-8474', 'address': 'xxx-xxx-xxx', 'birthday': '1995-11-15'}," +
            "{'firstname' : 'gnikiqkd', 'lastname': 'topi', 'phone': '781-995-1127', 'address': 'xxx-xxx-xxx', 'birthday': '1995-4-2'}," +
            "{'firstname' : 'zppglvsde', 'lastname': 'kbwdup', 'phone': '781-995-0727', 'address': 'xxx-xxx-xxx', 'birthday': '1991-2-2'}," +
            "{'firstname' : 'fiituiry', 'lastname': 'ny', 'phone': '781-995-1720', 'address': 'xxx-xxx-xxx', 'birthday': '1990-5-17'}," +
            "{'firstname' : 'iuv', 'lastname': 'bfign', 'phone': '781-995-4716', 'address': 'xxx-xxx-xxx', 'birthday': '1997-6-11'}] ");

    public ArrayList<OrderItem> parseOrderItem(String jsonOrderItem) {
        ArrayList<OrderItem> orderItems = new ArrayList<>();

        try {
            JSONArray jsonArray = new JSONArray(jsonOrderItem);
            for (int i = 0; i < jsonArray.length(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String itemName = jsonObject.getString("name");
                String itemUrl = jsonObject.getString("url");
                Double itemPrice = jsonObject.getDouble("price");
                String itemDescription = jsonObject.getString("description");

                orderItems.add(new OrderItem(itemName, itemPrice, itemUrl, itemDescription));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return orderItems;
    }

    public ArrayList<Contact> parseContacts(String jsonContact){
        ArrayList<Contact> contacts = new ArrayList<>();

        try {
            JSONArray jsonArray = new JSONArray(jsonContact);
            for (int i = 0; i < jsonArray.length(); ++i){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String firstname = jsonObject.getString("firstname");
                Log.i("JSONReader TEST", firstname);

                contacts.add(new Contact(jsonObject.getString("firstname"),
                        jsonObject.getString("lastname"),
                        jsonObject.getString("phone"),
                        jsonObject.getString("address"),
                        jsonObject.getString("birthday")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return contacts;

    }

    public void test(){
        parseContacts(testSuite);
    }


}
