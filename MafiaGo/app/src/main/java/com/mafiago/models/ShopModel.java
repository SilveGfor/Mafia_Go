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
    public ArrayList<ShopModel> list_usual_prices;
    public ArrayList<ShopModel> list_premium_prices;
    public String[] list_meaning;
    
    public ShopModel(String type, String amount, int price, boolean is_sale, String transaction_description, String sale_amount, int num) {
        this.type = type;
        this.amount = amount;
        this.price = price;
        this.is_sale = is_sale;
        this.transaction_description = transaction_description;
        this.sale_amount = sale_amount;
        this.num = num;
    }

    //для статусов
    public ShopModel(String type, ArrayList<ShopModel> list_usual_prices, String[] list_meaning, ArrayList<ShopModel> list_premium_prices) {
        this.type = type;
        this.list_usual_prices = list_usual_prices;
        this.list_premium_prices = list_premium_prices;
        this.list_meaning = list_meaning;
    }
    //для цветов и конвертации, где не нужен list_premium_prices
    public ShopModel(String type, ArrayList<ShopModel> list_usual_prices, String[] list_meaning) {
        this.type = type;
        this.list_usual_prices = list_usual_prices;
        this.list_meaning = list_meaning;
    }
}
