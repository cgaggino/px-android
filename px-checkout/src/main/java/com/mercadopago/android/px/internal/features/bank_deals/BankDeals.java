package com.mercadopago.android.px.internal.features.bank_deals;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.base.MvpView;
import com.mercadopago.android.px.internal.callbacks.OnSelectedCallback;
import com.mercadopago.android.px.model.BankDeal;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import java.util.List;

/* default */ interface BankDeals {

    /* default */ interface View extends MvpView {
        void showBankDealDetail(@NonNull final BankDeal bankDeal);

        void showApiExceptionError(@NonNull final MercadoPagoError error);

        void showLoadingView();

        void showBankDeals(@NonNull final List<BankDeal> bankDeals,
            @NonNull final OnSelectedCallback<BankDeal> onSelectedCallback);
    }

    /* default */ interface Actions {
        void trackView();

        void getBankDeals();

        void recoverFromFailure();

        void initialize();
    }
}
