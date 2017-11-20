package com.example.orangeparadise.clientUtility;

import com.example.orangeparadise.clientorderannotates.ClientAnnotation;

/**
 * Created by 97159 on 11/8/2017.
 */

@ClientAnnotation(id = 3, use = ClientAnnotation.usage.SOURCE)
public class OrderItem {
    String itemName;
    Double itemPrice;
    String itemUrl;
    String itemDesciption;

    public OrderItem(String itemName, Double itemPrice, String itemUrl, String itemDesciption) {
        this.itemName = itemName;
        this.itemPrice = itemPrice;
        this.itemUrl = itemUrl;
        this.itemDesciption = itemDesciption;
    }

    public Double getItemPrice() {
        return itemPrice;
    }

    public String getItemDesciption() {
        return itemDesciption;
    }

    public String getItemName() {
        return itemName;
    }

    public String getItemUrl() {
        return itemUrl;
    }
}
