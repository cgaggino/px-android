package com.mercadopago.android.px.core.internal;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.core.PaymentProcessor;
import com.mercadopago.android.px.core.SplitPaymentProcessor;
import com.mercadopago.android.px.internal.viewmodel.mappers.Mapper;
import com.mercadopago.android.px.model.BusinessPayment;
import com.mercadopago.android.px.model.GenericPayment;
import com.mercadopago.android.px.model.Payment;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;

public class PaymentListenerMapper
    extends Mapper<SplitPaymentProcessor.OnPaymentListener, PaymentProcessor.OnPaymentListener> {

    @Override
    public PaymentProcessor.OnPaymentListener map(@NonNull final SplitPaymentProcessor.OnPaymentListener val) {
        return new PaymentProcessor.OnPaymentListener() {
            @Override
            public void onPaymentFinished(@NonNull final Payment payment) {
                val.onPaymentFinished(payment);
            }

            @Override
            public void onPaymentFinished(@NonNull final GenericPayment genericPayment) {
                val.onPaymentFinished(genericPayment);
            }

            @Override
            public void onPaymentFinished(@NonNull final BusinessPayment businessPayment) {

            }

            @Override
            public void onPaymentError(@NonNull final MercadoPagoError error) {

            }
        };
    }
}
