package com.mafiago.models;

public class GoldModel {
    public String description;
    public String amount;//золото
    public int price;//рубли
    public int num;
    public boolean is_sale;
    public String transaction_description;
    public String sale_amount;
    public String type;


    public GoldModel(String description, String amount, int price, boolean is_sale, String transaction_description, String sale_amount, int num, String type) {
        this.description = description;
        this.amount = amount;
        this.price = price;
        this.is_sale = is_sale;
        this.transaction_description = transaction_description;
        this.sale_amount = sale_amount;
        this.num = num;
        this.type = type;
    }
}
