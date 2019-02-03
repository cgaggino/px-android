package com.mercadopago.android.px.configuration;

import android.support.annotation.NonNull;
import java.io.Serializable;

/**
 * Advanced configuration provides you support for custom checkout functionality/configure special behaviour when
 * checkout is running.
 */
@SuppressWarnings("unused")
public final class AdvancedConfiguration implements Serializable {

    /**
     * Instores usage / money in usage. use case : not all bank deals apply right now to all preferences.
     */
    private final boolean bankDealsEnabled;
    private final boolean escEnabled;
    private final boolean expressEnabled;
    @NonNull private final PaymentResultScreenConfiguration paymentResultScreenConfiguration;
    @NonNull private final ReviewAndConfirmConfiguration reviewAndConfirmConfiguration;
    @NonNull private final DynamicFragmentConfiguration dynamicFragmentConfiguration;
    @NonNull private final DynamicDialogConfiguration dynamicDialogConfiguration;
    @NonNull private final CustomStringConfiguration customStringConfiguration;
    @NonNull private final DiscountParamsConfiguration discountParamsConfiguration;

    /* default */ AdvancedConfiguration(final Builder builder) {
        bankDealsEnabled = builder.bankDealsEnabled;
        escEnabled = builder.escEnabled;
        expressEnabled = builder.expressEnabled;
        paymentResultScreenConfiguration = builder.paymentResultScreenConfiguration;
        reviewAndConfirmConfiguration = builder.reviewAndConfirmConfiguration;
        dynamicFragmentConfiguration = builder.dynamicFragmentConfiguration;
        dynamicDialogConfiguration = builder.dynamicDialogConfiguration;
        customStringConfiguration = builder.customStringConfiguration;
        discountParamsConfiguration = builder.discountParamsConfiguration;
    }

    public boolean isBankDealsEnabled() {
        return bankDealsEnabled;
    }

    public boolean isEscEnabled() {
        return escEnabled;
    }

    @NonNull
    public DynamicFragmentConfiguration getDynamicFragmentConfiguration() {
        return dynamicFragmentConfiguration;
    }

    @NonNull
    public DynamicDialogConfiguration getDynamicDialogConfiguration() {
        return dynamicDialogConfiguration;
    }

    @NonNull
    public PaymentResultScreenConfiguration getPaymentResultScreenConfiguration() {
        return paymentResultScreenConfiguration;
    }

    @NonNull
    public ReviewAndConfirmConfiguration getReviewAndConfirmConfiguration() {
        return reviewAndConfirmConfiguration;
    }

    public boolean isExpressPaymentEnabled() {
        return expressEnabled;
    }

    @NonNull
    public CustomStringConfiguration getCustomStringConfiguration() {
        return customStringConfiguration;
    }

    @NonNull
    public DiscountParamsConfiguration getDiscountParamsConfiguration() {
        return discountParamsConfiguration;
    }

    @SuppressWarnings("unused")
    public static class Builder {
        /* default */ boolean bankDealsEnabled = true;
        /* default */ boolean escEnabled = false;
        /* default */ boolean expressEnabled = false;
        /* default */ @NonNull PaymentResultScreenConfiguration paymentResultScreenConfiguration =
            new PaymentResultScreenConfiguration.Builder().build();
        /* default */ @NonNull ReviewAndConfirmConfiguration reviewAndConfirmConfiguration =
            new ReviewAndConfirmConfiguration.Builder().build();
        /* default */ @NonNull DynamicFragmentConfiguration dynamicFragmentConfiguration =
            new DynamicFragmentConfiguration.Builder().build();
        /* default */ @NonNull DynamicDialogConfiguration dynamicDialogConfiguration =
            new DynamicDialogConfiguration.Builder().build();
        /* default */ @NonNull CustomStringConfiguration customStringConfiguration =
            new CustomStringConfiguration.Builder().build();
        /* default */ @NonNull DiscountParamsConfiguration discountParamsConfiguration =
            new DiscountParamsConfiguration.Builder().build();

