package com.mercadopago.android.px.model;

import android.support.annotation.Nullable;
import com.mercadopago.android.px.internal.util.TextUtil;
import java.io.Serializable;
import java.math.BigDecimal;

public class PaymentData implements Serializable {
    private BigDecimal transactionAmount;
    private PaymentMethod paymentMethod;
    @Nullable private Issuer issuer;
    @Nullable private PayerCost payerCost;
    @Nullable private Token token;
    private Payer payer;

    /**
     * @deprecated CouponCode discount is not supported anymore.
     */
    @Deprecated
    private String couponCode;

    @Nullable private Discount discount;
    @Nullable private Campaign campaign;

    @Nullable
    public Campaign getCampaign() {
        return campaign;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    @Deprecated
    public void setPaymentMethod(final PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    @Nullable
    public Issuer getIssuer() {
        return issuer;
    }

    @Deprecated
    public void setIssuer(@Nullable final Issuer issuer) {
        this.issuer = issuer;
    }

    @Nullable
    public PayerCost getPayerCost() {
        return payerCost;
    }

    @Deprecated
    public void setPayerCost(@Nullable final PayerCost payerCost) {
        this.payerCost = payerCost;
    }

    @Nullable
    public Token getToken() {
        return token;
    }

    @Deprecated
    public void setToken(final Token token) {
        this.token = token;
    }

    @Deprecated
    public void setDiscount(@Nullable final Discount discount) {
        this.discount = discount;
    }

    @Nullable
    public Discount getDiscount() {
        return discount;
    }

    public Payer getPayer() {
        return payer;
    }

    @Deprecated
    public void setPayer(final Payer payer) {
        this.payer = payer;
    }

    /**
     * @param couponCode The coupon code
     * @deprecated CouponCode discount is not supported anymore.
     */
    @Deprecated
    public void setCouponCode(final String couponCode) {
        this.couponCode = couponCode;
    }

    /**
     * @return code
     * @deprecated CouponCode discount is not supported anymore.
     */
    @Deprecated
    public String getCouponCode() {
        return couponCode;
    }

    public BigDecimal getTransactionAmount() {
        return transactionAmount;
    }

    @Deprecated
    public void setTransactionAmount(final BigDecimal transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    @Deprecated
    public void setCampaign(@Nullable final Campaign campaign) {
        this.campaign = campaign;
    }

    public boolean containsCardInfo() {
        return getToken() != null && !TextUtil.isEmpty(getToken().getCardId());
    }
}
