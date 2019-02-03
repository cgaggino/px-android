package com.mercadopago.android.px.model.commission;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.repository.ChargeRepository;
import java.io.Serializable;
import java.math.BigDecimal;

public final class PaymentTypeChargeRule implements Serializable {

    @NonNull
    private final BigDecimal charge;

    @NonNull
    private final String paymentTypeId;

    /**
     * @param paymentTypeId the payment type associated with the charge to shouldBeTriggered.
     * @param charge the charge amount to apply for this rule
     */
    public PaymentTypeChargeRule(@NonNull final String paymentTypeId,
        @NonNull final BigDecimal charge) {
        this.paymentTypeId = paymentTypeId;
        this.charge = charge;
    }

    public boolean shouldBeTriggered(@NonNull final ChargeRepository chargeRepository) {
        return chargeRepository.shouldApply(this);
    }

    @NonNull
    public BigDecimal charge() {
        return charge;
    }

    @NonNull
    public String getPaymentTypeId() {
        return paymentTypeId;
    }
}
