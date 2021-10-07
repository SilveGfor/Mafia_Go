package com.mafiago.models;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.SkuDetails;

public class GoldModel {
    public SkuDetails skuDetails;
    public BillingClient billingClient;
    public int num; //номер для картинки

    public GoldModel(SkuDetails skuDetails, BillingClient billingClient, int num) {
        this.skuDetails = skuDetails;
        this.billingClient = billingClient;
        this.num = num;
    }
}
