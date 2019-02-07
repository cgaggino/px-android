package com.mercadopago.android.px.internal.features;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.base.MvpView;
import com.mercadopago.android.px.internal.callbacks.OnSelectedCallback;
import com.mercadopago.android.px.model.BankDeal;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import java.util.List;

public interface BankDeals {

    interface View extends MvpView {
        void showBankDealDetail(@NonNull final BankDeal bankDeal);

        void showApiExceptionError(@NonNull final MercadoPagoError error);

        void showLoadingView();

        void showBankDeals(@NonNull final List<BankDeal> bankDeals,
            @NonNull final OnSelectedCallback<BankDeal> onSelectedCallback);
    }

    interface Actions {
        void trackView();

        void getBankDeals();

        void solveBankDeals(@NonNull final List<BankDeal> bankDeals);

        void recoverFromFailure();

        void initialize();
    }
}
