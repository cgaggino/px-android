package com.mercadopago.android.px.model;

import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Split DTO - represents the split payment amount and charges.
 */
@Keep
public class Split implements Serializable {

    /**
     * determines if the split payment is active by default or not.
     */
    public boolean defaultEnabled;

    /**
     * message to show in split label.
     */
    @NonNull public String message;

    /**
     * if the split payment is between a card and account money
     */
    @NonNull public List<PayerCost> payerCosts;

    /**
     * amount to pay with alternative payment method - always account money.
     */
    @NonNull public BigDecimal amount;

    @NonNull
    public String secondaryPaymentMethodId;

    /**
     * Default selected payer cost index
     */
    public int selectedPayerCostIndex;

    @Nullable
    public Discount primaryPaymentMethodDiscount;

    @Nullable
    public Discount secondaryPaymentMethodDiscount;

    @NonNull
    public List<PayerCost>
    getPayerCosts() {
        return payerCosts == null ? new ArrayList<PayerCost>() : payerCosts;
    }

    @NonNull
    public BigDecimal getVisibleAmountToPay() {
        return secondaryPaymentMethodDiscount == null ? amount
            : amount.subtract(secondaryPaymentMethodDiscount.getCouponAmount());
    }
}
