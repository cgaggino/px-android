package com.mercadopago.android.px.internal.features.bank_deal_detail;

import com.mercadopago.android.px.internal.base.MvpView;

/* default */ interface BankDealDetail {

    /* default */ interface View extends MvpView {
        void hideLogoName();

        void hideLogo();
    }
}
