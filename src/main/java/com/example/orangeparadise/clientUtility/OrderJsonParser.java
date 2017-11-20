package com.example.orangeparadise.clientUtility;

/**
 * Created by 97159 on 11/14/2017.
 */

public class OrderJsonParser {

    String itemName;
    Integer itemAmt;

    public OrderJsonParser(String itemName, Integer itemAmt){
        this.itemName = itemName;
        this.itemAmt = itemAmt;
    }

    @Override
    public String toString() {
        return "{ \"name\" : \"" + this.itemName + "\", \"amount\": \"" + itemAmt + "\"}";
    }
}
