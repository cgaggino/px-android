package com.mercadopago.android.px.internal.features;

import com.mercadopago.android.px.internal.base.MvpView;

public interface BankDealDetail {

    interface View extends MvpView {
        void hideLogoName();

        void hideLogo();
    }
}
