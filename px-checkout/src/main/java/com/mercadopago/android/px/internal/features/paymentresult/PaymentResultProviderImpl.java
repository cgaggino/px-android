package com.mercadopago.android.px.internal.features.paymentresult;

import android.content.Context;
import android.support.annotation.NonNull;
import com.mercadopago.android.px.R;

public class PaymentResultProviderImpl implements PaymentResultProvider {
    private final Context context;

    /* default */ PaymentResultProviderImpl(@NonNull final Context context) {
        this.context = context;
    }

    @Override
    public String getStandardErrorMessage() {
        return context.getString(R.string.px_standard_error_message);
    }

    @Override
    public String getApprovedTitle() {
        return context.getString(R.string.px_title_approved_payment);
    }

    @Override
    public String getPendingTitle() {
        return context.getString(R.string.px_title_pending_payment);
    }

    @Override
    public String getRejectedOtherReasonTitle(String paymentMethodName) {
        return String.format(context.getString(R.string.px_title_other_reason_rejection), paymentMethodName);
    }

    @Override
    public String getRejectedInsufficientAmountTitle(String paymentMethodName) {
        return String.format(context.getString(R.string.px_text_insufficient_amount), paymentMethodName);
    }

    @Override
    public String getRejectedDuplicatedPaymentTitle(String paymentMethodName) {
        return String.format(context.getString(R.string.px_title_other_reason_rejection), paymentMethodName);
    }

    @Override
    public String getRejectedCardDisabledTitle(String paymentMethodName) {
        return String.format(context.getString(R.string.px_text_active_card), paymentMethodName);
    }

    @Override
    public String getRejectedBadFilledCardTitle(String paymentMethodName) {
        return String.format(context.getString(R.string.px_text_some_card_data_is_incorrect), paymentMethodName);
    }

    @Override
    public String getRejectedBadFilledCardTitle() {
        return context.getString(R.string.px_text_card_data_invalid);
    }

    @Override
    public String getRejectedHighRiskTitle() {
        return context.getString(R.string.px_title_rejection_high_risk);
    }

    @Override
    public String getRejectedMaxAttemptsTitle() {
        return context.getString(R.string.px_title_rejection_max_attempts);
    }

    @Override
    public String getRejectedInsufficientDataTitle() {
        return context.getString(R.string.px_bolbradesco_rejection);
    }

    @Override
    public String getRejectedBadFilledOther() {
        return context.getString(R.string.px_title_bad_filled_other);
    }

    @Override
    public String getRejectedCallForAuthorizeTitle() {
        return context.getString(R.string.px_title_activity_call_for_authorize);
    }

    @Override
    public String getPendingLabel() {
        return context.getString(R.string.px_pending_label);
    }

    @Override
    public String getRejectionLabel() {
        return context.getString(R.string.px_rejection_label);
    }
}
