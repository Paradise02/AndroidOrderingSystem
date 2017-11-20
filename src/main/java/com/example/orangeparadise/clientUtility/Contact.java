package com.example.orangeparadise.clientUtility;

import com.example.orangeparadise.clientorderannotates.ClientAnnotation;

/**
 * Created by 97159 on 11/7/2017.
 */

@ClientAnnotation(id = 2, use = ClientAnnotation.usage.TEST)
public class Contact{
    String fn;
    String ln;
    String phone;
    String address;
    String birthday;

    public Contact(String f, String l, String phone, String address, String birth) {
        this.fn = f;
        this.ln = l;
        this.phone = phone;
        this.address = address;
        this.birthday = birth;
    }

    @Override
    public String toString() {
        return ("{\"firstname\" : \""+ this.fn +
                "\", \"lastname\": \""+ this.ln +
                "\", \"phone\": \""+ this.phone +
                "\", \"address\": \""+ this.address +
                "\", \"birthday\": \""+ this.birthday + "\"},");
    }

    public String getFn() {
        return fn;
    }

    public String getLn() {
        return ln;
    }

    public String getAddress() {
        return address;
    }

    public String getBirthday() {
        return birthday;
    }

    public String getPhone() {
        return phone;
    }
}

