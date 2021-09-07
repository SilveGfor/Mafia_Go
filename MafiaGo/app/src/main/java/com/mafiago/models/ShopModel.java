package com.mafiago.models;

public class ShopModel {
    public String type;
    public int amount;//золото
    public int price;//рубли
    public int num;
    public boolean is_sale;
    public String transaction_description;
    public String sale_amount;


    public ShopModel(String type, int amount, int price, boolean is_sale, String transaction_description, String sale_amount, int num) {
        this.type = type;
        this.amount = amount;
        this.price = price;
        this.is_sale = is_sale;
        this.transaction_description = transaction_description;
        this.sale_amount = sale_amount;
        this.num = num;
    }
}
