package com.app.bemyrider.model.partner;

import com.app.bemyrider.model.Pagination;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class PartnerPaymentHistoryPojoData {

    @SerializedName("transection_list")
    private ArrayList<PartnerPaymentHistoryItem> partnerPaymentHistoryItem;

    @SerializedName("pagination")
    private Pagination pagination;

    public ArrayList<PartnerPaymentHistoryItem> getPartnerPaymentHistoryItem() {
        return partnerPaymentHistoryItem;
    }

    public void setPartnerPaymentHistoryItem(ArrayList<PartnerPaymentHistoryItem> partnerPaymentHistoryItem) {
        this.partnerPaymentHistoryItem = partnerPaymentHistoryItem;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }
}
