package com.mafiago.models;

import java.util.ArrayList;

public class ShopModel {
    public String type;
    public String amount;//золото
    public int price;//рубли
    public int num;
    public boolean is_sale;
    public String transaction_description;
    public String sale_amount;
    public ArrayList<ShopModel> list_prices;
    public String[] list_statuses;
    
    public ShopModel(String type, String amount, int price, boolean is_sale, String transaction_description, String sale_amount, int num) {
        this.type = type;
        this.amount = amount;
        this.price = price;
        this.is_sale = is_sale;
        this.transaction_description = transaction_description;
        this.sale_amount = sale_amount;
        this.num = num;
    }

    public ShopModel(String type, ArrayList<ShopModel> list_prices, String[] list_statuses, int num) {
        this.type = type;
        this.list_prices = list_prices;
        this.list_statuses = list_statuses;
        this.num = num;
    }
}