        /**
         * Add the possibility to configure Bank's deals behaviour. If set as true, then the checkout will try to
         * retrieve bank deals. If set as false, then the checkout will not try to retrieve bank deals.
         *
         * @param bankDealsEnabled bool that reflects it's behaviour
         * @return builder to keep operating
         */
        public Builder setBankDealsEnabled(final boolean bankDealsEnabled) {
            this.bankDealsEnabled = bankDealsEnabled;
            return this;
        }

        /**
         * Add the possibility to configure ESC behaviour. If set as true, then saved cards will try to use ESC feature.
         * If set as false, then security code will be always asked.
         *
         * @param escEnabled bool that reflects it's behaviour
         * @return builder to keep operating
         */
        public Builder setEscEnabled(final boolean escEnabled) {
            this.escEnabled = escEnabled;
            return this;
        }

        /**
         * Enable to preset configurations to customize visualization on the 'Congrats' screen / 'PaymentResult' screen.
         * see {@link PaymentResultScreenConfiguration.Builder}
         *
         * @param paymentResultScreenConfiguration your custom preferences.
         * @return builder to keep operating
         */
        public Builder setPaymentResultScreenConfiguration(
            @NonNull final PaymentResultScreenConfiguration paymentResultScreenConfiguration) {
            this.paymentResultScreenConfiguration = paymentResultScreenConfiguration;
            return this;
        }

        /**
         * Enable to preset configurations to customize visualization on the 'Review and Confirm screen' see {@link
         * ReviewAndConfirmConfiguration.Builder}
         *
         * @param reviewAndConfirmConfiguration your custom preferences.
         * @return builder to keep operating
         */
        public Builder setReviewAndConfirmConfiguration(
            @NonNull final ReviewAndConfirmConfiguration reviewAndConfirmConfiguration) {
            this.reviewAndConfirmConfiguration = reviewAndConfirmConfiguration;
            return this;
        }

        /**
         * Enable to preset configurations to customize dynamic visualization on several screen locations see {@link
         * DynamicFragmentConfiguration.Builder}
         *
         * @param dynamicFragmentConfiguration your custom configurations.
         * @return builder to keep operating
         */
        public Builder setDynamicFragmentConfiguration(
            @NonNull final DynamicFragmentConfiguration dynamicFragmentConfiguration) {
            this.dynamicFragmentConfiguration = dynamicFragmentConfiguration;
            return this;
        }

        /**
         * Enable to preset configurations to customize dynamic visualization on several screen locations see {@link
         * DynamicFragmentConfiguration.Builder}
         *
         * @param dynamicDialogConfiguration your custom configurations.
         * @return builder to keep operating
         */
        public Builder setDynamicDialogConfiguration(
            @NonNull final DynamicDialogConfiguration dynamicDialogConfiguration) {
            this.dynamicDialogConfiguration = dynamicDialogConfiguration;
            return this;
        }

        /**
         * By default express checkout is disabled. This configuration allows you to turn on the express behaviour if
         * you have a private key available.
         *
         * @param enabled if your checkout supports express mode
         * @return builder to keep operating
         */
        public Builder setExpressPaymentEnable(final boolean enabled) {
            expressEnabled = enabled;
            return this;
        }

        /**
         * Enable to preset configurations to configure specific wordings on several screen locations see {@link
         * CustomStringConfiguration.Builder}
         *
         * @param customStringConfiguration your custom configurations.
         * @return builder to keep operating
         */
        public Builder setCustomStringConfiguration(
            @NonNull final CustomStringConfiguration customStringConfiguration) {
            this.customStringConfiguration = customStringConfiguration;
            return this;
        }

        /**
         * Set productId additional info that will be applied to filter Mercado Pago discounts.
         *
         * @param discountParamsConfiguration additional productId info
         * @return builder to keep operating
         */
        public Builder setDiscountParamsConfiguration(
            @NonNull final DiscountParamsConfiguration discountParamsConfiguration) {
            this.discountParamsConfiguration = discountParamsConfiguration;
            return this;
        }

        public AdvancedConfiguration build() {
            return new AdvancedConfiguration(this);
        }
    }
}
