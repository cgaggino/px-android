package com.mercadopago.android.px.model;

import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.io.Serializable;
import java.util.List;

/**
 * Amount configuration represents one hash_amount representation for cards. this DTO is strongly linked with a {@link
 * DiscountConfigurationModel}.
 */
@Keep
public final class AmountConfiguration implements Serializable {

    private static final int NO_SELECTED_PAYER_COST = -1;

    /**
     * default selected payer cost configuration for single payment method selection
     */
    private int selectedPayerCostIndex;

    /**
     * Payer cost configuration for single payment method selection
     */
    @NonNull private List<PayerCost> payerCosts;

    /**
     * Split payment node it it applies.
     */
    @Nullable private Split split;

    /**
     * The discount token associated with this configuration.
     */
    @Nullable private String discountToken;

    @NonNull
    public List<PayerCost> getPayerCosts() {
        return payerCosts;
    }

    public boolean allowSplit() {
        return split != null;
    }

    @NonNull
    public List<PayerCost> getAppliedPayerCost(final boolean userWantToSplit) {
        if (isSplitPossible(userWantToSplit)) {
            return getSplitConfiguration().primaryPaymentMethod.getPayerCosts();
        } else {
            return getPayerCosts();
        }
    }

    @NonNull
    public PayerCost getCurrentPayerCost(final boolean userWantToSplit, final int userSelectedIndex) {
        if (isSplitPossible(userWantToSplit)) {
            return PayerCost
                .getPayerCost(getSplitConfiguration().primaryPaymentMethod.getPayerCosts(), userSelectedIndex,
                    getSplitConfiguration().primaryPaymentMethod.selectedPayerCostIndex);
        } else {
            return PayerCost.getPayerCost(getPayerCosts(), userSelectedIndex,
                selectedPayerCostIndex);
        }
    }

    @Nullable
    public Split getSplitConfiguration() {
        return split;
    }

    @Nullable
    public String getDiscountToken() {
        return discountToken;
    }

    @Nullable
    public PayerCost getPayerCost(final int userSelectedPayerCost) {
        if (userSelectedPayerCost == NO_SELECTED_PAYER_COST) {
            return payerCosts.get(selectedPayerCostIndex);
        } else {
            return payerCosts.get(userSelectedPayerCost);
        }
    }

    private boolean isSplitPossible(final boolean userWantToSplit) {
        return userWantToSplit && allowSplit();
    }
}
