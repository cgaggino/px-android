package com.mercadopago.android.px.internal.repository;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.model.commission.PaymentTypeChargeRule;
import java.math.BigDecimal;

public interface ChargeRepository {

    @NonNull
    BigDecimal getChargeAmount();

    boolean shouldApply(PaymentTypeChargeRule paymentTypeRule);
}
